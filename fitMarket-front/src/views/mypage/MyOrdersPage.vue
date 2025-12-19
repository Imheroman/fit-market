<template>
  <section class="space-y-6">
    <MyOrdersSection
      :filter-options="filterOptions"
      :selected-range="selectedRange"
      :filter-description="filterDescription"
      :orders="filteredOrders"
      :is-loading="isLoading"
      :error-message="errorMessage"
      @change-filter="setFilter"
    />
  </section>
</template>

<script setup>
import MyOrdersSection from '@/components/mypage/MyOrdersSection.vue';
import { useOrderHistory } from '@/composables/useOrderHistory';

const { filterOptions, selectedRange, filteredOrders, filterDescription, setFilter, isLoading, errorMessage } = useOrderHistory();
</script>
