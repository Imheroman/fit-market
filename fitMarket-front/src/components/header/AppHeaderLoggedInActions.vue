<template>
  <div class="flex items-center gap-3">
    <a
      href="/mypage"
      class="flex items-center gap-2 px-3 py-2 text-sm font-medium text-gray-700 hover:text-green-600 hover:bg-green-50 rounded-lg transition-colors"
    >
      <User class="w-4 h-4" />
      <span class="text-sm">{{ userName }}님</span>
    </a>
    <a href="/cart" class="relative p-2 hover:bg-green-50 rounded-lg transition-colors">
      <ShoppingCart class="w-5 h-5" />
      <span
        v-if="cartCount > 0"
        class="absolute -top-1 -right-1 w-4 h-4 bg-green-500 text-white text-xs rounded-full flex items-center justify-center"
      >
        {{ cartCount }}
      </span>
    </a>
    <button
      type="button"
      @click="emit('logout')"
      class="bg-gray-100 text-gray-700 hover:bg-gray-200 px-4 py-2 rounded-lg font-medium transition-colors"
    >
      로그아웃
    </button>
  </div>
</template>

<script setup>
import { User, UserRound, ShoppingCart } from 'lucide-vue-next'

const props = defineProps({
  cartCount: {
    type: Number,
    default: 0,
  },
  userName: {
    type: String,
    default: '게스트',
  },
})

const emit = defineEmits(['logout'])
</script>
