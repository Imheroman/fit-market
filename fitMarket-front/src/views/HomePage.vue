<template>
  <div class="min-h-screen bg-gradient-to-b from-green-50 to-white">
    <!-- Header -->
    <AppHeader />

    <!-- Hero Section -->
    <section class="relative overflow-hidden bg-gradient-to-br from-green-500 via-green-600 to-green-700 text-white">
      <div class="absolute inset-0 bg-[url('/fresh-vegetables-pattern.png')] opacity-10" />
      <div class="relative container mx-auto px-4 py-16 md:py-24">
        <div class="max-w-2xl">
          <span class="inline-block mb-4 px-3 py-1 text-sm font-medium bg-white/20 text-white border border-white/30 rounded-full backdrop-blur-sm">
            신선한 건강식 배송
          </span>
          <h1 class="text-4xl md:text-5xl lg:text-6xl font-bold mb-6 text-balance leading-tight">
            초록초록한 건강,<br />매일 맛있게
          </h1>
          <p class="text-lg md:text-xl text-green-50 mb-8 text-pretty leading-relaxed">
            영양 성분을 한눈에 확인하고, 나에게 맞는 건강식을 찾아보세요.<br />
            표준 식품 DB로 정확한 영양 정보를 제공합니다.
          </p>
          <div class="flex flex-col sm:flex-row gap-4">
            <div class="relative flex-1 max-w-md">
              <Search class="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input
                v-model="searchQuery"
                type="text"
                placeholder="칼로리, 단백질, 상품명으로 검색..."
                class="w-full pl-12 h-12 bg-white text-gray-900 rounded-lg border-0 shadow-lg focus:outline-none focus:ring-2 focus:ring-green-500"
                @keyup.enter="handleSearch"
              />
            </div>
            <button
              class="h-12 px-6 bg-white text-green-600 hover:bg-green-50 rounded-lg font-medium flex items-center justify-center gap-2 transition-colors"
              @click="handleSearch"
            >
              검색하기
              <ArrowRight class="w-5 h-5" />
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- Categories -->
    <section class="container mx-auto px-4 py-8">
      <div class="flex gap-3 overflow-x-auto pb-2 scrollbar-hide">
        <button
          v-for="category in displayCategories"
          :key="category.id"
          @click="filterByCategory(category.id)"
          :class="[
            'whitespace-nowrap px-4 py-2 border rounded-lg transition-colors',
            selectedCategoryId === category.id
              ? 'bg-green-600 border-green-600 text-white'
              : 'border-green-200 hover:bg-green-50 hover:border-green-400 hover:text-green-700 bg-transparent'
          ]"
        >
          {{ category.name }}
          <span :class="[
            'ml-2 px-2 py-0.5 text-xs rounded-full',
            selectedCategoryId === category.id
              ? 'bg-green-500 text-white'
              : 'bg-green-100 text-green-700'
          ]">
            {{ category.count }}
          </span>
        </button>
      </div>
    </section>

    <!-- Filters & Sort -->
    <section class="container mx-auto px-4 py-4">
      <div class="grid grid-cols-1 lg:grid-cols-[70%,30%] gap-3">
        <!-- 왼쪽: 영양 기반 검색 (70%) -->
        <div class="border border-green-100 bg-gradient-to-r from-green-50 to-white rounded-xl overflow-hidden">
          <button
            @click="isFilterOpen = !isFilterOpen"
            class="w-full px-6 py-4 flex items-center justify-between hover:bg-green-50/50 transition-colors"
          >
            <div class="flex items-center gap-2">
              <Flame class="w-5 h-5 text-orange-500" />
              <h3 class="text-base font-semibold">영양 기반 검색</h3>
              <span v-if="hasActiveFilters" class="px-2 py-0.5 bg-green-500 text-white text-xs rounded-full">
                {{ activeFilterCount }}
              </span>
            </div>
            <component
              :is="isFilterOpen ? ChevronUp : ChevronDown"
              class="w-5 h-5 text-gray-600 transition-transform"
            />
          </button>

          <!-- 필터 내용 (토글) -->
          <div v-show="isFilterOpen" class="px-6 pb-6 space-y-4">
            <div class="grid md:grid-cols-2 gap-4">
              <!-- 칼로리 필터 -->
              <div class="space-y-2">
                <div class="flex items-center justify-between">
                  <label class="text-xs font-medium text-gray-700">칼로리 (kcal)</label>
                  <span class="text-xs text-green-600 font-semibold">
                    {{ nutritionFilters.calories.min }}-{{ nutritionFilters.calories.max }}
                  </span>
                </div>
                <div class="flex gap-2">
                  <input
                    v-model.number="nutritionFilters.calories.min"
                    type="number"
                    placeholder="최소"
                    class="w-20 px-2 py-1.5 border border-green-200 rounded text-xs focus:outline-none focus:ring-1 focus:ring-green-500"
                  />
                  <div class="flex-1 flex items-center">
                    <div class="w-full h-0.5 bg-green-200 rounded-full"></div>
                  </div>
                  <input
                    v-model.number="nutritionFilters.calories.max"
                    type="number"
                    placeholder="최대"
                    class="w-20 px-2 py-1.5 border border-green-200 rounded text-xs focus:outline-none focus:ring-1 focus:ring-green-500"
                  />
                </div>
              </div>

              <!-- 단백질 필터 -->
              <div class="space-y-2">
                <div class="flex items-center justify-between">
                  <label class="text-xs font-medium text-gray-700">단백질 (g)</label>
                  <span class="text-xs text-green-600 font-semibold">
                    {{ nutritionFilters.protein.min }}-{{ nutritionFilters.protein.max }}
                  </span>
                </div>
                <div class="flex gap-2">
                  <input
                    v-model.number="nutritionFilters.protein.min"
                    type="number"
                    placeholder="최소"
                    class="w-20 px-2 py-1.5 border border-green-200 rounded text-xs focus:outline-none focus:ring-1 focus:ring-green-500"
                  />
                  <div class="flex-1 flex items-center">
                    <div class="w-full h-0.5 bg-green-200 rounded-full"></div>
                  </div>
                  <input
                    v-model.number="nutritionFilters.protein.max"
                    type="number"
                    placeholder="최대"
                    class="w-20 px-2 py-1.5 border border-green-200 rounded text-xs focus:outline-none focus:ring-1 focus:ring-green-500"
                  />
                </div>
              </div>

              <!-- 탄수화물 필터 -->
              <div class="space-y-2">
                <div class="flex items-center justify-between">
                  <label class="text-xs font-medium text-gray-700">탄수화물 (g)</label>
                  <span class="text-xs text-green-600 font-semibold">
                    {{ nutritionFilters.carbs.min }}-{{ nutritionFilters.carbs.max }}
                  </span>
                </div>
                <div class="flex gap-2">
                  <input
                    v-model.number="nutritionFilters.carbs.min"
                    type="number"
                    placeholder="최소"
                    class="w-20 px-2 py-1.5 border border-green-200 rounded text-xs focus:outline-none focus:ring-1 focus:ring-green-500"
                  />
                  <div class="flex-1 flex items-center">
                    <div class="w-full h-0.5 bg-green-200 rounded-full"></div>
                  </div>
                  <input
                    v-model.number="nutritionFilters.carbs.max"
                    type="number"
                    placeholder="최대"
                    class="w-20 px-2 py-1.5 border border-green-200 rounded text-xs focus:outline-none focus:ring-1 focus:ring-green-500"
                  />
                </div>
              </div>

              <!-- 지방 필터 -->
              <div class="space-y-2">
                <div class="flex items-center justify-between">
                  <label class="text-xs font-medium text-gray-700">지방 (g)</label>
                  <span class="text-xs text-green-600 font-semibold">
                    {{ nutritionFilters.fat.min }}-{{ nutritionFilters.fat.max }}
                  </span>
                </div>
                <div class="flex gap-2">
                  <input
                    v-model.number="nutritionFilters.fat.min"
                    type="number"
                    placeholder="최소"
                    class="w-20 px-2 py-1.5 border border-green-200 rounded text-xs focus:outline-none focus:ring-1 focus:ring-green-500"
                  />
                  <div class="flex-1 flex items-center">
                    <div class="w-full h-0.5 bg-green-200 rounded-full"></div>
                  </div>
                  <input
                    v-model.number="nutritionFilters.fat.max"
                    type="number"
                    placeholder="최대"
                    class="w-20 px-2 py-1.5 border border-green-200 rounded text-xs focus:outline-none focus:ring-1 focus:ring-green-500"
                  />
                </div>
              </div>
            </div>

            <!-- 초기화 버튼 -->
            <div class="flex justify-end pt-2">
              <button
                @click="resetFilters"
                class="text-green-600 hover:text-green-700 hover:bg-green-50 px-3 py-1.5 rounded-lg text-xs font-medium transition-colors"
              >
                초기화
              </button>
            </div>
          </div>
        </div>

        <!-- 오른쪽: 정렬 (30%, 가로 스크롤) -->
        <div class="border border-green-100 bg-white rounded-xl px-3 py-3">
          <div class="text-xs font-medium text-gray-500 mb-2">정렬</div>
          <div class="flex gap-1.5 overflow-x-auto pb-1 scrollbar-thin scrollbar-thumb-green-200 scrollbar-track-transparent">
            <button
              v-for="sort in sortOptions"
              :key="sort.key"
              @click="toggleSort(sort.key)"
              :class="[
                'px-2.5 py-1.5 rounded text-xs font-medium transition-all flex items-center gap-1 whitespace-nowrap flex-shrink-0',
                currentSort === sort.key
                  ? 'bg-green-600 text-white'
                  : 'bg-gray-50 text-gray-600 hover:bg-green-50 hover:text-green-700'
              ]"
            >
              <span>{{ sort.label }}</span>
              <component
                :is="getSortIcon(sort.key)"
                class="w-3 h-3"
              />
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- Products Grid -->
    <section class="container mx-auto px-4 py-8">
      <div class="flex items-center justify-between mb-6">
        <h2 class="text-2xl font-bold">
          {{ hasActiveFilters ? '필터링된 상품' : '인기 상품' }}
          <span class="text-lg text-gray-500 ml-2">({{ displayTotalCount }})</span>
        </h2>
      </div>

      <div v-if="isLoading" class="text-center text-gray-500 py-12">상품을 불러오는 중이에요...</div>
      <div v-else-if="errorMessage" class="text-center text-red-600 py-12">{{ errorMessage }}</div>
      <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        <ProductCard
          v-for="product in sortedProducts"
          :key="product.id"
          :product="product"
          @toggle-favorite="toggleFavorite"
          @add-to-cart="handleAddToCart"
        />
      </div>
      <div
        v-if="!isLoading && !errorMessage && pageCount > 1"
        class="mt-10 flex items-center justify-between"
      >
        <button
          class="px-3 py-1.5 rounded-lg border text-xs font-semibold transition-colors"
          :class="currentPage === 1 ? 'border-gray-200 text-gray-300' : 'border-gray-200 text-gray-600 hover:border-green-200 hover:text-green-700'"
          :disabled="currentPage === 1"
          @click="goToPage(currentPage - 1)"
        >
          이전
        </button>
        <div class="flex items-center gap-2">
          <button
            v-for="page in pageNumbers"
            :key="page"
            class="w-9 h-9 rounded-lg text-sm font-semibold border transition-colors"
            :class="
              currentPage === page
                ? 'bg-green-600 border-green-600 text-white'
                : 'border-gray-200 text-gray-600 hover:border-green-200'
            "
            @click="goToPage(page)"
          >
            {{ page }}
          </button>
        </div>
        <button
          class="px-3 py-1.5 rounded-lg border text-xs font-semibold transition-colors"
          :class="currentPage === pageCount ? 'border-gray-200 text-gray-300' : 'border-gray-200 text-gray-600 hover:border-green-200 hover:text-green-700'"
          :disabled="currentPage === pageCount"
          @click="goToPage(currentPage + 1)"
        >
          다음
        </button>
      </div>
    </section>

    <!-- Features Section -->
    <section class="container mx-auto px-4 py-16">
      <div class="grid md:grid-cols-3 gap-8">
        <div class="border border-green-100 bg-gradient-to-br from-green-50 to-white rounded-xl p-6 text-center">
          <div class="w-14 h-14 bg-gradient-to-br from-green-500 to-green-600 rounded-2xl flex items-center justify-center mx-auto mb-4">
            <Leaf class="w-7 h-7 text-white" />
          </div>
          <h3 class="text-lg font-semibold mb-2">표준 식품 DB</h3>
          <p class="text-sm text-gray-600 leading-relaxed">
            식약처 식품영양성분 DB 기반으로 정확한 영양 정보를 제공합니다
          </p>
        </div>

        <div class="border border-green-100 bg-gradient-to-br from-green-50 to-white rounded-xl p-6 text-center">
          <div class="w-14 h-14 bg-gradient-to-br from-green-500 to-green-600 rounded-2xl flex items-center justify-center mx-auto mb-4">
            <Flame class="w-7 h-7 text-white" />
          </div>
          <h3 class="text-lg font-semibold mb-2">영양 기반 검색</h3>
          <p class="text-sm text-gray-600 leading-relaxed">
            칼로리, 단백질, 탄수화물 등 영양소로 상품을 검색할 수 있습니다
          </p>
        </div>

        <div class="border border-green-100 bg-gradient-to-br from-green-50 to-white rounded-xl p-6 text-center">
          <div class="w-14 h-14 bg-gradient-to-br from-green-500 to-green-600 rounded-2xl flex items-center justify-center mx-auto mb-4">
            <ShoppingCart class="w-7 h-7 text-white" />
          </div>
          <h3 class="text-lg font-semibold mb-2">간편한 주문</h3>
          <p class="text-sm text-gray-600 leading-relaxed">
            장바구니에서 총 영양소를 자동 계산하고 목표와 비교할 수 있습니다
          </p>
        </div>
      </div>
    </section>

    <!-- Footer -->
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { Search, Flame, Leaf, ShoppingCart, ArrowRight, ArrowUp, ArrowDown, ChevronUp, ChevronDown } from 'lucide-vue-next';
import AppHeader from '@/components/AppHeader.vue';
import AppFooter from '@/components/AppFooter.vue';
import ProductCard from '@/components/ProductCard.vue';
import { useProducts } from '@/composables/useProducts';
import { useCart } from '@/composables/useCart';
import { shouldShowErrorAlert } from '@/utils/httpError';
import { fetchCategories } from '@/api/categoriesApi';

