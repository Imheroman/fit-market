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

export async function createProduct(productData) {
  const response = await fetch(BASE_URL, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(productData),
  })

  if (!response.ok) {
    const error = await response.text()
    throw new Error(`상품 등록 실패: ${error}`)
  }

  return response.json()
}

export async function fetchSellerProducts() {
  const response = await fetch(`${BASE_URL}/seller`, {
    credentials: 'include', // 쿠키 포함
  })
  if (!response.ok) {
    throw new Error(`판매자 상품 조회 실패: ${response.status}`)
  }
  return response.json()
}

export async function updateProduct(productId, productData) {
  const response = await fetch(`${BASE_URL}/${productId}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(productData),
  })

  if (!response.ok) {
    const error = await response.text()
    throw new Error(`상품 수정 실패: ${error}`)
  }

  return response.json()
}
