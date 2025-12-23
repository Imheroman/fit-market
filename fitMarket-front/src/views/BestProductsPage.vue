<template>
  <div class="min-h-screen bg-gradient-to-b from-amber-50 to-white">
    <AppHeader />

    <section class="bg-white/70 backdrop-blur border-b border-amber-100">
      <div class="container mx-auto px-4 py-10 flex flex-col gap-4">
        <div class="flex items-center gap-3 text-amber-700">
          <Star class="w-5 h-5" />
          <span class="text-sm font-medium">리뷰와 평점이 좋은 상품</span>
        </div>
        <div class="flex flex-col md:flex-row md:items-end md:justify-between gap-4">
          <div>
            <h1 class="text-3xl md:text-4xl font-bold text-gray-900">베스트</h1>
            <p class="text-gray-600 mt-2">평점 우선, 리뷰 수 보조로 정렬했습니다.</p>
          </div>
          <div class="flex items-center gap-3 text-sm text-gray-500">
            <span class="px-3 py-1 bg-amber-100 text-amber-700 rounded-full font-medium">BEST</span>
            <span>총 {{ totalCount }}개</span>
          </div>
        </div>
      </div>
    </section>

    <main class="container mx-auto px-4 py-10">
      <div v-if="isLoading" class="text-center text-gray-500 py-12">베스트 상품을 불러오는 중이에요...</div>
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
          :class="currentPage === 1 ? 'border-gray-200 text-gray-300' : 'border-amber-200 text-amber-700 hover:border-amber-300 hover:text-amber-800'"
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
                ? 'bg-amber-500 border-amber-500 text-white'
                : 'border-gray-200 text-gray-600 hover:border-amber-200 hover:text-amber-700'
            "
            @click="goToPage(page)"
          >
            {{ page }}
          </button>
        </div>
        <button
          class="px-3 py-1.5 rounded-lg border text-xs font-semibold transition-colors"
          :class="currentPage === pageCount ? 'border-gray-200 text-gray-300' : 'border-amber-200 text-amber-700 hover:border-amber-300 hover:text-amber-800'"
          :disabled="currentPage === pageCount"
          @click="goToPage(currentPage + 1)"
        >
          다음
        </button>
      </div>
    </main>

    <AppFooter />
  </div>
</template>

<script setup>
import { computed, ref } from 'vue';
import { Star } from 'lucide-vue-next';
import AppHeader from '@/components/AppHeader.vue';
import AppFooter from '@/components/AppFooter.vue';
import ProductCard from '@/components/ProductCard.vue';
import { useCart } from '@/composables/useCart';
import { useBestProducts } from '@/composables/useBestProducts';
import { shouldShowErrorAlert } from '@/utils/httpError';

const { products, isLoading, errorMessage, toggleFavorite, loadProducts, pagination } = useBestProducts();
const { addToCart } = useCart();

const sortedProducts = products;
const currentPage = ref(1);
const pageSize = 12;

const totalCount = computed(() => pagination.value.totalElements || sortedProducts.value.length);
const pageCount = computed(() => Math.max(pagination.value.totalPages || 1, 1));

const pageNumbers = computed(() => {
  const total = pageCount.value;
  const current = currentPage.value;
  const maxButtons = 10;
  const half = Math.floor(maxButtons / 2);
  let start = Math.max(1, current - half);
  let end = Math.min(total, start + maxButtons - 1);
  if (end - start + 1 < maxButtons) {
    start = Math.max(1, end - maxButtons + 1);
  }
  return Array.from({ length: end - start + 1 }, (_, index) => start + index);
});

const goToPage = async (page) => {
  const total = pageCount.value;
  const nextPage = Math.min(Math.max(page, 1), total);
  if (nextPage === currentPage.value) return;
  currentPage.value = nextPage;
  await loadProducts({ page: nextPage, size: pageSize });
};

const handleAddToCart = async (productId) => {
  const product = sortedProducts.value.find((p) => p.id === productId);
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
</script>
