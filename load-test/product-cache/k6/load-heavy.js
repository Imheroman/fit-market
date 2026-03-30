/**
 * load-heavy.js — Heavy Load 테스트 (많은 부하)
 *
 * 목적: 높은 트래픽 이벤트(세일, 신제품 출시) 시나리오 → 한계 관찰
 * VU 패턴:
 *   0 → 30   (2분 ramp-up)      ← Normal 수준 경유
 *   30 유지  (3분 steady)
 *   30 → 100 (2분 증가)
 *   100 유지 (5분 steady) ← 핵심 측정 구간
 *   100 → 200 (2분 증가)  ← 극한 관찰
 *   200 유지  (3분 steady)
 *   200 → 0   (3분 ramp-down)
 * 총 기간: 20분
 * Think time: 0.1~0.3초 (이벤트 시 급박한 탐색 패턴)
 *
 * SLA (Before Redis):
 *   p95 < 2,000ms / p99 < 4,000ms / 에러율 < 5%
 * SLA (After Redis):
 *   p95 < 100ms / p99 < 200ms
 */
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate } from 'k6/metrics';
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.1.0/index.js';
import { testParams, buildUrl, randomSleep } from './common.js';

// 커스텀 메트릭
const productResponseTime = new Trend('product_response_time', true);
const productErrorRate    = new Rate('product_error_rate');

export const options = {
  scenarios: {
    heavy_load: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '2m',  target: 30  },  // Normal 수준 경유
        { duration: '3m',  target: 30  },  // 안정화
        { duration: '2m',  target: 100 },  // ★ Heavy 진입
        { duration: '5m',  target: 100 },  // ★ 핵심 측정 구간
        { duration: '2m',  target: 200 },  // 극한
        { duration: '3m',  target: 200 },  // 극한 관찰
        { duration: '3m',  target: 0   },  // ramp-down
      ],
      gracefulRampDown: '30s',
    },
  },
  thresholds: {
    'http_req_duration':     ['p(95)<2000', 'p(99)<4000'],  // 완화된 기준
    'http_req_failed':       ['rate<0.05'],
    'product_response_time': ['p(95)<2000'],
    'product_error_rate':    ['rate<0.05'],
  },
};

export default function () {
  // 랜덤 파라미터 선택 (급박한 이벤트 탐색 패턴)
  const param = testParams[Math.floor(Math.random() * testParams.length)];
  const url = buildUrl(param);

  const res = http.get(url, {
    tags: { test: 'heavy', endpoint: param.tag },
    timeout: '60s',
  });

  const ok = check(res, {
    'status is 200': (r) => r.status === 200,
    'response time < 2s': (r) => r.timings.duration < 2000,
    'has body': (r) => r.body && r.body.length > 0,
  });

  // 커스텀 메트릭 기록 (엔드포인트 태그별 분리)
  productResponseTime.add(res.timings.duration, { endpoint: param.tag });
  productErrorRate.add(!ok);

  // Think time: 0.1~0.3초 (이벤트 시 급박한 탐색 패턴)
  sleep(randomSleep(0.1, 0.3));
}

export function handleSummary(data) {
  const resultsDir = __ENV.RESULTS_DIR || 'results';
  const phase = __ENV.PHASE || 'before';
  const timestamp = new Date().toISOString().replace(/[:.]/g, '-').slice(0, 19);
  const filename = `${resultsDir}/${phase}_heavy_${timestamp}.json`;

  return {
    stdout: textSummary(data, { indent: ' ', enableColors: true }),
    [filename]: JSON.stringify(data, null, 2),
  };
}
