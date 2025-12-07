import { ref, reactive, computed } from 'vue'
import { createProduct } from '@/api/productsApi'

export const PRODUCT_CATEGORIES = [
  { value: 'lunchbox', label: '도시락', categoryId: 1 },
  { value: 'mealkit', label: '밀키트', categoryId: 2 },
  { value: 'salad', label: '샐러드', categoryId: 1 },
  { value: 'smoothie', label: '스무디', categoryId: 2 },
  { value: 'protein', label: '단백질 보충식', categoryId: 1 },
  { value: 'snack', label: '건강 간식', categoryId: 2 },
]

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
  stock: '',
  weight: '',
  imageFile: null,
})

export function useSellerProducts() {
  const form = reactive(createDefaultForm())
  const errors = reactive({
    name: '',
    category: '',
    price: '',
    description: '',
    stock: '',
    weight: '',
    imageFile: '',
  })
  const isSubmitting = ref(false)
  const successMessage = ref('')
  const errorMessage = ref('')

  const myProducts = computed(() => {
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

    const stock = Number(form.stock)
    if (!form.stock || isNaN(stock) || stock < 0) {
      errors.stock = '올바른 재고를 입력해주세요.'
      isValid = false
    }

    const weight = Number(form.weight)
    if (!form.weight || isNaN(weight) || weight < 1) {
      errors.weight = '중량은 1g 이상이어야 합니다.'
      isValid = false
    }

    if (!form.imageFile) {
      errors.imageFile = '상품 이미지를 업로드해주세요.'
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
      // 카테고리 문자열을 백엔드 ID로 변환
      const selectedCategory = PRODUCT_CATEGORIES.find((cat) => cat.value === form.category)
      if (!selectedCategory) {
        throw new Error('올바른 카테고리를 선택해주세요.')
      }

      // TODO: 실제로는 이미지 파일을 먼저 업로드하고 URL을 받아야 함
      // 현재는 임시로 이미지 파일명을 imageUrl로 사용
      const imageUrl = form.imageFile ? form.imageFile.name : '/default-product.png'

      // 백엔드 API 호출
      const productData = {
        name: form.name,
        categoryId: selectedCategory.categoryId,
        price: Number(form.price),
        description: form.description,
        stock: Number(form.stock),
        imageUrl: imageUrl,
        userId: 1, // TODO: 인증 구현 후 실제 사용자 ID로 변경
      }

      const response = await createProduct(productData)

      // 로컬 상품 목록에 추가 (UI 업데이트용)
      const newProduct = {
        id: response.id,
        sellerId: response.userId || 1,
        sellerName: '건강한 밥상',
        name: response.name,
        category: form.category,
        price: response.price,
        description: form.description,
        image: response.imageUrl,
        weight: Number(form.weight),
        stock: response.stock,
        calories: response.calories,
        protein: response.protein,
        carbs: response.carbs,
        fat: response.fat,
        isActive: true,
        createdAt: new Date().toISOString(),
      }

      sellerProducts.value.unshift(newProduct)
      successMessage.value = '상품이 등록되었습니다.'
      Object.assign(form, createDefaultForm())
      return true
    } catch (error) {
      console.error(error)
      errorMessage.value = error.message || '상품 등록 중 오류가 발생했습니다.'
      return false
    } finally {
      isSubmitting.value = false
    }
  }

  const updateProduct = async (productId, updates) => {
    try {
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
