<template>
  <header class="sticky top-0 z-50 bg-white/95 backdrop-blur-sm border-b border-green-100">
    <div class="container mx-auto px-4">
      <div class="flex items-center justify-between h-16">
        <a href="/" class="flex items-center gap-2">
          <div class="flex items-center justify-center w-10 h-10 bg-gradient-to-br from-green-500 to-green-600 rounded-xl">
            <Leaf class="w-6 h-6 text-white" />
          </div>
          <span class="text-2xl font-bold bg-gradient-to-r from-green-600 to-green-800 bg-clip-text text-transparent">
            초록초록
          </span>
        </a>

        <nav class="hidden md:flex items-center gap-8">
          <a href="/" class="text-sm font-medium hover:text-green-600 transition-colors">전체상품</a>
          <a href="#" class="text-sm font-medium text-gray-600 hover:text-green-600 transition-colors">베스트</a>
          <a href="#" class="text-sm font-medium text-gray-600 hover:text-green-600 transition-colors">신상품</a>
          <a
            v-if="!isSeller && isAuthenticated"
            href="/seller/apply"
            class="text-sm font-medium text-gray-600 hover:text-green-600 transition-colors"
          >
            판매자 신청
          </a>
          <a
            v-if="isSeller"
            href="/seller/products"
            class="text-sm font-medium text-green-600 hover:text-green-700 transition-colors font-semibold"
          >
            상품 관리
          </a>
          <a
            v-if="isAdmin"
            href="/admin/seller-applications"
            class="text-sm font-medium text-purple-600 hover:text-purple-700 transition-colors font-semibold"
          >
            판매자 관리
          </a>
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
import { computed } from 'vue';
import { Leaf } from 'lucide-vue-next';
import { useRouter } from 'vue-router';
import { useCart } from '@/composables/useCart';
import { useAuth } from '@/composables/useAuth';
import AppHeaderLoggedInActions from '@/components/header/AppHeaderLoggedInActions.vue';
import AppHeaderLoggedOutActions from '@/components/header/AppHeaderLoggedOutActions.vue';

const router = useRouter();
const { cartCount: cartItemCount } = useCart();
const { isAuthenticated, userName, isSeller, isAdmin, cartCount: authCartCount, login, logout } = useAuth();

const headerCartCount = computed(() => {
  if (isAuthenticated.value) {
    return Number.isFinite(authCartCount.value) ? Math.max(0, authCartCount.value) : cartItemCount.value;
  }
  return 0;
});

const handleLogin = () => {
  router.push({ name: 'login' });
};

const handleLogout = () => {
  logout();
};
</script>
