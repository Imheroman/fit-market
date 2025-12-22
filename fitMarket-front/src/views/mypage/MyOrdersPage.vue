<template>
  <section class="space-y-6">
    <MyOrdersSection
      :filter-options="filterOptions"
      :selected-range="selectedRange"
      :filter-description="filterDescription"
      :orders="filteredOrders"
      :is-loading="isLoading"
      :error-message="errorMessage"
      :deleting-order-numbers="deletingOrderNumbers"
      @change-filter="setFilter"
      @select-order="handleSelectOrder"
      @delete-order="handleDeleteOrder"
    />
  </section>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import MyOrdersSection from '@/components/mypage/MyOrdersSection.vue';
import { useOrderHistory } from '@/composables/useOrderHistory';
import { deleteOrder } from '@/api/ordersApi';

const router = useRouter();
const {
  filterOptions,
  selectedRange,
  filteredOrders,
  filterDescription,
  setFilter,
  isLoading,
  errorMessage,
  removeOrderByNumber,
} = useOrderHistory();

const deletingOrderNumbers = ref([]);

const addOrderNumber = (listRef, orderNumber) => {
  if (!orderNumber || listRef.value.includes(orderNumber)) return;
  listRef.value = [...listRef.value, orderNumber];
};

const removeOrderNumber = (listRef, orderNumber) => {
  listRef.value = listRef.value.filter((entry) => entry !== orderNumber);
};

const handleSelectOrder = (orderNumber) => {
  if (!orderNumber) return;
  router.push({ name: 'my-page-order-detail', params: { orderNumber } });
};

const handleDeleteOrder = async (order) => {
  const orderNumber = order?.orderNumber;
  if (!orderNumber) {
    window.alert('주문번호를 찾지 못했어요. 다시 시도해 주세요.');
    return;
  }
  if (deletingOrderNumbers.value.includes(orderNumber)) return;

  const confirmed = window.confirm('주문 내역을 삭제할까요? 삭제하면 복구할 수 없어요.');
  if (!confirmed) return;

  addOrderNumber(deletingOrderNumbers, orderNumber);

  try {
    await deleteOrder(orderNumber);
    removeOrderByNumber(orderNumber);
    window.alert('주문 내역을 삭제했어요.');
  } catch (error) {
    window.alert(error?.message ?? '주문 내역을 삭제하지 못했어요. 잠시 후 다시 시도해 주세요.');
  } finally {
    removeOrderNumber(deletingOrderNumbers, orderNumber);
  }
};
</script>
