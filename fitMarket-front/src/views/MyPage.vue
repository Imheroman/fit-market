<template>
    <div class="min-h-screen bg-gray-50 flex flex-col">
        <AppHeader/>

        <main class="flex-1">
            <div class="container mx-auto px-4 py-10">
                <div class="space-y-8">
                    <header class="text-center md:text-left space-y-3">
                        <p class="text-sm font-semibold text-green-600">마이페이지</p>
                        <h1 class="text-3xl md:text-4xl font-bold text-gray-900">나의 회원 정보</h1>
                        <p class="text-gray-500">주문 기록과 배송지를 한 곳에서 관리할 수 있어요.</p>
                    </header>

                    <MyPageTabs :tabs="tabs" :active-tab="activeTab"/>

                    <router-view/>
                </div>
            </div>
        </main>

        <AppFooter/>
    </div>
</template>

<script setup>
import {computed} from 'vue';
import {useRoute} from 'vue-router';
import {UserRound, Package, MapPin} from 'lucide-vue-next';
import AppHeader from '@/components/AppHeader.vue';
import AppFooter from '@/components/AppFooter.vue';
import MyPageTabs from '@/components/mypage/MyPageTabs.vue';

const route = useRoute();

const tabs = [
    {label: '기본 정보', value: 'my-page-profile', icon: UserRound},
    {label: '주문 내역', value: 'my-page-orders', icon: Package},
    {label: '배송지 관리', value: 'my-page-addresses', icon: MapPin},
];

const activeTab = computed(() => {
    if (!route.name) return 'my-page-profile';
    if (route.name === 'my-page-orders' || route.name === 'my-page-order-detail') return 'my-page-orders';
    if (route.name === 'my-page-addresses' || route.name === 'my-page-addresses-new') return 'my-page-addresses';
    return 'my-page-profile';
});
</script>
