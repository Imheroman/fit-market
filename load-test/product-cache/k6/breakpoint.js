/**
 * breakpoint.js — Breakpoint 테스트 (한계점 탐색)
 *
 * 목적: 시스템이 무너지기 시작하는 RPS 한계점 측정
 * 방식: ramping-arrival-rate (Open Model)
 *       — 서버 응답 속도와 무관하게 요청 주입 → 실제 한계점 정확 측정
 * 패턴: 10 RPS → 200 RPS (10분간 선형 증가)
 * 중단: 에러율 10% 초과 시 자동 중단 (abortOnFail + 30초 지속 확인)
 * 최대 VU: 500
 *
 * Open Model 선택 이유:
 *   Closed Model(ramping-vus)은 서버가 느려지면 요청률도 자동 감소
 *   → 실제 한계점보다 낮은 값이 측정됨.
 *   Open Model은 서버 응답 속도와 무관하게 RPS를 유지하므로
 *   실제 처리 한계를 정확히 관찰 가능.
 */
import http from 'k6/http';
import { check } from 'k6';
import { Trend, Rate } from 'k6/metrics';
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.1.0/index.js';
import { testParams, buildUrl } from './common.js';

// 커스텀 메트릭
const productResponseTime = new Trend('product_response_time', true);
const productErrorRate    = new Rate('product_error_rate');

export const options = {
  scenarios: {
    breakpoint: {
      executor: 'ramping-arrival-rate',   // Open Model ← 핵심
      startRate: 10,
      timeUnit: '1s',
      stages: [
        { duration: '10m', target: 200 },  // 10 → 200 RPS 선형 증가
      ],
      preAllocatedVUs: 100,
      maxVUs: 500,
    },
  },
  thresholds: {
    http_req_failed: [
      {
        threshold: 'rate<0.10',
        abortOnFail: true,          // ★ 에러율 10% 초과 시 자동 중단
        delayAbortEval: '30s',      // 30초 지속 후 중단 (순간적 스파이크 무시)
      },
    ],
    http_req_duration: ['p(95)<5000'],
    'product_response_time': ['p(95)<5000'],
  },
};

export default function () {
  // 상품 목록 API에 집중 (가장 무거운 케이스)
  const param = testParams[Math.floor(Math.random() * testParams.length)];
  const url = buildUrl(param);

  const res = http.get(url, {
    tags: { test: 'breakpoint', endpoint: param.tag },
    timeout: '60s',
  });

  const ok = check(res, {
    'status is 200': (r) => r.status === 200,
    'has body': (r) => r.body && r.body.length > 0,
  });

  // 커스텀 메트릭 기록
  productResponseTime.add(res.timings.duration, { endpoint: param.tag });
  productErrorRate.add(!ok);

  // Breakpoint는 think time 없음 (Open Model이므로 요청률은 executor가 제어)
}

export function handleSummary(data) {
  const resultsDir = __ENV.RESULTS_DIR || 'results';
  const phase = __ENV.PHASE || 'before';
  const timestamp = new Date().toISOString().replace(/[:.]/g, '-').slice(0, 19);
  const filename = `${resultsDir}/${phase}_breakpoint_${timestamp}.json`;

  return {
    stdout: textSummary(data, { indent: ' ', enableColors: true }),
    [filename]: JSON.stringify(data, null, 2),
  };
}
