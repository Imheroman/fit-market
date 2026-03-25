<template>
  <ul
    class="absolute top-full left-0 w-full bg-white shadow-lg rounded-b-xl border border-t-0 z-50 max-h-80 overflow-y-auto"
  >
    <SearchSuggestionItem
      v-for="(product, index) in products"
      :key="product.id"
      :product="product"
      :is-active="index === activeIndex"
      @select="$emit('select', product)"
    />
  </ul>
</template>

<script setup>
import SearchSuggestionItem from './SearchSuggestionItem.vue'

defineProps({
  products: { type: Array, required: true },
  activeIndex: { type: Number, default: -1 }
})
defineEmits(['select'])
</script>
