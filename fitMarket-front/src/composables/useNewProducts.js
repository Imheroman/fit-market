import { onMounted, ref } from 'vue'
import { fetchNewProducts } from '@/api/productsApi'

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

export function useNewProducts({ page = 1, size = 12 } = {}) {
  const products = ref([])
  const isLoading = ref(false)
  const errorMessage = ref('')

  const loadProducts = async () => {
    isLoading.value = true
    errorMessage.value = ''
    try {
      const response = await fetchNewProducts({ page, size })
      const content = Array.isArray(response?.content) ? response.content : []
      products.value = content.map(mapProduct)
    } catch (error) {
      console.error(error)
      errorMessage.value = '신상품을 불러오지 못했습니다.'
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
    toggleFavorite,
    loadProducts,
  }
}