const { products, isLoading, errorMessage, toggleFavorite, loadProducts, pagination } = useProducts();
const { addToCart } = useCart();

const searchQuery = ref('');
const activeKeyword = ref('');
const isFilterOpen = ref(false);
const categories = ref([]);
const selectedCategoryId = ref(null);
const currentPage = ref(1);
const pageSize = ref(20);

// Load categories from API
const loadCategories = async () => {
  try {
    const data = await fetchCategories();
    categories.value = data.map(cat => ({
      id: cat.id,
      name: cat.name,
      count: cat.productCount || 0
    }));
  } catch (error) {
    console.error('카테고리 로딩 실패:', error);
  }
};

// Filter products by category
const loadWithFilters = async (page = 1) => {
  const keyword = activeKeyword.value.trim();
  const categoryId = selectedCategoryId.value ?? undefined;
  await loadProducts({ page, size: pageSize.value, categoryId, keyword: keyword || undefined });
  currentPage.value = page;
};

const filterByCategory = async (categoryId) => {
  if (selectedCategoryId.value === categoryId) {
    // 같은 카테고리를 다시 클릭하면 필터 해제
    selectedCategoryId.value = null;
  } else {
    selectedCategoryId.value = categoryId;
  }
  await loadWithFilters(1);
};

onMounted(() => {
  loadCategories();
  loadWithFilters(1);
});

