const BASE_URL = '/api/products'

export async function fetchProducts({ page = 1, size = 20 } = {}) {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  })

  const response = await fetch(`${BASE_URL}?${params.toString()}`)
  if (!response.ok) {
    throw new Error(`상품 조회 실패: ${response.status}`)
  }
  return response.json()
}
