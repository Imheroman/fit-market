import { fitmarket } from '@/lib/axios';

export async function fetchProducts({ page = 1, size = 20, categoryId, keyword } = {}) {
  const params = { page, size }
  if (categoryId) params.categoryId = categoryId
  if (keyword?.trim()) params.keyword = keyword.trim()

  const response = await fitmarket.get('/products', { params })
  return response.data
}

export async function createProduct(productData) {
  const response = await fitmarket.post('/products', productData)
  return response.data
}

export async function fetchSellerProducts() {
  const response = await fitmarket.get('/products/seller')
  return response.data
}

export async function updateProduct(productId, productData) {
  const response = await fitmarket.put(`/products/${productId}`, productData)
  return response.data
}

export async function deleteProduct(productId) {
  await fitmarket.delete(`/products/${productId}`)
}

export async function fetchBestProducts({ page = 1, size = 12 } = {}) {
  const response = await fitmarket.get('/products/best', { params: { page, size } })
  return response.data
}

export async function fetchNewProducts({ page = 1, size = 12 } = {}) {
  const response = await fitmarket.get('/products/new', { params: { page, size } })
  return response.data
}

export async function fetchProductDetail(productId) {
  const response = await fitmarket.get(`/products/${productId}`)
  return response.data
}

const CATEGORIES_PATH = '/categories'

export async function fetchCategories() {
  const response = await fitmarket.get(CATEGORIES_PATH)
  return response.data
}
