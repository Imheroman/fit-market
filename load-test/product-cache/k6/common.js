/**
 * common.js — 공통 설정 (모든 k6 스크립트 공유)
 *
 * - BASE_URL: 환경변수 또는 기본값 http://localhost:8080
 * - testParams: test-params.csv에서 로드한 테스트 케이스 배열
 * - buildUrl(param): 완전한 요청 URL 생성
 */
import { SharedArray } from 'k6/data';

export const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

/**
 * test-params.csv를 파싱하여 SharedArray로 로드.
 * 헤더(첫 줄) 제거, 빈 줄 필터링, URL 쿼리 파라미터 조합.
 */
export const testParams = new SharedArray('testParams', function () {
  const raw = open('../data/test-params.csv');
  return raw
    .split('\n')
    .slice(1)                        // 헤더 제거
    .filter(line => line.trim())
    .map(line => {
      const parts = line.split(',');
      const endpoint  = (parts[0] || '').trim();
      const categoryId = (parts[1] || '').trim();
      const keyword    = (parts[2] || '').trim();
      const page       = (parts[3] || '').trim();
      const size       = (parts[4] || '20').trim();

      const params = [];
      if (categoryId) params.push(`categoryId=${encodeURIComponent(categoryId)}`);
      if (keyword)    params.push(`keyword=${encodeURIComponent(keyword)}`);
      if (page)       params.push(`page=${encodeURIComponent(page)}`);
      params.push(`size=${encodeURIComponent(size)}`);

      // 엔드포인트 유형 태그 (메트릭 분리용)
      let tag = 'products-list';
      if (endpoint.includes('/best')) tag = 'products-best';
      else if (endpoint.includes('/new')) tag = 'products-new';
      else if (categoryId && keyword) tag = 'products-category-keyword';
      else if (categoryId) tag = 'products-category';
      else if (keyword) tag = 'products-keyword';

      return {
        endpoint,
        query: params.join('&'),
        tag,
      };
    });
});

/**
 * param 객체로부터 완전한 요청 URL을 반환.
 * @param {Object} param - testParams 배열의 원소
 * @returns {string} 완전한 URL
 */
export function buildUrl(param) {
  return `${BASE_URL}${param.endpoint}?${param.query}`;
}

/**
 * 0~max-1 범위의 랜덤 정수 반환 (균등 분포).
 */
export function randomInt(max) {
  return Math.floor(Math.random() * max);
}

/**
 * [min, max] 범위의 랜덤 실수 반환.
 */
export function randomSleep(min, max) {
  return min + Math.random() * (max - min);
}
