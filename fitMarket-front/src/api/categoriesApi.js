const BASE_URL = '/api/categories'

export async function fetchCategories() {
  const response = await fetch(BASE_URL)
  if (!response.ok) {
    throw new Error(`카테고리 조회 실패: ${response.status}`)
  }
  return response.json()
}