// Nutrition filters
const nutritionFilters = ref({
  calories: { min: 0, max: 2000 },
  protein: { min: 0, max: 200 },
  carbs: { min: 0, max: 200 },
  fat: { min: 0, max: 100 },
})

const resetFilters = () => {
  nutritionFilters.value = {
    calories: { min: 0, max: 2000 },
    protein: { min: 0, max: 200 },
    carbs: { min: 0, max: 200 },
    fat: { min: 0, max: 100 },
  }
}

const hasActiveFilters = computed(() => {
  return (
    nutritionFilters.value.calories.min > 0 || nutritionFilters.value.calories.max < 2000 ||
    nutritionFilters.value.protein.min > 0 || nutritionFilters.value.protein.max < 200 ||
    nutritionFilters.value.carbs.min > 0 || nutritionFilters.value.carbs.max < 200 ||
    nutritionFilters.value.fat.min > 0 || nutritionFilters.value.fat.max < 100
  )
})

const activeFilterCount = computed(() => {
  let count = 0
  if (nutritionFilters.value.calories.min > 0 || nutritionFilters.value.calories.max < 2000) count++
  if (nutritionFilters.value.protein.min > 0 || nutritionFilters.value.protein.max < 200) count++
  if (nutritionFilters.value.carbs.min > 0 || nutritionFilters.value.carbs.max < 200) count++
  if (nutritionFilters.value.fat.min > 0 || nutritionFilters.value.fat.max < 100) count++
  return count
})

