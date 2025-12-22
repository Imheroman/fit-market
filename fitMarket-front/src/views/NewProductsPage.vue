<template>
    <div class="min-h-screen bg-gradient-to-b from-green-50 to-white">
        <AppHeader/>

        <section class="bg-white/70 backdrop-blur border-b border-green-100">
            <div class="container mx-auto px-4 py-10 flex flex-col gap-4">
                <div class="flex items-center gap-3 text-green-700">
                    <Clock class="w-5 h-5"/>
                    <span class="text-sm font-medium">새로 올라온 건강식</span>
                </div>
                <div class="flex flex-col md:flex-row md:items-end md:justify-between gap-4">
                    <div>
                        <h1 class="text-3xl md:text-4xl font-bold text-gray-900">신상품</h1>
                        <p class="text-gray-600 mt-2">최신 등록 순으로 정렬된 상품을 한눈에 만나보세요.</p>
                    </div>
                    <div class="flex items-center gap-3 text-sm text-gray-500">
                        <span class="px-3 py-1 bg-green-100 text-green-700 rounded-full font-medium">NEW</span>
                        <span>총 {{ sortedProducts.length }}개</span>
                    </div>
                </div>
            </div>
        </section>

        <main class="container mx-auto px-4 py-10">
            <div v-if="isLoading" class="text-center text-gray-500 py-12">신상품을 불러오는 중이에요...</div>
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

        <AppFooter/>
    </div>
</template>

<script setup>
import {Clock} from 'lucide-vue-next';
import AppHeader from '@/components/AppHeader.vue';
import AppFooter from '@/components/AppFooter.vue';
import ProductCard from '@/components/ProductCard.vue';
import {useCart} from '@/composables/useCart';
import {useNewProducts} from '@/composables/useNewProducts';
import {shouldShowErrorAlert} from '@/utils/httpError';

const {products: sortedProducts, isLoading, errorMessage, toggleFavorite} = useNewProducts();
const {addToCart} = useCart();

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
