<template>
  <div class="min-h-screen bg-gradient-to-b from-green-50 to-white">
    <AppHeader />

    <div class="container mx-auto px-4 py-8">
      <!-- Breadcrumb -->
      <nav class="flex items-center gap-2 text-sm text-gray-600 mb-8">
        <a href="/" class="hover:text-green-600">홈</a>
        <ChevronRight class="w-4 h-4" />
        <a href="#" class="hover:text-green-600">{{ product.category }}</a>
        <ChevronRight class="w-4 h-4" />
        <span class="text-gray-900 font-medium">{{ product.name }}</span>
      </nav>

      <div class="grid md:grid-cols-2 gap-12 mb-16">
        <!-- Product Image -->
        <div class="space-y-4">
          <div class="relative aspect-square bg-green-50 rounded-2xl overflow-hidden">
            <img :src="product.image" :alt="product.name" class="w-full h-full object-cover" />
          </div>
        </div>

        <!-- Product Info -->
        <div>
          <span class="inline-block px-3 py-1 bg-green-100 text-green-700 text-sm font-medium rounded-full mb-4">
            {{ product.category }}
          </span>
          <h1 class="text-3xl md:text-4xl font-bold mb-4">{{ product.name }}</h1>
          
          <div class="flex items-center gap-2 mb-6">
            <div class="flex items-center gap-1">
              <Star v-for="i in 5" :key="i" :class="['w-5 h-5', i <= Math.floor(product.rating) ? 'fill-yellow-400 text-yellow-400' : 'text-gray-300']" />
            </div>
            <span class="text-lg font-semibold">{{ product.rating }}</span>
            <span class="text-gray-600">({{ product.reviews }}개 리뷰)</span>
          </div>

          <div class="text-4xl font-bold text-green-600 mb-8">{{ formattedPrice }}</div>

          <!-- Nutrition Info -->
          <div class="bg-gradient-to-br from-green-50 to-white border border-green-100 rounded-xl p-5 mb-6">
            <div class="flex items-center justify-between mb-3">
              <h3 class="text-base font-semibold flex items-center gap-2">
                <Flame class="w-4 h-4 text-orange-500" />
                총 영양정보
              </h3>
              <span class="text-xs text-gray-500">1회 제공량 기준</span>
            </div>

            <!-- 주요 영양소 -->
            <div class="grid grid-cols-4 gap-2 mb-3">
              <div class="bg-white rounded-lg p-2.5 border border-green-100 text-center">
                <div class="text-xs text-gray-600 mb-0.5">칼로리</div>
                <div class="text-lg font-bold">{{ nutrition.calories }}</div>
                <div class="text-xs text-gray-400">kcal</div>
              </div>
              <div class="bg-white rounded-lg p-2.5 border border-green-100 text-center">
                <div class="text-xs text-gray-600 mb-0.5">단백질</div>
                <div class="text-lg font-bold text-green-600">{{ nutrition.protein }}</div>
                <div class="text-xs text-gray-400">g</div>
              </div>
              <div class="bg-white rounded-lg p-2.5 border border-green-100 text-center">
                <div class="text-xs text-gray-600 mb-0.5">탄수화물</div>
                <div class="text-lg font-bold">{{ nutrition.carbs }}</div>
                <div class="text-xs text-gray-400">g</div>
              </div>
              <div class="bg-white rounded-lg p-2.5 border border-green-100 text-center">
                <div class="text-xs text-gray-600 mb-0.5">지방</div>
                <div class="text-lg font-bold">{{ nutrition.fat }}</div>
                <div class="text-xs text-gray-400">g</div>
              </div>
            </div>

            <!-- 세부 영양소 (간단한 리스트) -->
            <div class="border-t border-green-100 pt-3">
              <div class="grid grid-cols-2 gap-x-4 gap-y-1.5 text-xs">
                <div v-if="nutrition.sodium" class="flex justify-between text-gray-700">
                  <span>나트륨</span>
                  <span class="font-semibold">{{ nutrition.sodium }}mg</span>
                </div>
                <div v-if="nutrition.sugars" class="flex justify-between text-gray-700">
                  <span>당류</span>
                  <span class="font-semibold">{{ nutrition.sugars }}g</span>
                </div>
                <div v-if="nutrition.fiber" class="flex justify-between text-gray-700">
                  <span>식이섬유</span>
                  <span class="font-semibold">{{ nutrition.fiber }}g</span>
                </div>
                <div v-if="nutrition.saturatedFat" class="flex justify-between text-gray-700">
                  <span>포화지방</span>
                  <span class="font-semibold">{{ nutrition.saturatedFat }}g</span>
                </div>
                <div v-if="nutrition.transFat" class="flex justify-between text-gray-700">
                  <span>트랜스지방</span>
                  <span class="font-semibold">{{ nutrition.transFat }}g</span>
                </div>
                <div v-if="nutrition.calcium" class="flex justify-between text-gray-700">
                  <span>칼슘</span>
                  <span class="font-semibold">{{ nutrition.calcium }}mg</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Quantity & Add to Cart -->
          <div class="flex gap-4 mb-8">
            <div class="flex items-center border border-green-200 rounded-lg">
              <button @click="quantity > 1 && quantity--" class="px-4 py-3 hover:bg-green-50 transition-colors">
                <Minus class="w-5 h-5" />
              </button>
              <div class="px-6 py-3 font-semibold min-w-[60px] text-center">{{ quantity }}</div>
              <button @click="quantity++" class="px-4 py-3 hover:bg-green-50 transition-colors">
                <Plus class="w-5 h-5" />
              </button>
            </div>
            <button
              class="flex-1 bg-gradient-to-r from-green-500 to-green-600 hover:from-green-600 hover:to-green-700 text-white font-semibold py-3 px-6 rounded-lg flex items-center justify-center gap-2 transition-all"
              @click="handleAddToCart"
            >
              <ShoppingCart class="w-5 h-5" />
              장바구니에 담기
            </button>
          </div>

          <button
            class="w-full bg-green-900 hover:bg-green-800 text-white font-semibold py-4 px-6 rounded-lg transition-colors"
            @click="handleBuyNow"
          >
            바로 구매하기
          </button>
        </div>
      </div>

      <!-- Product Description -->
      <div class="border-t border-green-100 pt-12">
        <h2 class="text-2xl font-bold mb-6">상품 상세 정보</h2>
        <div class="prose max-w-none">
          <p class="text-gray-700 leading-relaxed mb-4">
            신선한 재료로 만든 건강한 한끼 식사입니다. 영양 균형을 고려하여 전문 영양사가 설계한 메뉴로,
            바쁜 일상 속에서도 건강한 식사를 즐기실 수 있습니다.
          </p>
          <p class="text-gray-700 leading-relaxed mb-4">
            모든 식재료는 식품의약품안전처의 표준 식품 DB를 기반으로 영양 성분이 정확하게 계산되었으며,
            신선도를 유지하기 위해 당일 조리하여 배송됩니다.
          </p>
        </div>
      </div>
    </div>

    <AppFooter />
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Star, Flame, ShoppingCart, ChevronRight, Minus, Plus } from 'lucide-vue-next'
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
import { useCart } from '@/composables/useCart'

