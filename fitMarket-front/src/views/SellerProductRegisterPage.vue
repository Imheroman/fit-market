<template>
    <div class="min-h-screen bg-gray-50 flex flex-col">
        <AppHeader/>

        <main class="flex-1">
            <div class="container mx-auto px-4 py-10">
                <div class="space-y-8">
                    <!-- 헤더 -->
                    <header class="text-center md:text-left space-y-3">
                        <p class="text-sm font-semibold text-green-600">판매자 센터</p>
                        <h1 class="text-3xl md:text-4xl font-bold text-gray-900">상품 관리</h1>
                        <p class="text-gray-500">새로운 상품을 등록하고 기존 상품을 관리하세요.</p>
                    </header>

                    <!-- 탭 -->
                    <div class="flex gap-3 border-b border-gray-200">
                        <button
                            @click="handleTabChange('register')"
                            class="px-6 py-3 font-semibold transition-colors border-b-2"
                            :class="
                activeTab === 'register'
                  ? 'text-green-600 border-green-600'
                  : 'text-gray-500 border-transparent hover:text-gray-700'
              "
                        >
                            상품 등록
                        </button>
                        <button
                            @click="handleTabChange('list')"
                            class="px-6 py-3 font-semibold transition-colors border-b-2"
                            :class="
                activeTab === 'list'
                  ? 'text-green-600 border-green-600'
                  : 'text-gray-500 border-transparent hover:text-gray-700'
              "
                        >
                            등록 상품 목록
                        </button>
                    </div>

                    <!-- 상품 등록 폼 -->
                    <div v-if="activeTab === 'register'">
                        <!-- 성공 메시지 -->
                        <div
                            v-if="successMessage"
                            class="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-lg flex items-center gap-2 mb-6"
                        >
                            <CheckCircle2 class="w-5 h-5"/>
                            <span>{{ successMessage }}</span>
                        </div>

                        <!-- 에러 메시지 -->
                        <div
                            v-if="errorMessage"
                            class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg flex items-center gap-2 mb-6"
                        >
                            <AlertCircle class="w-5 h-5"/>
                            <span>{{ errorMessage }}</span>
                        </div>

                        <form @submit.prevent="handleSubmit"
                              class="bg-white shadow-lg rounded-2xl p-6 md:p-8 border border-gray-200">
                            <div class="space-y-6">
                                <h2 class="text-xl font-semibold text-gray-900 pb-3 border-b border-gray-200">
                                    {{ editingProductId ? '상품 정보 수정' : '상품 정보 입력' }}
                                </h2>

                                <div class="grid md:grid-cols-2 gap-6">
                                    <!-- 상품명 -->
                                    <div class="md:col-span-2">
                                        <label for="name" class="block text-sm font-medium text-gray-700 mb-2">
                                            상품명 <span class="text-red-500">*</span>
                                        </label>
                                        <input
                                            id="name"
                                            v-model="form.name"
                                            type="text"
                                            placeholder="예: 그린 샐러드 도시락"
                                            class="w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
                                            :class="errors.name ? 'border-red-300' : 'border-gray-300'"
                                        />
                                        <p v-if="errors.name" class="mt-1 text-sm text-red-600">{{ errors.name }}</p>
                                    </div>

                                    <!-- 카테고리 -->
                                    <div>
                                        <label for="category" class="block text-sm font-medium text-gray-700 mb-2">
                                            카테고리 <span class="text-red-500">*</span>
                                        </label>
                                        <select
                                            id="category"
                                            v-model="form.category"
                                            class="w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
                                            :class="errors.category ? 'border-red-300' : 'border-gray-300'"
                                        >
                                            <option value="">선택해주세요</option>
                                            <option v-for="cat in categories" :key="cat.value" :value="cat.value">
                                                {{ cat.label }}
                                            </option>
                                        </select>
                                        <p v-if="errors.category" class="mt-1 text-sm text-red-600">{{
                                                errors.category
                                            }}</p>
                                    </div>

                                    <!-- 가격 -->
                                    <div>
                                        <label for="price" class="block text-sm font-medium text-gray-700 mb-2">
                                            가격 (원) <span class="text-red-500">*</span>
                                        </label>
                                        <input
                                            id="price"
                                            v-model="form.price"
                                            type="number"
                                            placeholder="9800"
                                            class="w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
                                            :class="errors.price ? 'border-red-300' : 'border-gray-300'"
                                        />
                                        <p v-if="errors.price" class="mt-1 text-sm text-red-600">{{ errors.price }}</p>
                                    </div>

                                    <!-- 재고 -->
                                    <div>
                                        <label for="stock" class="block text-sm font-medium text-gray-700 mb-2">
                                            재고 수량 <span class="text-red-500">*</span>
                                        </label>
                                        <input
                                            id="stock"
                                            v-model="form.stock"
                                            type="number"
                                            placeholder="50"
                                            class="w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
                                            :class="errors.stock ? 'border-red-300' : 'border-gray-300'"
                                        />
                                        <p v-if="errors.stock" class="mt-1 text-sm text-red-600">{{ errors.stock }}</p>
                                    </div>

                                    <!-- 상품 이미지 업로드 -->
                                    <div class="md:col-span-2">
                                        <label for="image" class="block text-sm font-medium text-gray-700 mb-2">
                                            상품 이미지 <span class="text-red-500">*</span>
                                        </label>
                                        <div class="flex items-center gap-4">
                                            <label
                                                class="flex-1 flex items-center justify-center px-4 py-3 border-2 border-dashed rounded-lg cursor-pointer transition-colors"
                                                :class="errors.imageFile ? 'border-red-300 bg-red-50' : form.imageFile ? 'border-green-300 bg-green-50' : 'border-gray-300 hover:border-green-400 hover:bg-green-50'"
                                            >
                                                <input
                                                    id="image"
                                                    type="file"
                                                    accept="image/*"
                                                    class="hidden"
                                                    @change="handleImageChange"
                                                />
                                                <div class="text-center">
                                                    <Upload class="w-6 h-6 mx-auto mb-2 text-gray-400"/>
                                                    <p class="text-sm text-gray-600">
                                                        {{ form.imageFile ? form.imageFile.name : '이미지를 선택하거나 드래그하세요' }}
                                                    </p>
                                                    <p class="text-xs text-gray-500 mt-1">JPG, PNG, GIF (최대 5MB)</p>
                                                </div>
                                            </label>
                                            <div v-if="imagePreview"
                                                 class="w-24 h-24 border border-gray-200 rounded-lg overflow-hidden">
                                                <img :src="imagePreview" alt="미리보기" class="w-full h-full object-cover"/>
                                            </div>
                                        </div>
                                        <p v-if="errors.imageFile" class="mt-1 text-sm text-red-600">{{
                                                errors.imageFile
                                            }}</p>
                                    </div>
                                </div>

                                <!-- 중량 입력 -->
                                <div class="pt-4 border-t border-gray-200">
                                    <div class="flex items-start gap-3 mb-4">
                                        <div
                                            class="w-10 h-10 bg-blue-50 rounded-lg flex items-center justify-center flex-shrink-0">
                                            <svg class="w-5 h-5 text-blue-600" fill="none" stroke="currentColor"
                                                 viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                                      d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"></path>
                                            </svg>
                                        </div>
                                        <div class="flex-1">
                                            <h3 class="text-lg font-semibold text-gray-900 mb-1">AI 기반 영양정보 자동 계산</h3>
                                            <p class="text-sm text-gray-600">
                                                상품명과 설명을 기반으로 AI가 식품 DB를 자동으로 매칭해서 영양정보를 계산해요.
                                            </p>
                                        </div>
                                    </div>

                                    <div class="max-w-md">
                                        <label for="weight" class="block text-sm font-medium text-gray-700 mb-2">
                                            1회 제공량 중량 (g) <span class="text-red-500">*</span>
                                        </label>
                                        <input
                                            id="weight"
                                            v-model="form.weight"
                                            type="number"
                                            placeholder="350"
                                            min="1"
                                            class="w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
                                            :class="errors.weight ? 'border-red-300' : 'border-gray-300'"
                                        />
                                        <p v-if="errors.weight" class="mt-1 text-sm text-red-600">{{
                                                errors.weight
                                            }}</p>
                                        <p class="text-xs text-gray-500 mt-1">
                                            예: 샐러드 도시락 350g, 닭가슴살 150g
                                        </p>
                                    </div>
                                </div>

                                <!-- 상품 설명 -->
                                <div>
                                    <label for="description" class="block text-sm font-medium text-gray-700 mb-2">
                                        상품 설명 <span class="text-red-500">*</span>
                                    </label>
                                    <textarea
                                        id="description"
                                        v-model="form.description"
                                        rows="5"
                                        placeholder="상품의 특징과 장점을 자세히 설명해주세요. (최소 10자)"
                                        class="w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 resize-none"
                                        :class="errors.description ? 'border-red-300' : 'border-gray-300'"
                                    ></textarea>
                                    <p v-if="errors.description" class="mt-1 text-sm text-red-600">{{
                                            errors.description
                                        }}</p>
                                </div>

                                <!-- 버튼 -->
                                <div class="flex gap-3 pt-4">
                                    <button
                                        type="button"
                                        @click="handleReset"
                                        class="flex-1 px-6 py-3 bg-gray-100 text-gray-700 rounded-lg font-medium hover:bg-gray-200 transition-colors"
                                        :disabled="isSubmitting"
                                    >
                                        초기화
                                    </button>
                                    <button
                                        type="submit"
                                        class="flex-1 px-6 py-3 bg-green-600 text-white rounded-lg font-medium hover:bg-green-700 transition-colors disabled:bg-gray-300 disabled:cursor-not-allowed flex items-center justify-center gap-2"
                                        :disabled="isSubmitting"
                                    >
                                        <Loader2 v-if="isSubmitting" class="w-5 h-5 animate-spin"/>
                                        <span>{{
                                                isSubmitting ? (editingProductId ? '수정 중...' : '등록 중...') : (editingProductId ? '상품 수정하기' : '상품 등록하기')
                                            }}</span>
                                    </button>
                                </div>
                            </div>
                        </form>
                    </div>

                    <!-- 등록 상품 목록 -->
                    <div v-else-if="activeTab === 'list'">
                        <div class="bg-white shadow-lg rounded-2xl border border-gray-200 overflow-hidden">
                            <div class="p-6 border-b border-gray-200">
                                <div class="flex items-center justify-between">
                                    <h2 class="text-xl font-semibold text-gray-900">내 상품 목록</h2>
                                    <div class="text-sm text-gray-500">전체 {{ myProducts.length }}개</div>
                                </div>
                            </div>

                            <div v-if="myProducts.length === 0" class="p-12 text-center text-gray-500">
                                <Package class="w-16 h-16 mx-auto mb-4 text-gray-300"/>
                                <p>등록된 상품이 없습니다.</p>
                                <button
                                    @click="handleTabChange('register')"
                                    class="mt-4 px-6 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors"
                                >
                                    상품 등록하기
                                </button>
                            </div>

                            <div v-else class="divide-y divide-gray-200">
                                <div
                                    v-for="product in myProducts"
                                    :key="product.id"
                                    class="p-6 hover:bg-gray-50 transition-colors"
                                >
                                    <div class="flex gap-4">
                                        <img
                                            :src="product.image"
                                            :alt="product.name"
                                            class="w-24 h-24 object-cover rounded-lg"
                                        />
                                        <div class="flex-1">
                                            <div class="flex items-start justify-between">
                                                <div>
                                                    <h3 class="text-lg font-semibold text-gray-900">{{
                                                            product.name
                                                        }}</h3>
                                                    <p class="text-sm text-gray-500 mt-1">
                                                        {{ getCategoryLabel(product.category) }}
                                                    </p>
                                                </div>
                                                <div class="flex items-center gap-2">
                          <span
                              class="px-3 py-1 rounded-full text-xs font-medium"
                              :class="
                              product.isActive
                                ? 'bg-green-100 text-green-700'
                                : 'bg-gray-100 text-gray-600'
                            "
                          >
                            {{ product.isActive ? '판매중' : '판매중지' }}
                          </span>
                                                </div>
                                            </div>
                                            <div class="mt-3 flex items-center gap-6 text-sm text-gray-600">
                                                <span class="font-semibold text-gray-900">{{
                                                        product.price.toLocaleString()
                                                    }}원</span>
                                                <span>재고: {{ product.stock }}개</span>
                                                <span>{{ product.calories }}kcal</span>
                                            </div>
                                            <div class="mt-3 flex gap-2">
                                                <button
                                                    @click="handleEditProduct(product)"
                                                    class="px-4 py-2 text-sm text-blue-600 border border-blue-300 rounded-lg hover:bg-blue-50 transition-colors"
                                                >
                                                    수정
                                                </button>
                                                <button
                                                    @click="handleDeleteProduct(product.id)"
                                                    class="px-4 py-2 text-sm text-red-600 border border-red-300 rounded-lg hover:bg-red-50 transition-colors"
                                                >
                                                    삭제
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </main>

        <AppFooter/>
    </div>
