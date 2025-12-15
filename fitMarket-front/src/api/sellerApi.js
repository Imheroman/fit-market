const BASE_URL = '/api/seller'

export async function applySeller(data) {
  const response = await fetch(BASE_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify(data),
  })
  if (!response.ok) {
    const text = await response.text()
    throw new Error(text || `판매자 신청 실패 (${response.status})`)
  }
  return response.json()
}

export async function fetchMySellerApplication() {
  const response = await fetch(`${BASE_URL}/me`, {
    credentials: 'include',
  })
  if (!response.ok) {
    throw new Error(`판매자 신청 조회 실패 (${response.status})`)
  }
  return response.json()
}

export async function fetchSellerApplicationsByStatus(status = 'pending') {
  const url = status ? `${BASE_URL}?status=${encodeURIComponent(status)}` : BASE_URL
  const response = await fetch(url, { credentials: 'include' })
  if (!response.ok) {
    throw new Error(`판매자 신청 목록 조회 실패 (${response.status})`)
  }
  return response.json()
}

export async function reviewSellerApplication(id, decision, reviewNote = '') {
  const response = await fetch(`${BASE_URL}/${id}/review`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ decision, reviewNote }),
  })
  if (!response.ok) {
    const text = await response.text()
    throw new Error(text || `신청 처리 실패 (${response.status})`)
  }
  return response.json()
}
