import { onMounted, ref } from 'vue'
import { fetchBestProducts } from '@/api/productsApi'

const mapProduct = (item) => ({
  id: item.id,
  name: item.name,
  category: item.categoryName ?? '기타',
  price: item.price,
  image: item.imageUrl ? `http://localhost:8080/api${item.imageUrl}` : item.imageUrl,
  rating: item.rating ?? 0,
  reviews: item.reviewCount ?? 0,
  calories: item.calories ?? 0,
  protein: item.protein ?? 0,
  carbs: item.carbs ?? 0,
  fat: item.fat ?? 0,
  isFavorite: false,
})

export function useBestProducts({ page = 1, size = 12 } = {}) {
  const products = ref([])
  const isLoading = ref(false)
  const errorMessage = ref('')
  const pagination = ref({
    page,
    size,
    totalElements: 0,
    totalPages: 1,
    hasNext: false,
    hasPrevious: false,
  })

  const loadProducts = async ({ page: nextPage = pagination.value.page, size: nextSize = pagination.value.size } = {}) => {
    isLoading.value = true
    errorMessage.value = ''
    try {
      const response = await fetchBestProducts({ page: nextPage, size: nextSize })
      const content = Array.isArray(response?.content) ? response.content : []
      products.value = content.map(mapProduct)
      pagination.value = {
        page: response.page ?? nextPage,
        size: response.size ?? nextSize,
        totalElements: response.totalElements ?? content.length,
        totalPages: response.totalPages ?? 1,
        hasNext: response.hasNext ?? false,
        hasPrevious: response.hasPrevious ?? false,
      }
    } catch (error) {
      console.error(error)
      errorMessage.value = '베스트 상품을 불러오지 못했습니다.'
      pagination.value = {
        ...pagination.value,
        page: nextPage,
        size: nextSize,
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
    const product = products.value.find((p) => p.id === productId)
    if (product) {
      product.isFavorite = !product.isFavorite
    }
  }

  onMounted(() => {
    if (!products.value.length) {
      loadProducts()
    }
  })

  return {
    products,
    isLoading,
    errorMessage,
    pagination,
    toggleFavorite,
    loadProducts,
  }
}
