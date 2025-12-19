<template>
  <section class="bg-white shadow-lg rounded-2xl p-6 md:p-8 border border-gray-100 space-y-6">
    <div class="flex flex-col gap-1">
      <h2 class="text-2xl font-semibold text-gray-900">전체 주문 목록</h2>
      <p class="text-sm text-gray-500">{{ filterDescription }}</p>
    </div>

    <div class="flex flex-wrap gap-3">
      <button
        v-for="option in filterOptions"
        :key="option.value"
        class="px-4 py-2 rounded-full text-sm font-semibold border transition-colors"
        :class="selectedRange === option.value ? 'bg-green-600 border-green-600 text-white' : 'bg-white border-gray-200 text-gray-600 hover:border-green-200'"
        @click="emit('change-filter', option.value)"
      >
        {{ option.label }}
      </button>
    </div>

    <div v-if="isLoading" class="text-sm text-gray-500">주문 내역을 불러오고 있어요.</div>

    <div v-else-if="errorMessage" class="text-sm text-red-600 bg-red-50 border border-red-100 rounded-xl p-4">
      {{ errorMessage }}
    </div>

    <div v-else-if="orders.length" class="space-y-4">
      <article
        v-for="order in orders"
        :key="order.id"
        class="border border-gray-100 rounded-2xl p-5 hover:border-green-200 transition-colors"
      >
        <div class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
          <div>
            <p class="text-sm text-gray-500">주문번호 {{ order.orderNumber }}</p>
            <p class="text-xl font-semibold">{{ order.orderName }}</p>
          </div>
          <span
            class="inline-flex items-center gap-2 text-sm font-semibold px-3 py-1 rounded-full"
            :class="getStatusMeta(order.approvalStatus).badgeClass"
          >
            {{ getStatusMeta(order.approvalStatus).label }}
          </span>
        </div>

        <dl class="mt-4 grid gap-3 md:grid-cols-4 text-sm text-gray-600">
          <div>
            <dt class="text-gray-500">주문일</dt>
            <dd class="font-semibold">{{ formatOrderDate(order.orderedAt) }}</dd>
          </div>
          <div>
            <dt class="text-gray-500">상품 수</dt>
            <dd class="font-semibold">{{ order.itemCount }}건</dd>
          </div>
          <div>
            <dt class="text-gray-500">결제 금액</dt>
            <dd class="font-semibold">{{ formatCurrency(order.totalAmount) }}</dd>
          </div>
          <div>
            <dt class="text-gray-500">결제 상태</dt>
            <dd class="font-semibold">{{ getPaymentStatusLabel(order.paymentStatus) }}</dd>
          </div>
        </dl>

        <div class="mt-4 flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
          <p class="text-xs text-gray-500">주문 같은 날 다른 상품이 있다면 묶음으로 확인할 수 있어요.</p>
          <RouterLink
            :to="{ name: 'my-page-order-detail', params: { orderNumber: order.orderNumber } }"
            class="px-4 py-2 rounded-lg text-sm font-semibold border border-green-200 text-green-700 hover:bg-green-50 transition-colors"
          >
            주문 상세 보기
          </RouterLink>
        </div>
      </article>
    </div>
    <div v-else class="text-center text-gray-500 py-12 border border-dashed border-gray-200 rounded-2xl">
      해당 기간의 주문이 없어요. 기간을 넓혀 다시 확인해 주세요.
    </div>
  </section>
</template>

<script setup>
import { RouterLink } from 'vue-router';

defineProps({
  filterOptions: {
    type: Array,
    default: () => [],
  },
  selectedRange: {
    type: String,
    default: 'ALL',
  },
  filterDescription: {
    type: String,
    default: '',
  },
  orders: {
    type: Array,
    default: () => [],
  },
  isLoading: {
    type: Boolean,
    default: false,
  },
  errorMessage: {
    type: String,
    default: '',
  },
});

const emit = defineEmits(['change-filter']);

const orderStatusMeta = {
  pending_approval: { label: '승인 대기', badgeClass: 'bg-yellow-100 text-yellow-700' },
  approved: { label: '승인 완료', badgeClass: 'bg-green-100 text-green-700' },
  rejected: { label: '승인 거절', badgeClass: 'bg-red-100 text-red-600' },
  cancelled: { label: '주문 취소', badgeClass: 'bg-red-100 text-red-600' },
  shipping: { label: '배송 중', badgeClass: 'bg-blue-100 text-blue-700' },
  delivered: { label: '배송 완료', badgeClass: 'bg-green-100 text-green-700' },
};

const getStatusMeta = (status) => orderStatusMeta[status] ?? { label: '확인 필요', badgeClass: 'bg-gray-100 text-gray-600' };

const formatOrderDate = (date) => {
  if (!date) return '-';
  return new Date(date).toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' });
};

const formatCurrency = (value) => `${Number(value ?? 0).toLocaleString()}원`;

const paymentStatusLabel = {
  PENDING: '결제 대기',
  PAID: '결제 완료',
  REFUNDED: '환불 완료',
  FAILED: '결제 실패',
};

const getPaymentStatusLabel = (status) => paymentStatusLabel[status] ?? '확인 필요';
</script>
