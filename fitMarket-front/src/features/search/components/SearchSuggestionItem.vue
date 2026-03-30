<template>
  <li
    class="flex items-center gap-3 px-4 py-2 cursor-pointer hover:bg-green-50 transition-colors"
    :class="{ 'bg-green-50': isActive }"
    @mousedown.prevent="$emit('select', product)"
  >
    <img
      v-if="product.imageUrl && !imgError"
      :src="imageFullUrl"
      :alt="product.name"
      class="w-8 h-8 object-cover rounded flex-shrink-0 overflow-hidden"
      @error="imgError = true"
    />
    <div v-else class="w-8 h-8 bg-gray-100 rounded flex items-center justify-center flex-shrink-0">
      <span class="text-gray-400 text-xs">No img</span>
    </div>
    <div class="flex flex-col min-w-0 flex-1">
      <span class="text-sm font-medium text-gray-900 truncate">{{ product.name }}</span>
      <span v-if="product.categoryName" class="text-xs text-gray-400 truncate">{{ product.categoryName }}</span>
    </div>
  </li>
</template>

<script setup>
import { computed, ref } from 'vue'
import { getImageUrl } from '@/utils/image'

const props = defineProps({
  product: { type: Object, required: true },
  isActive: { type: Boolean, default: false }
})
defineEmits(['select'])

const imgError = ref(false)

const imageFullUrl = computed(() => {
  return getImageUrl(props.product.imageUrl)
})
</script>
