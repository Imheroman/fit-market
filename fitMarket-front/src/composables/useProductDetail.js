import { onMounted, ref, watch } from 'vue'
import { fetchProductDetail } from '@/api/productsApi'

const mapProductDetail = (item) => ({
  id: item.id,
  name: item.name,
  category: item.categoryName ?? '기타',
  price: item.price,
  image: item.imageUrl,
  rating: item.rating ?? 0,
  reviews: item.reviewCount ?? 0,
  description: item.description ?? '',
  stock: item.stock ?? 0,
  nutrition: {
    calories: item.calories ?? 0,
    protein: item.protein ?? 0,
    carbs: item.carbs ?? 0,
    fat: item.fat ?? 0,
    // 상세 영양 정보는 백엔드 응답에 없어 기본값 사용
    sodium: null,
    sugars: null,
    fiber: null,
    saturatedFat: null,
    transFat: null,
    calcium: null,
  },
})

const createEmptyProduct = () => mapProductDetail({
  id: 0,
  name: '',
  categoryName: '기타',
  price: 0,
  imageUrl: '',
  rating: 0,
  reviewCount: 0,
  description: '',
  stock: 0,
  calories: 0,
  protein: 0,
  carbs: 0,
  fat: 0,
})

export function useProductDetail(productId) {
  const product = ref(createEmptyProduct())
  const isLoading = ref(false)
  const errorMessage = ref('')

  const loadProductDetail = async (id) => {
    if (!id) return
    isLoading.value = true
    errorMessage.value = ''
    try {
      const data = await fetchProductDetail(id)
      product.value = mapProductDetail(data)
    } catch (error) {
      console.error(error)
      errorMessage.value = '상품 정보를 불러오지 못했습니다.'
      product.value = createEmptyProduct()
    } finally {
      isLoading.value = false
    }
  }

  onMounted(() => loadProductDetail(productId.value ?? productId))

  watch(
    () => productId.value ?? productId,
    (newId) => loadProductDetail(newId),
  )

  return {
    product,
    isLoading,
    errorMessage,
    loadProductDetail,
  }
}
