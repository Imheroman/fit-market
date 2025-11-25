<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <AppHeader />

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
              @click="activeTab = 'register'"
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
              @click="activeTab = 'list'"
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
              <CheckCircle2 class="w-5 h-5" />
              <span>{{ successMessage }}</span>
            </div>

            <!-- 에러 메시지 -->
            <div
              v-if="errorMessage"
              class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg flex items-center gap-2 mb-6"
            >
              <AlertCircle class="w-5 h-5" />
              <span>{{ errorMessage }}</span>
            </div>

            <form @submit.prevent="handleSubmit" class="bg-white shadow-lg rounded-2xl p-6 md:p-8 border border-gray-200">
              <div class="space-y-6">
                <h2 class="text-xl font-semibold text-gray-900 pb-3 border-b border-gray-200">
                  상품 정보 입력
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
                    <p v-if="errors.category" class="mt-1 text-sm text-red-600">{{ errors.category }}</p>
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

                  <!-- 이미지 URL -->
                  <div class="md:col-span-2">
                    <label for="image" class="block text-sm font-medium text-gray-700 mb-2">
                      상품 이미지 URL <span class="text-red-500">*</span>
                    </label>
                    <input
                      id="image"
                      v-model="form.image"
                      type="text"
                      placeholder="/product-image.png"
                      class="w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
                      :class="errors.image ? 'border-red-300' : 'border-gray-300'"
                    />
                    <p v-if="errors.image" class="mt-1 text-sm text-red-600">{{ errors.image }}</p>
                  </div>
                </div>

                <!-- 영양 정보 -->
                <div class="pt-4">
                  <h3 class="text-lg font-semibold text-gray-900 mb-4">영양 정보</h3>
                  <div class="grid md:grid-cols-4 gap-4">
                    <div>
                      <label for="calories" class="block text-sm font-medium text-gray-700 mb-2">
                        칼로리 (kcal) <span class="text-red-500">*</span>
                      </label>
                      <input
                        id="calories"
                        v-model="form.calories"
                        type="number"
                        placeholder="320"
                        class="w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
                        :class="errors.calories ? 'border-red-300' : 'border-gray-300'"
                      />
                      <p v-if="errors.calories" class="mt-1 text-sm text-red-600">{{ errors.calories }}</p>
                    </div>

                    <div>
                      <label for="protein" class="block text-sm font-medium text-gray-700 mb-2">
                        단백질 (g) <span class="text-red-500">*</span>
                      </label>
                      <input
                        id="protein"
                        v-model="form.protein"
                        type="number"
                        placeholder="18"
                        class="w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
                        :class="errors.protein ? 'border-red-300' : 'border-gray-300'"
                      />
                      <p v-if="errors.protein" class="mt-1 text-sm text-red-600">{{ errors.protein }}</p>
                    </div>

                    <div>
                      <label for="carbs" class="block text-sm font-medium text-gray-700 mb-2">
                        탄수화물 (g) <span class="text-red-500">*</span>
                      </label>
                      <input
                        id="carbs"
                        v-model="form.carbs"
                        type="number"
                        placeholder="35"
                        class="w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
                        :class="errors.carbs ? 'border-red-300' : 'border-gray-300'"
                      />
                      <p v-if="errors.carbs" class="mt-1 text-sm text-red-600">{{ errors.carbs }}</p>
                    </div>

                    <div>
                      <label for="fat" class="block text-sm font-medium text-gray-700 mb-2">
                        지방 (g) <span class="text-red-500">*</span>
                      </label>
                      <input
                        id="fat"
                        v-model="form.fat"
                        type="number"
                        placeholder="12"
                        class="w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
                        :class="errors.fat ? 'border-red-300' : 'border-gray-300'"
                      />
                      <p v-if="errors.fat" class="mt-1 text-sm text-red-600">{{ errors.fat }}</p>
                    </div>
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
                  <p v-if="errors.description" class="mt-1 text-sm text-red-600">{{ errors.description }}</p>
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
                    <Loader2 v-if="isSubmitting" class="w-5 h-5 animate-spin" />
                    <span>{{ isSubmitting ? '등록 중...' : '상품 등록하기' }}</span>
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
                  <div class="text-sm text-gray-500">
                    전체 {{ myProducts.length }}개 (활성: {{ activeProducts.length }}개)
                  </div>
                </div>
              </div>

              <div v-if="myProducts.length === 0" class="p-12 text-center text-gray-500">
                <Package class="w-16 h-16 mx-auto mb-4 text-gray-300" />
                <p>등록된 상품이 없습니다.</p>
                <button
                  @click="activeTab = 'register'"
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
                          <h3 class="text-lg font-semibold text-gray-900">{{ product.name }}</h3>
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
                        <span class="font-semibold text-gray-900">{{ product.price.toLocaleString() }}원</span>
                        <span>재고: {{ product.stock }}개</span>
                        <span>{{ product.calories }}kcal</span>
                      </div>
                      <div class="mt-3 flex gap-2">
                        <button
                          @click="handleToggleStatus(product.id)"
                          class="px-4 py-2 text-sm border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
                        >
                          {{ product.isActive ? '판매중지' : '판매재개' }}
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

    <AppFooter />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { CheckCircle2, AlertCircle, Loader2, Package } from 'lucide-vue-next'
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
import { useSellerProducts, PRODUCT_CATEGORIES } from '@/composables/useSellerProducts'

const activeTab = ref('register')
const categories = PRODUCT_CATEGORIES

const {
  form,
  errors,
  isSubmitting,
  successMessage,
  errorMessage,
  myProducts,
  activeProducts,
  registerProduct,
  toggleProductStatus,
  deleteProduct,
  resetForm,
} = useSellerProducts()

const handleSubmit = async () => {
  const success = await registerProduct()
  if (success) {
    setTimeout(() => {
      activeTab.value = 'list'
    }, 1500)
  }
}

const handleReset = () => {
  if (confirm('입력한 내용을 모두 초기화하시겠습니까?')) {
    resetForm()
  }
}

const handleToggleStatus = async (productId) => {
  await toggleProductStatus(productId)
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
