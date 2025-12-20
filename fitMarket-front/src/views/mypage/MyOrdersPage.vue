<template>
  <section class="space-y-6">
    <MyOrdersSection
      :filter-options="filterOptions"
      :selected-range="selectedRange"
      :filter-description="filterDescription"
      :orders="filteredOrders"
      :is-loading="isLoading"
      :error-message="errorMessage"
      :refunding-order-numbers="refundingOrderNumbers"
      :deleting-order-numbers="deletingOrderNumbers"
      :refund-requested-order-numbers="refundRequestedOrderNumbers"
      @change-filter="setFilter"
      @select-order="handleSelectOrder"
      @request-refund="handleRefundRequest"
      @delete-order="handleDeleteOrder"
    />
  </section>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import MyOrdersSection from '@/components/mypage/MyOrdersSection.vue';
import { useOrderHistory } from '@/composables/useOrderHistory';
import { deleteOrder, requestOrderRefund } from '@/api/ordersApi';

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

const refundingOrderNumbers = ref([]);
const deletingOrderNumbers = ref([]);
const refundRequestedOrderNumbers = ref([]);

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

const handleRefundRequest = async (order) => {
  const orderNumber = order?.orderNumber;
  if (!orderNumber) {
    window.alert('주문번호를 찾지 못했어요. 다시 시도해 주세요.');
    return;
  }
  if (refundRequestedOrderNumbers.value.includes(orderNumber)) {
    window.alert('이미 환불 요청이 접수됐어요.');
    return;
  }
  if (refundingOrderNumbers.value.includes(orderNumber)) return;

  const confirmed = window.confirm('환불을 요청할까요?');
  if (!confirmed) return;

  addOrderNumber(refundingOrderNumbers, orderNumber);

  try {
    await requestOrderRefund(orderNumber);
    addOrderNumber(refundRequestedOrderNumbers, orderNumber);
    window.alert('환불 요청이 접수됐어요. 처리 현황은 알림으로 알려드릴게요.');
  } catch (error) {
    window.alert(error?.message ?? '환불 요청을 접수하지 못했어요. 잠시 후 다시 시도해 주세요.');
  } finally {
    removeOrderNumber(refundingOrderNumbers, orderNumber);
  }
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
