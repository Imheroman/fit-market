import { ref } from 'vue'
import { fetchProducts } from '@/api/productsApi'

const products = ref([])
const isLoading = ref(false)
const errorMessage = ref('')
const pagination = ref({
  page: 1,
  size: 20,
  totalElements: 0,
  totalPages: 1,
  hasNext: false,
  hasPrevious: false,
})

const mapProduct = (item) => ({
  id: item.id,
  name: item.name,
  categoryId: item.categoryId ?? null,
  category: item.categoryName ?? '기타',
  price: item.price,
  image: item.imageUrl ? `http://localhost:8080/api${item.imageUrl}` : item.imageUrl,
  rating: item.rating ?? 0,
  reviews: item.reviewCount ?? 0,
  // 백엔드에서 평탄화된 구조로 전달됨
  calories: item.calories ?? 0,
  protein: item.protein ?? 0,
  carbs: item.carbs ?? 0,
  fat: item.fat ?? 0,
  isFavorite: false,
})

export function useProducts() {
  const loadProducts = async ({ page = pagination.value.page, size = pagination.value.size, categoryId, keyword } = {}) => {
    isLoading.value = true
    errorMessage.value = ''
    try {
      const response = await fetchProducts({ page, size, categoryId, keyword })
      products.value = (response.content ?? []).map(mapProduct)
      pagination.value = {
        page: response.page ?? page,
        size: response.size ?? size,
        totalElements: response.totalElements ?? products.value.length,
        totalPages: response.totalPages ?? 1,
        hasNext: response.hasNext ?? false,
        hasPrevious: response.hasPrevious ?? false,
      }
    } catch (error) {
      console.error(error)
      errorMessage.value = '상품을 불러오지 못했어요. 잠시 후 다시 시도해주세요.'
      pagination.value = {
        ...pagination.value,
        page,
        size,
        totalElements: 0,
        totalPages: 1,
        hasNext: false,
        hasPrevious: false,
      }
    } finally {
      isLoading.value = false
    }
  }

  const toggleFavorite = (productId) => {
    const product = products.value.find(p => p.id === productId)
    if (product) {
      product.isFavorite = !product.isFavorite
    }
  }

  const getProductById = (id) => {
    return products.value.find(p => p.id === id)
  }

  const searchProducts = async (keyword) => {
    return loadProducts({ keyword })
  }

  return {
    products,
    isLoading,
    errorMessage,
    pagination,
    toggleFavorite,
    getProductById,
    searchProducts,
    loadProducts,
  }
}