// Sort options
const sortOptions = [
  { key: 'date', label: '최신순' },
  { key: 'popularity', label: '인기순' },
  { key: 'price', label: '가격' },
  { key: 'calories', label: '칼로리' },
  { key: 'carbs', label: '탄수화물' },
  { key: 'protein', label: '단백질' },
  { key: 'fat', label: '지방' },
]

const currentSort = ref('date')
const sortDirections = ref({
  date: 'desc',      // 최신순 (desc) ↔ 오래된순 (asc)
  popularity: 'desc', // 인기순 (desc) ↔ 비인기순 (asc)
  price: 'asc',      // 낮은 (asc) ↔ 높은 (desc)
  calories: 'asc',   // 낮은 (asc) ↔ 높은 (desc)
  carbs: 'asc',      // 낮은 (asc) ↔ 높은 (desc)
  protein: 'asc',    // 낮은 (asc) ↔ 높은 (desc)
  fat: 'asc',        // 낮은 (asc) ↔ 높은 (desc)
})

const toggleSort = (key) => {
  if (currentSort.value === key) {
    // 같은 정렬을 다시 클릭하면 방향 토글
    sortDirections.value[key] = sortDirections.value[key] === 'asc' ? 'desc' : 'asc'
  } else {
    // 다른 정렬을 클릭하면 해당 정렬로 변경
    currentSort.value = key
  }
}

