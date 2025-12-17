import { ref, reactive, computed } from 'vue'
import {
  createProduct,
  fetchSellerProducts,
  updateProduct as updateProductApi,
  deleteProduct as deleteProductApi,
} from '@/api/productsApi'
import { uploadImage } from '@/api/uploadApi'

export const PRODUCT_CATEGORIES = [
  { value: 'lunchbox', label: '도시락', categoryId: 1 },
  { value: 'mealkit', label: '밀키트', categoryId: 2 },
  { value: 'salad', label: '샐러드', categoryId: 3 },
  { value: 'protein', label: '단백질 보충식', categoryId: 4 },
  { value: 'smoothie', label: '스무디', categoryId: 5 },
]

const sellerProducts = ref([])

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
  const editingProductId = ref(null)

  const myProducts = computed(() => {
    return sellerProducts.value
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

      // 1. 이미지 파일 업로드
      let imageUrl = '/default-product.png'
      if (form.imageFile) {
        try {
          imageUrl = await uploadImage(form.imageFile)
        } catch (uploadError) {
          console.error('이미지 업로드 실패:', uploadError)
          throw new Error(uploadError.message || '이미지 업로드에 실패했습니다.')
        }
      }

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
      // API 응답에 stock이 없으면 입력값으로 대체
      const createdStock = response?.stock ?? productData.stock
      const newProduct = {
        id: response.id,
        sellerId: response.userId || 1,
        sellerName: '건강한 밥상',
        name: response.name,
        category: form.category,
        price: response.price,
        description: form.description,
        image: response.imageUrl ? `http://localhost:8080/api${response.imageUrl}` : response.imageUrl,
        weight: Number(form.weight),
        stock: createdStock,
        calories: response.calories,
        protein: response.protein,
        carbs: response.carbs,
        fat: response.fat,
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

  const deleteProduct = async (productId) => {
    try {
      isSubmitting.value = true
      errorMessage.value = ''
      successMessage.value = ''

      await deleteProductApi(productId)
      const index = sellerProducts.value.findIndex((p) => p.id === productId)
      if (index !== -1) {
        sellerProducts.value.splice(index, 1)
      }
      successMessage.value = '상품이 삭제되었습니다.'
      return true
    } catch (error) {
      console.error(error)
      errorMessage.value = error.message || '상품 삭제 중 오류가 발생했습니다.'
      return false
    } finally {
      isSubmitting.value = false
    }
  }

  const setEditingProduct = (product) => {
    editingProductId.value = product.id
    form.name = product.name
    form.category = product.category
    form.price = product.price.toString()
    form.description = product.description
    form.stock = product.stock.toString()
    form.weight = product.weight.toString()
    form.imageFile = null // 수정 시에는 이미지를 변경하지 않을 수도 있음
  }

  const submitProduct = async () => {
    // 수정 모드일 때는 이미지 파일 검증 건너뛰기 (기존 이미지 유지)
    if (editingProductId.value && !form.imageFile) {
      errors.imageFile = '' // 수정 시 이미지는 선택사항
    }

    // 수정 모드면 등록, 아니면 수정
    if (editingProductId.value) {
      return await updateProductSubmit()
    } else {
      return await registerProduct()
    }
  }

  const updateProductSubmit = async () => {
    // 이미지 제외하고 검증
    const tempImageFile = form.imageFile
    if (!tempImageFile) {
      form.imageFile = { name: 'placeholder' } // 임시 값 설정
    }

    if (!validate()) {
      form.imageFile = tempImageFile // 원래 값 복원
      errorMessage.value = '입력 항목을 확인해주세요.'
      return false
    }

    form.imageFile = tempImageFile // 원래 값 복원
    isSubmitting.value = true
    errorMessage.value = ''
    successMessage.value = ''

    try {
      const selectedCategory = PRODUCT_CATEGORIES.find((cat) => cat.value === form.category)
      if (!selectedCategory) {
        throw new Error('올바른 카테고리를 선택해주세요.')
      }

      // 기존 상품 찾기
      const existingProduct = sellerProducts.value.find((p) => p.id === editingProductId.value)

      // 이미지 업로드
      let imageUrl = existingProduct.image?.replace('http://localhost:8080/api', '') || existingProduct.image
      if (form.imageFile) {
        try {
          imageUrl = await uploadImage(form.imageFile)
        } catch (uploadError) {
          console.error('이미지 업로드 실패:', uploadError)
          throw new Error(uploadError.message || '이미지 업로드에 실패했습니다.')
        }
      }

      const productData = {
        name: form.name,
        categoryId: selectedCategory.categoryId,
        price: Number(form.price),
        description: form.description,
        stock: Number(form.stock),
        imageUrl: imageUrl,
        userId: 1,
      }

      const response = await updateProductApi(editingProductId.value, productData)

      // 로컬 상품 목록 업데이트
      const productIndex = sellerProducts.value.findIndex((p) => p.id === editingProductId.value)
      if (productIndex !== -1) {
        const updatedStock = response?.stock ?? productData.stock
        sellerProducts.value[productIndex] = {
          ...sellerProducts.value[productIndex],
          name: response?.name ?? form.name,
          category: form.category,
          price: response?.price ?? Number(form.price),
          description: form.description,
          image: response?.imageUrl ? `http://localhost:8080/api${response.imageUrl}` : sellerProducts.value[productIndex].image,
          weight: Number(form.weight),
          stock: updatedStock,
          calories: response?.calories ?? sellerProducts.value[productIndex].calories,
          protein: response?.protein ?? sellerProducts.value[productIndex].protein,
          carbs: response?.carbs ?? sellerProducts.value[productIndex].carbs,
          fat: response?.fat ?? sellerProducts.value[productIndex].fat,
        }
      }

      successMessage.value = '상품이 수정되었습니다.'
      Object.assign(form, createDefaultForm())
      editingProductId.value = null
      return true
    } catch (error) {
      console.error(error)
      errorMessage.value = error.message || '상품 수정 중 오류가 발생했습니다.'
      return false
    } finally {
      isSubmitting.value = false
    }
  }

  const resetForm = () => {
    Object.assign(form, createDefaultForm())
    resetErrors()
    successMessage.value = ''
    errorMessage.value = ''
    editingProductId.value = null
  }

  const loadSellerProducts = async () => {
    try {
      const products = await fetchSellerProducts()

      // 백엔드 데이터를 프론트엔드 형식으로 매핑
      sellerProducts.value = products.map((p) => {
        // categoryId로 category value 찾기
        const categoryObj = PRODUCT_CATEGORIES.find((cat) => cat.categoryId === p.categoryId)

        return {
          id: p.id,
          sellerId: 1, // 백엔드 응답에 없음
          sellerName: '건강한 밥상', // TODO: 실제 판매자 정보 사용
          name: p.name,
          category: categoryObj?.value || 'lunchbox',
          price: p.price,
          description: p.description ?? '',
          image: p.imageUrl ? `http://localhost:8080/api${p.imageUrl}` : p.imageUrl,
          weight: 0, // 백엔드 응답에 weight 없음
          stock: p.stock ?? 0,
          calories: p.calories,
          protein: p.protein,
          carbs: p.carbs,
          fat: p.fat,
          createdAt: new Date().toISOString(),
        }
      })
    } catch (error) {
      console.error('판매자 상품 조회 실패:', error)
      errorMessage.value = '상품 목록을 불러오지 못했습니다.'
    }
  }

  return {
    form,
    errors,
    isSubmitting,
    successMessage,
    errorMessage,
    editingProductId,
    myProducts,
    registerProduct,
    submitProduct,
    setEditingProduct,
    deleteProduct,
    resetForm,
    loadSellerProducts,
  }
}