const router = useRouter()
const route = useRoute()
const { addToCart } = useCart()

const quantity = ref(1)

// Mock 데이터 (실제로는 route.params.id로 API 호출해서 백엔드에서 계산된 총 영양정보를 받음)
const product = ref({
  id: Number(route.params.id) || 1,
  name: '그린 샐러드 도시락',
  category: '도시락',
  price: 8500,
  image: '/fresh-green-salad-bowl.png',
  rating: 4.8,
  reviews: 124,
  // 백엔드에서 계산된 총 영양정보 (상품 전체 기준)
  nutrition: {
    calories: 320,
    protein: 18,
    carbs: 35,
    fat: 12,
    sodium: 450,
    sugars: 8,
    fiber: 6,
    saturatedFat: 2.5,
    transFat: 0,
    calcium: 120,
  }
})

const formattedPrice = computed(() => `${product.value.price.toLocaleString()}원`)

// 백엔드에서 받은 총 영양정보를 그대로 사용
const nutrition = computed(() => product.value.nutrition)

const normalizedProduct = computed(() => ({
  id: product.value.id,
  name: product.value.name,
  category: product.value.category,
  price: product.value.price,
  image: product.value.image,
  calories: nutrition.value.calories,
  protein: nutrition.value.protein,
  carbs: nutrition.value.carbs,
  fat: nutrition.value.fat,
}))

const handleAddToCart = () => {
  addToCart(normalizedProduct.value, quantity.value)
}

const handleBuyNow = () => {
  handleAddToCart()
  router.push({ name: 'order-checkout' })
}
</script>
