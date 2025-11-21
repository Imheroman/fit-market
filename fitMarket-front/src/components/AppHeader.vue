<template>
  <header class="sticky top-0 z-50 bg-white/95 backdrop-blur-sm border-b border-green-100">
    <div class="container mx-auto px-4">
      <div class="flex items-center justify-between h-16">
        <a href="/" class="flex items-center gap-2">
          <div class="flex items-center justify-center w-10 h-10 bg-gradient-to-br from-green-500 to-green-600 rounded-xl">
            <Leaf class="w-6 h-6 text-white" />
          </div>
          <span class="text-2xl font-bold bg-gradient-to-r from-green-600 to-green-800 bg-clip-text text-transparent">
            초록초록
          </span>
        </a>

        <nav class="hidden md:flex items-center gap-8">
          <a href="/" class="text-sm font-medium hover:text-green-600 transition-colors">전체상품</a>
          <a href="#" class="text-sm font-medium text-gray-600 hover:text-green-600 transition-colors">베스트</a>
          <a href="#" class="text-sm font-medium text-gray-600 hover:text-green-600 transition-colors">신상품</a>
          <a href="#" class="text-sm font-medium text-gray-600 hover:text-green-600 transition-colors">판매자 등록</a>
        </nav>

        <div class="flex items-center gap-3">
          <button class="relative p-2 hover:bg-green-50 rounded-lg transition-colors">
            <Heart class="w-5 h-5" />
            <span class="absolute -top-1 -right-1 w-4 h-4 bg-green-500 text-white text-xs rounded-full flex items-center justify-center">
              3
            </span>
          </button>
          <a href="/cart" class="relative p-2 hover:bg-green-50 rounded-lg transition-colors">
            <ShoppingCart class="w-5 h-5" />
            <span v-if="cartCount > 0" class="absolute -top-1 -right-1 w-4 h-4 bg-green-500 text-white text-xs rounded-full flex items-center justify-center">
              {{ cartCount }}
            </span>
          </a>
          <button class="bg-gradient-to-r from-green-500 to-green-600 hover:from-green-600 hover:to-green-700 text-white px-4 py-2 rounded-lg font-medium transition-all">
            로그인
          </button>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { Leaf, Heart, ShoppingCart } from 'lucide-vue-next'
import { useCart } from '@/composables/useCart'

const { cartCount } = useCart()
</script>