const getSortIcon = (key) => {
  if (currentSort.value !== key) return ArrowDown
  return sortDirections.value[key] === 'asc' ? ArrowUp : ArrowDown
}

const filteredProducts = computed(() => {
  return products.value.filter((product) => {
    const calories = product.calories || 0
    const protein = product.protein || 0
    const carbs = product.carbs || 0
    const fat = product.fat || 0

    return (
      calories >= nutritionFilters.value.calories.min &&
      calories <= nutritionFilters.value.calories.max &&
      protein >= nutritionFilters.value.protein.min &&
      protein <= nutritionFilters.value.protein.max &&
      carbs >= nutritionFilters.value.carbs.min &&
      carbs <= nutritionFilters.value.carbs.max &&
      fat >= nutritionFilters.value.fat.min &&
      fat <= nutritionFilters.value.fat.max
    )
  })
})

const sortedProducts = computed(() => {
  const filtered = filteredProducts.value

  // 2. 정렬
  const sorted = [...filtered]
  const direction = sortDirections.value[currentSort.value]
  const multiplier = direction === 'asc' ? 1 : -1

  sorted.sort((a, b) => {
    let aVal, bVal

    switch (currentSort.value) {
      case 'date':
        // id가 클수록 최신 (가정)
        aVal = a.id
        bVal = b.id
        break
      case 'popularity':
        // rating과 reviews를 조합하여 인기도 계산
        aVal = (a.rating || 0) * 100 + (a.reviews || 0)
        bVal = (b.rating || 0) * 100 + (b.reviews || 0)
        break
      case 'price':
        aVal = a.price || 0
        bVal = b.price || 0
        break
      case 'calories':
        aVal = a.calories || 0
        bVal = b.calories || 0
        break
      case 'carbs':
        aVal = a.carbs || 0
        bVal = b.carbs || 0
        break
      case 'protein':
        aVal = a.protein || 0
        bVal = b.protein || 0
        break
      case 'fat':
        aVal = a.fat || 0
        bVal = b.fat || 0
        break
      default:
        return 0
    }

    return (aVal - bVal) * multiplier
  })

  return sorted
})

