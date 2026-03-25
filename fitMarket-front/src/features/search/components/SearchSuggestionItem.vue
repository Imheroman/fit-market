<template>
  <li
    class="flex items-center gap-3 px-4 py-2 cursor-pointer hover:bg-green-50 transition-colors"
    :class="{ 'bg-green-50': isActive }"
    @mousedown.prevent="$emit('select', product)"
  >
    <img
      v-if="product.imageUrl"
      :src="imageFullUrl"
      :alt="product.name"
      class="w-8 h-8 object-cover rounded"
    />
    <div class="w-8 h-8 bg-gray-100 rounded flex items-center justify-center" v-else>
      <span class="text-gray-400 text-xs">No img</span>
    </div>
    <div class="flex flex-col min-w-0">
      <span class="text-sm font-medium truncate">{{ product.name }}</span>
      <span v-if="product.categoryName" class="text-xs text-gray-400">{{ product.categoryName }}</span>
    </div>
  </li>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  product: { type: Object, required: true },
  isActive: { type: Boolean, default: false }
})
defineEmits(['select'])

const imageFullUrl = computed(() => {
  if (!props.product.imageUrl) return ''
  if (props.product.imageUrl.startsWith('http')) return props.product.imageUrl
  return `http://localhost:8080/api${props.product.imageUrl}`
})
</script>
