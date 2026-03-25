<template>
  <div class="relative w-full">
    <div class="relative">
      <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
      <input
        ref="inputRef"
        :value="query"
        type="text"
        placeholder="상품 검색..."
        class="w-full pl-10 pr-10 py-2 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent text-sm"
        @input="onInput"
        @compositionstart="onCompositionStart"
        @compositionend="onCompositionEnd"
        @keydown.down.prevent="moveDown"
        @keydown.up.prevent="moveUp"
        @keydown.enter.prevent="handleEnter"
        @keydown.escape="close"
        @blur="handleBlur"
      />
      <Loader2
        v-if="isLoading"
        class="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 animate-spin text-gray-400"
      />
    </div>

    <SearchSuggestionList
      v-if="isOpen"
      :products="products"
      :active-index="activeIndex"
      @select="onSelect"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Search, Loader2 } from 'lucide-vue-next'
import { useRouter } from 'vue-router'
import { useSearchAutocomplete } from '../composables/useSearchAutocomplete'
import SearchSuggestionList from './SearchSuggestionList.vue'

const emit = defineEmits(['search'])
const router = useRouter()
const { query, products, isOpen, isLoading, activeIndex, triggerAutocomplete, moveUp, moveDown, selectCurrent, close } =
  useSearchAutocomplete()

const isComposing = ref(false)

function onCompositionStart() {
  isComposing.value = true
}

function onCompositionEnd(e) {
  isComposing.value = false
  query.value = e.target.value
  triggerAutocomplete(e.target.value)
}

function onInput(e) {
  query.value = e.target.value
  triggerAutocomplete(e.target.value)
}

function onSelect(product) {
  if (!product) return
  close()
  router.push(`/product/${product.id}`)
}

function handleEnter() {
  const selected = selectCurrent()
  if (selected) {
    onSelect(selected)
  } else if (query.value.trim()) {
    close()
    emit('search', query.value.trim())
  }
}

let blurTimer = null
function handleBlur() {
  blurTimer = setTimeout(() => close(), 150)
}
</script>