const categoryCountMap = computed(() => {
  const counts = new Map()
  filteredProducts.value.forEach((product) => {
    if (product.categoryId == null) return
    counts.set(product.categoryId, (counts.get(product.categoryId) ?? 0) + 1)
  })
  return counts
})

const isScopedCount = computed(() => activeKeyword.value.trim().length > 0 || hasActiveFilters.value)

const displayCategories = computed(() =>
  categories.value.map((category) => ({
    ...category,
    count: isScopedCount.value ? (categoryCountMap.value.get(category.id) ?? 0) : category.count,
  }))
)

const displayTotalCount = computed(() => {
  if (hasActiveFilters.value) {
    return filteredProducts.value.length
  }
  return pagination.value.totalElements || filteredProducts.value.length
})

const pageCount = computed(() => Math.max(pagination.value.totalPages || 1, 1))

const pageNumbers = computed(() => {
  const total = pageCount.value
  const current = currentPage.value
  const maxButtons = 10
  const half = Math.floor(maxButtons / 2)
  let start = Math.max(1, current - half)
  let end = Math.min(total, start + maxButtons - 1)
  if (end - start + 1 < maxButtons) {
    start = Math.max(1, end - maxButtons + 1)
  }
  return Array.from({ length: end - start + 1 }, (_, index) => start + index)
})

const handleAddToCart = async (productId) => {
  const product = products.value.find((p) => p.id === productId);
  if (!product) return;

  const priceNumber = typeof product.price === 'number'
    ? product.price
    : parseInt(String(product.price).replace(/[^0-9]/g, ''), 10);

  try {
    await addToCart({
      id: product.id,
      name: product.name,
      category: product.category,
      price: priceNumber,
      image: product.image,
      calories: product.calories,
      protein: product.protein,
      carbs: product.carbs,
      fat: product.fat,
    });
    window.alert('장바구니에 담겼어요! 결제 전에 언제든 수정할 수 있어요.');
  } catch (error) {
    if (!shouldShowErrorAlert(error)) return;
    window.alert(error?.message ?? '장바구니에 담지 못했어요. 다시 시도해 주세요.');
  }
};

const goToPage = async (page) => {
  const total = pageCount.value
  const nextPage = Math.min(Math.max(page, 1), total)
  if (nextPage === currentPage.value) return
  await loadWithFilters(nextPage)
}

const handleSearch = async () => {
  const keyword = searchQuery.value.trim();
  activeKeyword.value = keyword;
  await loadWithFilters(1);
};
</script>
