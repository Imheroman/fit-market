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
        class="border border-gray-100 rounded-2xl p-5 cursor-pointer transition-all duration-200 hover:border-green-200 hover:scale-[1.01] hover:shadow-sm"
        tabindex="0"
        role="link"
        @click="emit('select-order', order.orderNumber)"
        @keydown.enter.prevent="emit('select-order', order.orderNumber)"
      >
        <div class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
          <div>
            <p class="text-sm text-gray-500">주문번호 {{ order.orderNumber }}</p>
            <p class="text-xl font-semibold">{{ order.orderName }}</p>
          </div>
          <span
            class="inline-flex items-center gap-2 text-sm font-semibold px-3 py-1 rounded-full"
            :class="getOrderStatusMeta(order).badgeClass"
          >
            {{ getOrderStatusMeta(order).label }}
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
        </dl>

        <div class="mt-4 flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
          <p class="text-xs text-gray-500">주문 같은 날 다른 상품이 있다면 묶음으로 확인할 수 있어요.</p>
          <div class="flex flex-wrap gap-2">
            <button
              class="px-3 py-2 rounded-lg text-sm font-semibold border transition-colors"
              :class="isDeleting(order.orderNumber) ? 'border-gray-200 text-gray-400 bg-gray-50 cursor-not-allowed' : 'border-red-200 text-red-600 hover:bg-red-50'"
              :disabled="isDeleting(order.orderNumber)"
              @click.stop="emit('delete-order', order)"
            >
              {{ isDeleting(order.orderNumber) ? '삭제 중...' : '주문 삭제' }}
            </button>
          </div>
        </div>
      </article>
    </div>
    <div v-else class="text-center text-gray-500 py-12 border border-dashed border-gray-200 rounded-2xl">
      해당 기간의 주문이 없어요. 기간을 넓혀 다시 확인해 주세요.
    </div>
  </section>
</template>

<script setup>
import { getOrderStatusMeta } from '@/utils/orderStatus';

const props = defineProps({
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
  deletingOrderNumbers: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits(['change-filter', 'select-order', 'delete-order']);

const isDeleting = (orderNumber) => props.deletingOrderNumbers.includes(orderNumber);

const formatOrderDate = (date) => {
  if (!date) return '-';
  return new Date(date).toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' });
};

const formatCurrency = (value) => `${Number(value ?? 0).toLocaleString()}원`;
</script>
