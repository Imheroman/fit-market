import { ref, onUnmounted } from 'vue'
import { fetchAutocomplete } from '../api/searchApi'

/**
 * 검색 자동완성 상태 관리 컴포저블.
 *
 * 한글 IME 대응: Vue 3의 v-model은 IME 조합 중 값을 업데이트하지 않으므로,
 * watch(query) 대신 triggerAutocomplete()를 직접 호출하는 방식으로 구현한다.
 * 디바운스 300ms, 키보드 네비게이션, 로딩/에러 상태 관리.
 */
export function useSearchAutocomplete() {
  const query = ref('')
  const products = ref([])
  const isOpen = ref(false)
  const isLoading = ref(false)
  const isError = ref(false)
  const activeIndex = ref(-1)

  let debounceTimer = null

  async function fetchSuggestions(q) {
    if (!q || q.trim().length < 1) {
      products.value = []
      isOpen.value = false
      return
    }
    isLoading.value = true
    isError.value = false
    try {
      products.value = await fetchAutocomplete(q)
      activeIndex.value = -1
      isOpen.value = products.value.length > 0
    } catch (err) {
      if (err?.code === 'ERR_CANCELED') return
      console.error('Autocomplete fetch failed:', err)
      isError.value = true
      products.value = []
      isOpen.value = false
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 자동완성을 트리거한다.
   * SearchBar의 @input / @compositionend 이벤트에서 직접 호출한다.
   */
  function triggerAutocomplete(value) {
    clearTimeout(debounceTimer)
    activeIndex.value = -1
    if (!value || value.trim().length < 1) {
      products.value = []
      isOpen.value = false
      return
    }
    debounceTimer = setTimeout(() => fetchSuggestions(value), 300)
  }

  function moveDown() {
    if (products.value.length === 0) return
    activeIndex.value = (activeIndex.value + 1) % products.value.length
  }

  function moveUp() {
    if (products.value.length === 0) return
    activeIndex.value = activeIndex.value <= 0
      ? products.value.length - 1
      : activeIndex.value - 1
  }

  function selectCurrent() {
    if (activeIndex.value >= 0 && activeIndex.value < products.value.length) {
      return products.value[activeIndex.value]
    }
    return null
  }

  function close() {
    isOpen.value = false
    activeIndex.value = -1
  }

  onUnmounted(() => clearTimeout(debounceTimer))

  return {
    query, products, isOpen, isLoading, isError,
    activeIndex, triggerAutocomplete, moveUp, moveDown, selectCurrent, close
  }
}
