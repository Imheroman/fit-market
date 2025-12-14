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
    credentials: 'include',
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
    credentials: 'include',
    body: JSON.stringify(productData),
  })

  if (!response.ok) {
    const error = await response.text()
    throw new Error(`상품 수정 실패: ${error}`)
  }

  return response.json()
}

export async function deleteProduct(productId) {
  const response = await fetch(`${BASE_URL}/${productId}`, {
    method: 'DELETE',
    credentials: 'include',
  })

  if (!response.ok) {
    const error = await response.text()
    throw new Error(`상품 삭제 실패: ${error}`)
  }
}

export async function fetchBestProducts({ page = 1, size = 12 } = {}) {
  const params = new URLSearchParams({ page: String(page), size: String(size) })
  const response = await fetch(`${BASE_URL}/best?${params.toString()}`)
  if (!response.ok) {
    throw new Error(`베스트 상품 조회 실패: ${response.status}`)
  }
  return response.json()
}

export async function fetchNewProducts({ page = 1, size = 12 } = {}) {
  const params = new URLSearchParams({ page: String(page), size: String(size) })
  const response = await fetch(`${BASE_URL}/new?${params.toString()}`)
  if (!response.ok) {
    throw new Error(`신상품 조회 실패: ${response.status}`)
  }
  return response.json()
}

export async function fetchProductDetail(productId) {
  const response = await fetch(`${BASE_URL}/${productId}`)
  if (!response.ok) {
    throw new Error(`상품 상세 조회 실패: ${response.status}`)
  }
  return response.json()
}
