<template>
  <header class="sticky top-0 z-50 bg-white/95 backdrop-blur-sm border-b border-green-100">
    <div class="container mx-auto px-4">
      <div class="flex items-center justify-between h-16">
        <RouterLink to="/" class="flex items-center gap-2">
          <div class="flex items-center justify-center w-10 h-10 bg-gradient-to-br from-green-500 to-green-600 rounded-xl">
            <Leaf class="w-6 h-6 text-white" />
          </div>
          <span class="text-2xl font-bold bg-gradient-to-r from-green-600 to-green-800 bg-clip-text text-transparent">
            초록초록
          </span>
        </RouterLink>

        <nav class="hidden md:flex items-center gap-8">
          <RouterLink to="/" class="text-sm font-medium hover:text-green-600 transition-colors">전체상품</RouterLink>
          <RouterLink to="/products/best" class="text-sm font-medium text-gray-600 hover:text-green-600 transition-colors">베스트</RouterLink>
          <RouterLink to="/products/new" class="text-sm font-medium text-gray-600 hover:text-green-600 transition-colors">신상품</RouterLink>
          <RouterLink
            v-if="!isSeller && isAuthenticated"
            to="/seller/apply"
            class="text-sm font-medium text-gray-600 hover:text-green-600 transition-colors"
          >
            판매자 신청
          </RouterLink>
          <RouterLink
            v-if="isSeller"
            to="/seller/products"
            class="text-sm font-medium text-green-600 hover:text-green-700 transition-colors font-semibold"
          >
            상품 관리
          </RouterLink>
          <RouterLink
            v-if="isAdmin"
            to="/admin/seller-applications"
            class="text-sm font-medium text-purple-600 hover:text-purple-700 transition-colors font-semibold"
          >
            판매자 관리
          </RouterLink>
        </nav>

        <div class="flex items-center gap-3">
          <AppHeaderLoggedInActions
            v-if="isAuthenticated"
            :cart-count="headerCartCount"
            :user-name="userName"
            @logout="handleLogout"
          />
          <AppHeaderLoggedOutActions v-else @login="handleLogin" />
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed, onMounted, watch } from 'vue';
import { Leaf } from 'lucide-vue-next';
import { useRouter } from 'vue-router';
import { useCart } from '@/composables/useCart';
import { useAuth } from '@/composables/useAuth';
import AppHeaderLoggedInActions from '@/components/header/AppHeaderLoggedInActions.vue';
import AppHeaderLoggedOutActions from '@/components/header/AppHeaderLoggedOutActions.vue';

const router = useRouter();
const { cartCount: cartItemCount, isInitialized: isCartInitialized, loadCart, resetCart } = useCart();
const { isAuthenticated, userName, isSeller, isAdmin, cartCount: authCartCount, logout } = useAuth();

const headerCartCount = computed(() => {
  if (isAuthenticated.value) {
    if (isCartInitialized.value) {
      return cartItemCount.value;
    }
    if (Number.isFinite(authCartCount.value)) {
      return Math.max(0, authCartCount.value);
    }
    return cartItemCount.value;
  }
  return 0;
});

const handleLogin = () => {
  router.push({ name: 'login' });
};

const handleLogout = () => {
  logout();
  resetCart();
};

const tryLoadCart = (force = false) => {
  if (!isAuthenticated.value) return;
  loadCart({ force }).catch((error) => {
    console.error('장바구니를 불러오지 못했어요.', error);
  });
};

onMounted(() => {
  tryLoadCart();
});

watch(
  () => isAuthenticated.value,
  (next, prev) => {
    if (next && !prev) {
      tryLoadCart(true);
    }
    if (!next) {
      resetCart();
    }
  }
);
</script>
