<template>
  <div class="min-h-screen bg-gradient-to-b from-green-50 to-white">
    <AppHeader />

    <div class="container mx-auto px-4 py-8">
      <h1 class="text-3xl font-bold mb-8">장바구니</h1>

      <div class="grid lg:grid-cols-3 gap-8">
        <!-- Cart Items -->
        <div class="lg:col-span-2 space-y-4">
          <div v-for="item in cartItems" :key="item.id" class="bg-white border border-green-100 rounded-xl p-6 flex gap-6">
            <img :src="item.image" :alt="item.name" class="w-24 h-24 object-cover rounded-lg bg-green-50" />
            
            <div class="flex-1">
              <h3 class="font-semibold text-lg mb-2">{{ item.name }}</h3>
              <p class="text-sm text-gray-600 mb-4">{{ item.category }}</p>
              
              <div class="flex items-center gap-4 mb-4">
                <div class="flex items-center border border-green-200 rounded-lg">
                  <button @click="updateQuantity(item.id, item.quantity - 1)" class="px-3 py-1 hover:bg-green-50 transition-colors">
                    <Minus class="w-4 h-4" />
                  </button>
                  <div class="px-4 py-1 font-semibold min-w-[40px] text-center">{{ item.quantity }}</div>
                  <button @click="updateQuantity(item.id, item.quantity + 1)" class="px-3 py-1 hover:bg-green-50 transition-colors">
                    <Plus class="w-4 h-4" />
                  </button>
                </div>
                
                <div class="text-xl font-bold text-green-600">{{ item.price }}</div>
              </div>

              <div class="flex gap-4 text-sm">
                <span class="text-gray-600">칼로리: <strong>{{ item.calories * item.quantity }}</strong>kcal</span>
                <span class="text-gray-600">단백질: <strong class="text-green-600">{{ item.protein * item.quantity }}g</strong></span>
              </div>
            </div>

            <button @click="removeItem(item.id)" class="text-gray-400 hover:text-red-500 transition-colors">
              <Trash2 class="w-5 h-5" />
            </button>
          </div>
        </div>

        <!-- Order Summary -->
        <div class="lg:col-span-1">
          <div class="bg-white border border-green-100 rounded-xl p-6 sticky top-24">
            <h3 class="text-xl font-bold mb-6">주문 요약</h3>
            
            <div class="space-y-4 mb-6">
              <div class="flex justify-between">
                <span class="text-gray-600">상품 금액</span>
                <span class="font-semibold">{{ totalPrice }}원</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-600">배송비</span>
                <span class="font-semibold">3,000원</span>
              </div>
              <div class="border-t border-green-100 pt-4 flex justify-between text-lg">
                <span class="font-bold">총 결제 금액</span>
                <span class="font-bold text-green-600">{{ totalPrice + 3000 }}원</span>
              </div>
            </div>

            <div class="bg-gradient-to-br from-green-50 to-white border border-green-100 rounded-lg p-4 mb-6">
              <h4 class="font-semibold mb-3 flex items-center gap-2">
                <Flame class="w-5 h-5 text-orange-500" />
                총 영양 정보
              </h4>
              <div class="grid grid-cols-2 gap-3">
                <div>
                  <div class="text-xs text-gray-600">칼로리</div>
                  <div class="text-lg font-bold">{{ totalNutrition.calories }}</div>
                </div>
                <div>
                  <div class="text-xs text-gray-600">단백질</div>
                  <div class="text-lg font-bold text-green-600">{{ totalNutrition.protein }}g</div>
                </div>
                <div>
                  <div class="text-xs text-gray-600">탄수화물</div>
                  <div class="text-lg font-bold">{{ totalNutrition.carbs }}g</div>
                </div>
                <div>
                  <div class="text-xs text-gray-600">지방</div>
                  <div class="text-lg font-bold">{{ totalNutrition.fat }}g</div>
                </div>
              </div>
            </div>

            <button
              class="w-full bg-gradient-to-r from-green-500 to-green-600 hover:from-green-600 hover:to-green-700 text-white font-semibold py-4 px-6 rounded-lg transition-all"
              @click="handleOrder"
            >
              주문하기
            </button>
          </div>
        </div>
      </div>
    </div>

    <AppFooter />
  </div>
</template>

<script setup>
import { Minus, Plus, Trash2, Flame } from 'lucide-vue-next'
import { useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
import { useCart } from '@/composables/useCart'

const router = useRouter()
const { cartItems, totalPrice, totalNutrition, updateQuantity, removeItem } = useCart()

const handleOrder = () => {
  router.push({ name: 'order-checkout' })
}
</script>
