import { ref, reactive, computed } from 'vue'

// 카테고리 옵션
export const PRODUCT_CATEGORIES = [
  { value: 'lunchbox', label: '도시락' },
  { value: 'mealkit', label: '밀키트' },
  { value: 'salad', label: '샐러드' },
  { value: 'smoothie', label: '스무디' },
  { value: 'protein', label: '단백질 보충식' },
  { value: 'snack', label: '건강 간식' },
]

// 판매자의 등록 상품 목록 (Mock)
const sellerProducts = ref([
  {
    id: 101,
    sellerId: 2,
    sellerName: '건강한 밥상',
    name: '퀴노아 닭가슴살 파워볼',
    category: 'protein',
    price: 9800,
    description: '고단백 저칼로리 식단으로 운동 후 최적의 식사입니다.',
    image: '/quinoa-chicken-bowl-healthy.png',
    calories: 380,
    protein: 35,
    carbs: 42,
    fat: 8,
    stock: 50,
    isActive: true,
    createdAt: '2025-01-20T10:00:00+09:00',
  },
  {
    id: 102,
    sellerId: 2,
    sellerName: '건강한 밥상',
    name: '지중해식 그릴 샐러드',
    category: 'salad',
    price: 11500,
    description: '신선한 채소와 올리브유로 만든 건강한 샐러드입니다.',
    image: '/mediterranean-salad.png',
    calories: 290,
    protein: 15,
    carbs: 25,
    fat: 16,
    stock: 30,
    isActive: true,
    createdAt: '2025-01-19T14:30:00+09:00',
  },
])

const createDefaultForm = () => ({
  name: '',
  category: '',
  price: '',
  description: '',
  calories: '',
  protein: '',
  carbs: '',
  fat: '',
  stock: '',
  image: '',
})

export function useSellerProducts() {
  const form = reactive(createDefaultForm())
  const errors = reactive({
    name: '',
    category: '',
    price: '',
    description: '',
    calories: '',
    protein: '',
    carbs: '',
    fat: '',
    stock: '',
    image: '',
  })
  const isSubmitting = ref(false)
  const successMessage = ref('')
  const errorMessage = ref('')

  // 현재 판매자의 상품 목록 (Mock - 실제로는 sellerId로 필터링)
  const myProducts = computed(() => {
    // TODO: 실제로는 로그인한 판매자의 ID로 필터링
    return sellerProducts.value
  })

  const activeProducts = computed(() => {
    return myProducts.value.filter((p) => p.isActive)
  })

  const inactiveProducts = computed(() => {
    return myProducts.value.filter((p) => !p.isActive)
  })

  const resetErrors = () => {
    Object.keys(errors).forEach((key) => {
      errors[key] = ''
    })
  }

  const validate = () => {
    resetErrors()
    let isValid = true

    if (!form.name.trim()) {
      errors.name = '상품명을 입력해주세요.'
      isValid = false
    } else if (form.name.trim().length < 3) {
      errors.name = '상품명은 최소 3자 이상 입력해주세요.'
      isValid = false
    }

    if (!form.category) {
      errors.category = '카테고리를 선택해주세요.'
      isValid = false
    }

    const price = Number(form.price)
    if (!form.price || isNaN(price) || price < 1000) {
      errors.price = '가격은 1,000원 이상이어야 합니다.'
      isValid = false
    }

    if (!form.description.trim()) {
      errors.description = '상품 설명을 입력해주세요.'
      isValid = false
    } else if (form.description.trim().length < 10) {
      errors.description = '상품 설명은 최소 10자 이상 입력해주세요.'
      isValid = false
    }

    const calories = Number(form.calories)
    if (!form.calories || isNaN(calories) || calories < 0) {
      errors.calories = '올바른 칼로리를 입력해주세요.'
      isValid = false
    }

    const protein = Number(form.protein)
    if (!form.protein || isNaN(protein) || protein < 0) {
      errors.protein = '올바른 단백질을 입력해주세요.'
      isValid = false
    }

    const carbs = Number(form.carbs)
    if (!form.carbs || isNaN(carbs) || carbs < 0) {
      errors.carbs = '올바른 탄수화물을 입력해주세요.'
      isValid = false
    }

    const fat = Number(form.fat)
    if (!form.fat || isNaN(fat) || fat < 0) {
      errors.fat = '올바른 지방을 입력해주세요.'
      isValid = false
    }

    const stock = Number(form.stock)
    if (!form.stock || isNaN(stock) || stock < 0) {
      errors.stock = '올바른 재고를 입력해주세요.'
      isValid = false
    }

    if (!form.image.trim()) {
      errors.image = '상품 이미지 URL을 입력해주세요.'
      isValid = false
    }

    return isValid
  }

  const registerProduct = async () => {
    if (!validate()) {
      errorMessage.value = '입력 항목을 확인해주세요.'
      return false
    }

    isSubmitting.value = true
    errorMessage.value = ''
    successMessage.value = ''

    try {
      // TODO: 실제 API 호출
      await new Promise((resolve) => setTimeout(resolve, 500))

      const newProduct = {
        id: sellerProducts.value.length + 101,
        sellerId: 2, // Mock
        sellerName: '건강한 밥상', // Mock
        name: form.name,
        category: form.category,
        price: Number(form.price),
        description: form.description,
        image: form.image,
        calories: Number(form.calories),
        protein: Number(form.protein),
        carbs: Number(form.carbs),
        fat: Number(form.fat),
        stock: Number(form.stock),
        isActive: true,
        createdAt: new Date().toISOString(),
      }

      sellerProducts.value.unshift(newProduct)
      successMessage.value = '상품이 등록되었습니다.'
      Object.assign(form, createDefaultForm())
      return true
    } catch (error) {
      console.error(error)
      errorMessage.value = '상품 등록 중 오류가 발생했습니다.'
      return false
    } finally {
      isSubmitting.value = false
    }
  }

  const updateProduct = async (productId, updates) => {
    try {
      // TODO: 실제 API 호출
      await new Promise((resolve) => setTimeout(resolve, 300))

      const product = sellerProducts.value.find((p) => p.id === productId)
      if (product) {
        Object.assign(product, updates)
      }
      return true
    } catch (error) {
      console.error(error)
      return false
    }
  }

  const toggleProductStatus = async (productId) => {
    const product = sellerProducts.value.find((p) => p.id === productId)
    if (!product) return false

    return updateProduct(productId, { isActive: !product.isActive })
  }

  const deleteProduct = async (productId) => {
    try {
      // TODO: 실제 API 호출
      await new Promise((resolve) => setTimeout(resolve, 300))

      const index = sellerProducts.value.findIndex((p) => p.id === productId)
      if (index !== -1) {
        sellerProducts.value.splice(index, 1)
      }
      return true
    } catch (error) {
      console.error(error)
      return false
    }
  }

  const resetForm = () => {
    Object.assign(form, createDefaultForm())
    resetErrors()
    successMessage.value = ''
    errorMessage.value = ''
  }

  return {
    form,
    errors,
    isSubmitting,
    successMessage,
    errorMessage,
    myProducts,
    activeProducts,
    inactiveProducts,
    registerProduct,
    updateProduct,
    toggleProductStatus,
    deleteProduct,
    resetForm,
  }
}
