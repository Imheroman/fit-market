import { ref, reactive, computed } from 'vue'

export const PRODUCT_CATEGORIES = [
  { value: 'lunchbox', label: '도시락' },
  { value: 'mealkit', label: '밀키트' },
  { value: 'salad', label: '샐러드' },
  { value: 'smoothie', label: '스무디' },
  { value: 'protein', label: '단백질 보충식' },
  { value: 'snack', label: '건강 간식' },
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
      // 실제로는 FormData로 이미지와 함께 서버에 전송
      // 백엔드에서 상품명/설명 기반으로 AI가 자동으로 식품 DB 매칭 후 영양정보 계산
      // const formData = new FormData()
      // formData.append('name', form.name)
      // formData.append('category', form.category)
      // formData.append('price', form.price)
      // formData.append('description', form.description)
      // formData.append('stock', form.stock)
      // formData.append('weight', form.weight)  // AI가 이 중량으로 영양정보 계산
      // formData.append('image', form.imageFile)
      // const response = await axios.post('/api/products', formData)

      await new Promise((resolve) => setTimeout(resolve, 500))

      // Mock: 이미지 URL 생성 (실제로는 서버에서 반환)
      const imageUrl = form.imageFile ? URL.createObjectURL(form.imageFile) : '/default-product.png'

      const newProduct = {
        id: sellerProducts.value.length + 101,
        sellerId: 2,
        sellerName: '건강한 밥상',
        name: form.name,
        category: form.category,
        price: Number(form.price),
        description: form.description,
        image: imageUrl,
        weight: Number(form.weight),
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
