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
            <span>총 {{ sortedProducts.length }}개</span>
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
    </main>

    <AppFooter />
  </div>
</template>

<script setup>
import { Star } from 'lucide-vue-next';
import AppHeader from '@/components/AppHeader.vue';
import AppFooter from '@/components/AppFooter.vue';
import ProductCard from '@/components/ProductCard.vue';
import { useCart } from '@/composables/useCart';
import { useBestProducts } from '@/composables/useBestProducts';

const { products, isLoading, errorMessage, toggleFavorite } = useBestProducts();
const { addToCart } = useCart();

const sortedProducts = products;

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
    window.alert(error?.message ?? '장바구니에 담지 못했어요. 다시 시도해 주세요.');
  }
};
</script>
