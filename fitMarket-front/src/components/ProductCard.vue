<template>
  <div class="group overflow-hidden border border-green-100 hover:shadow-xl hover:border-green-300 transition-all duration-300 rounded-xl bg-white">
    <div class="relative overflow-hidden aspect-square bg-green-50">
      <img
        :src="product.image"
        :alt="product.name"
        class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500"
      />
      <button
        @click="$emit('toggle-favorite', product.id)"
        class="absolute top-3 right-3 w-9 h-9 rounded-full bg-white/90 backdrop-blur-sm hover:bg-white shadow-md flex items-center justify-center transition-colors"
      >
        <Heart
          :class="[
            'w-5 h-5',
            product.isFavorite ? 'fill-red-500 text-red-500' : 'text-gray-600'
          ]"
        />
      </button>
      <span class="absolute top-3 left-3 px-3 py-1 bg-green-500 text-white text-sm font-medium rounded-full">
        {{ product.category }}
      </span>
    </div>

    <div class="p-5">
      <div class="flex items-start justify-between mb-2">
        <h3 class="font-semibold text-lg">{{ product.name }}</h3>
      </div>

      <div class="flex items-center gap-1 mb-3">
        <Star class="w-4 h-4 fill-yellow-400 text-yellow-400" />
        <span class="text-sm font-medium">{{ product.rating }}</span>
        <span class="text-sm text-gray-600">({{ product.reviews }})</span>
      </div>

      <div class="grid grid-cols-4 gap-2 mb-4 p-3 bg-green-50 rounded-lg">
        <div class="text-center">
          <div class="text-xs text-gray-600 mb-1">칼로리</div>
          <div class="text-sm font-semibold">{{ product.calories }}</div>
        </div>
        <div class="text-center">
          <div class="text-xs text-gray-600 mb-1">단백질</div>
          <div class="text-sm font-semibold text-green-600">{{ product.protein }}g</div>
        </div>
        <div class="text-center">
          <div class="text-xs text-gray-600 mb-1">탄수화물</div>
          <div class="text-sm font-semibold">{{ product.carbs }}g</div>
        </div>
        <div class="text-center">
          <div class="text-xs text-gray-600 mb-1">지방</div>
          <div class="text-sm font-semibold">{{ product.fat }}g</div>
        </div>
      </div>

      <div class="flex items-center justify-between">
        <span class="text-xl font-bold text-green-600">{{ product.price }}</span>
        <button
          @click="$emit('add-to-cart', product.id)"
          class="bg-gradient-to-r from-green-500 to-green-600 hover:from-green-600 hover:to-green-700 text-white px-4 py-2 rounded-lg text-sm font-medium flex items-center gap-2 transition-all"
        >
          장바구니
          <ShoppingCart class="w-4 h-4" />
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Heart, Star, ShoppingCart } from 'lucide-vue-next'

defineProps({
  product: {
    type: Object,
    required: true
  }
})

defineEmits(['toggle-favorite', 'add-to-cart'])
</script>
