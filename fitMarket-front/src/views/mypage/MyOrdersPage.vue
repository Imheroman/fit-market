<template>
  <section class="space-y-6">
    <MyOrdersSection
      :filter-options="filterOptions"
      :selected-range="selectedRange"
      :filter-description="filterDescription"
      :orders="filteredOrders"
      @change-filter="setFilter"
      @view-order="handleViewOrder"
    />
  </section>
</template>

<script setup>
import MyOrdersSection from '@/components/mypage/MyOrdersSection.vue';
import { useOrderHistory } from '@/composables/useOrderHistory';

const { filterOptions, selectedRange, filteredOrders, filterDescription, setFilter } = useOrderHistory();

const handleViewOrder = (orderNumber) => {
  window.alert(`주문번호 ${orderNumber} 상세 페이지는 준비 중이에요.`);
};
</script>
