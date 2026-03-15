/**
 * load-normal.js — Normal Load 테스트 (무난한 부하)
 *
 * 목적: 일상적 트래픽 수준에서 성능 기준선(baseline) 확보
 * VU 패턴:
 *   0 → 10 (1분 ramp-up)
 *   10 유지 (3분 warm-up steady)
 *   10 → 30 (1분 증가)
 *   30 유지 (5분 steady) ← 핵심 측정 구간
 *   30 → 0  (1분 ramp-down)
 * 총 기간: 11분
 * Think time: 0.3~0.7초
 *
 * SLA (Before Redis):
 *   p95 < 1,000ms / p99 < 2,000ms / 에러율 < 1%
 * SLA (After Redis):
 *   p95 < 50ms / p99 < 100ms
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
    normal_load: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '1m',  target: 10 },  // ramp-up
        { duration: '3m',  target: 10 },  // warm-up steady
        { duration: '1m',  target: 30 },  // 증가
        { duration: '5m',  target: 30 },  // ★ 핵심 측정 구간
        { duration: '1m',  target: 0  },  // ramp-down
      ],
      gracefulRampDown: '30s',
    },
  },
  thresholds: {
    'http_req_duration':     ['p(95)<1000', 'p(99)<2000'],
    'http_req_failed':       ['rate<0.01'],
    'product_response_time': ['p(95)<1000'],
    'product_error_rate':    ['rate<0.01'],
  },
};

export default function () {
  // 랜덤 파라미터 선택 (현실적 사용자 탐색 패턴)
  const param = testParams[Math.floor(Math.random() * testParams.length)];
  const url = buildUrl(param);

  const res = http.get(url, {
    tags: { test: 'normal', endpoint: param.tag },
    timeout: '30s',
  });

  const ok = check(res, {
    'status is 200': (r) => r.status === 200,
    'response time < 1s': (r) => r.timings.duration < 1000,
    'has body': (r) => r.body && r.body.length > 0,
  });

  // 커스텀 메트릭 기록 (엔드포인트 태그별 분리)
  productResponseTime.add(res.timings.duration, { endpoint: param.tag });
  productErrorRate.add(!ok);

  // Think time: 0.3~0.7초 (현실적 사용자 행동)
  sleep(randomSleep(0.3, 0.7));
}

export function handleSummary(data) {
  const resultsDir = __ENV.RESULTS_DIR || 'results';
  const phase = __ENV.PHASE || 'before';
  const timestamp = new Date().toISOString().replace(/[:.]/g, '-').slice(0, 19);
  const filename = `${resultsDir}/${phase}_normal_${timestamp}.json`;

  return {
    stdout: textSummary(data, { indent: ' ', enableColors: true }),
    [filename]: JSON.stringify(data, null, 2),
  };
}