</template>

<script setup>
import {ref, onMounted} from 'vue'
import {CheckCircle2, AlertCircle, Loader2, Package, Upload} from 'lucide-vue-next'
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
import {useSellerProducts, PRODUCT_CATEGORIES} from '@/composables/useSellerProducts'

const activeTab = ref('register')
const categories = PRODUCT_CATEGORIES
const imagePreview = ref(null)

const {
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
} = useSellerProducts()

// 페이지 로드 시 판매자 상품 목록 조회
onMounted(() => {
    loadSellerProducts()
})

// 탭 변경 핸들러
const handleTabChange = async (tab) => {
    activeTab.value = tab
    if (tab === 'list') {
        await loadSellerProducts()
    }
}

const handleImageChange = (event) => {
    const file = event.target.files[0]
    if (!file) return

    // 파일 크기 체크 (5MB)
    if (file.size > 5 * 1024 * 1024) {
        errors.imageFile = '이미지 크기는 5MB 이하여야 합니다.'
        return
    }

    form.imageFile = file
    errors.imageFile = ''

    // 이미지 미리보기
    const reader = new FileReader()
    reader.onload = (e) => {
        imagePreview.value = e.target.result
    }
    reader.readAsDataURL(file)
}

const handleSubmit = async () => {
    const success = await submitProduct()
    if (success) {
        imagePreview.value = null
        setTimeout(() => {
            handleTabChange('list')
        }, 1500)
    }
}

const handleEditProduct = (product) => {
    setEditingProduct(product)
    activeTab.value = 'register'
}

const handleReset = () => {
    if (confirm('입력한 내용을 모두 초기화하시겠습니까?')) {
        resetForm()
        imagePreview.value = null
    }
}

const handleDeleteProduct = async (productId) => {
    if (confirm('정말 이 상품을 삭제하시겠습니까?')) {
        await deleteProduct(productId)
    }
}

const getCategoryLabel = (value) => {
    const category = categories.find((c) => c.value === value)
    return category ? category.label : value
}
</script>
