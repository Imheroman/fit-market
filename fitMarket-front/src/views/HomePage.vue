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
          v-for="category in categories"
          :key="category.name"
          class="whitespace-nowrap px-4 py-2 border border-green-200 hover:bg-green-50 hover:border-green-400 hover:text-green-700 bg-transparent rounded-lg transition-colors"
        >
          {{ category.name }}
          <span class="ml-2 px-2 py-0.5 bg-green-100 text-green-700 text-xs rounded-full">
            {{ category.count }}
          </span>
        </button>
      </div>
    </section>

    <!-- Filters -->
    <section class="container mx-auto px-4 py-4">
      <div class="border border-green-100 bg-gradient-to-r from-green-50 to-white rounded-xl p-4">
        <div class="flex flex-wrap items-center gap-4">
          <span class="text-sm font-medium">영양 기반 검색:</span>
          <button class="px-4 py-2 border border-green-200 hover:bg-green-50 bg-transparent rounded-lg text-sm flex items-center gap-2 transition-colors">
            <Flame class="w-4 h-4 text-orange-500" />
            칼로리 300-500
          </button>
          <button class="px-4 py-2 border border-green-200 hover:bg-green-50 bg-transparent rounded-lg text-sm transition-colors">
            단백질 20g 이상
          </button>
          <button class="px-4 py-2 border border-green-200 hover:bg-green-50 bg-transparent rounded-lg text-sm transition-colors">
            탄수화물 50g 이하
          </button>
          <button class="text-green-600 hover:text-green-700 hover:bg-green-50 px-4 py-2 rounded-lg text-sm ml-auto transition-colors">
            필터 초기화
          </button>
        </div>
      </div>
    </section>

    <!-- Products Grid -->
    <section class="container mx-auto px-4 py-8">
      <div class="flex items-center justify-between mb-6">
        <h2 class="text-2xl font-bold">인기 상품</h2>
        <select class="px-4 py-2 rounded-lg border border-green-200 bg-white text-sm focus:outline-none focus:ring-2 focus:ring-green-500">
          <option>최신순</option>
          <option>인기순</option>
          <option>낮은 가격순</option>
          <option>높은 가격순</option>
          <option>칼로리 낮은순</option>
        </select>
      </div>

      <div v-if="isLoading" class="text-center text-gray-500 py-12">상품을 불러오는 중이에요...</div>
      <div v-else-if="errorMessage" class="text-center text-red-600 py-12">{{ errorMessage }}</div>
      <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        <ProductCard
          v-for="product in products"
          :key="product.id"
          :product="product"
          @toggle-favorite="toggleFavorite"
          @add-to-cart="handleAddToCart"
        />
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
import { ref } from 'vue';
import { Search, Flame, Leaf, ShoppingCart, ArrowRight } from 'lucide-vue-next';
import AppHeader from '@/components/AppHeader.vue';
import AppFooter from '@/components/AppFooter.vue';
import ProductCard from '@/components/ProductCard.vue';
import { useProducts } from '@/composables/useProducts';
import { useCart } from '@/composables/useCart';

const { products, isLoading, errorMessage, toggleFavorite, loadProducts } = useProducts();
const { addToCart } = useCart();

const searchQuery = ref('');

const categories = [
  { name: '도시락', count: 45 },
  { name: '밀키트', count: 32 },
  { name: '샐러드', count: 28 },
  { name: '단백질 보충식', count: 24 },
  { name: '스무디', count: 18 },
]

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
    window.alert(error?.message ?? '장바구니에 담지 못했어요. 다시 시도해 주세요.');
  }
};

const handleSearch = async () => {
  const keyword = searchQuery.value.trim();
  if (!keyword) {
    await loadProducts();
    return;
  }
  await loadProducts({ keyword });
};
</script>
