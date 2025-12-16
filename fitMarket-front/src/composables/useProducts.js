import { onMounted, ref } from 'vue'
import { fetchProducts } from '@/api/productsApi'

const products = ref([])
const isLoading = ref(false)
const errorMessage = ref('')

const mapProduct = (item) => ({
  id: item.id,
  name: item.name,
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
  const loadProducts = async () => {
    isLoading.value = true
    errorMessage.value = ''
    try {
      const response = await fetchProducts({ page: 1, size: 20 })
      products.value = (response.content ?? []).map(mapProduct)
    } catch (error) {
      console.error(error)
      errorMessage.value = '상품을 불러오지 못했어요. 잠시 후 다시 시도해주세요.'
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

  onMounted(() => {
    // 홈 진입 시마다 최신 목록을 불러온다
    loadProducts()
  })

  return {
    products,
    isLoading,
    errorMessage,
    toggleFavorite,
    getProductById,
    loadProducts,
  }
}
