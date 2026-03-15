/**
 * smoke.js — Smoke 테스트 (동작 확인)
 *
 * 목적: API 정상 동작 확인 (다른 테스트의 전제 조건)
 * VU: 3 / 반복: 각 VU당 5회 / 최대 2분
 * Pass 기준: 에러 0건, p95 < 2s
 */
import http from 'k6/http';
import { check } from 'k6';
import { Trend, Rate } from 'k6/metrics';
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.1.0/index.js';
import { testParams, buildUrl } from './common.js';

// 커스텀 메트릭 (load-normal.js, load-heavy.js, breakpoint.js와 동일 구조)
const productResponseTime = new Trend('product_response_time', true);
const productErrorRate    = new Rate('product_error_rate');

export const options = {
  scenarios: {
    smoke: {
      executor: 'per-vu-iterations',
      vus: 3,
      iterations: 5,
      maxDuration: '2m',
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<2000'],
    http_req_failed:   ['rate<0.01'],
  },
};

export default function () {
  // 각 VU/반복 조합으로 파라미터를 순환
  const idx = (__ITER + __VU) % testParams.length;
  const param = testParams[idx];
  const url = buildUrl(param);

  const res = http.get(url, {
    tags: { endpoint: param.tag, test: 'smoke' },
    timeout: '10s',
  });

  const ok = check(res, {
    'status is 200': (r) => r.status === 200,
    'response time < 2s': (r) => r.timings.duration < 2000,
    'has body': (r) => r.body && r.body.length > 0,
    'body is JSON': (r) => {
      try {
        JSON.parse(r.body);
        return true;
      } catch {
        return false;
      }
    },
    'content field exists': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.content !== undefined || body.data !== undefined || Array.isArray(body);
      } catch {
        return false;
      }
    },
  });

  // 커스텀 메트릭 기록 (result-summarizer.py와의 일관성)
  productResponseTime.add(res.timings.duration, { endpoint: param.tag });
  productErrorRate.add(!ok);
}

export function handleSummary(data) {
  const resultsDir = __ENV.RESULTS_DIR || 'results';
  const phase = __ENV.PHASE || 'before';
  const timestamp = new Date().toISOString().replace(/[:.]/g, '-').slice(0, 19);
  return {
    stdout: textSummary(data, { indent: ' ', enableColors: true }),
    [`${resultsDir}/${phase}_smoke_${timestamp}.json`]: JSON.stringify(data, null, 2),
  };
}
