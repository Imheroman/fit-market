import { fitmarket } from '@/lib/axios'

let abortController = null

/**
 * ES Completion Suggester 기반 상품 자동완성 조회.
 * 이전 in-flight 요청은 자동 취소된다.
 * @param {string} query - 검색어
 * @param {number} limit - 최대 반환 개수 (기본 8)
 * @returns {Promise<Array<{id: number, name: string, categoryName: string, imageUrl: string}>>}
 */
export async function fetchAutocomplete(query, limit = 8) {
  if (abortController) abortController.abort()
  abortController = new AbortController()

  const res = await fitmarket.get('/search/autocomplete', {
    params: { q: query, limit },
    signal: abortController.signal
  })
  return res.data.data.products // ApiResponse.data.products
}
