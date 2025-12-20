<template>
  <section class="space-y-6">
    <header class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
      <div>
        <p class="text-sm font-semibold text-green-600">주문 상세</p>
        <h2 class="text-2xl font-semibold text-gray-900">주문번호 {{ orderDetail?.orderNumber ?? '-' }}</h2>
        <p class="text-sm text-gray-500">주문한 상품과 배송 정보를 확인할 수 있어요.</p>
      </div>
      <RouterLink
        :to="{ name: 'my-page-orders' }"
        class="inline-flex items-center justify-center px-4 py-2 rounded-lg border border-gray-200 text-sm font-semibold text-gray-600 hover:border-green-200 hover:text-green-700 transition-colors"
      >
        주문 목록으로
      </RouterLink>
    </header>

    <div v-if="isLoading" class="text-sm text-gray-500">주문 정보를 불러오고 있어요.</div>

    <div v-else-if="errorMessage" class="space-y-4">
      <div class="text-sm text-red-600 bg-red-50 border border-red-100 rounded-xl p-4">
        {{ errorMessage }}
      </div>
      <button
        type="button"
        class="px-4 py-2 rounded-lg text-sm font-semibold border border-green-200 text-green-700 hover:bg-green-50 transition-colors"
        @click="loadOrderDetail"
      >
        다시 불러오기
      </button>
    </div>

    <div v-else-if="orderDetail" class="space-y-6">
      <section class="bg-white border border-gray-100 rounded-2xl p-6 shadow-sm space-y-4">
        <div class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
          <div>
            <p class="text-sm text-gray-500">{{ orderDetail.orderName }}</p>
            <p class="text-2xl font-semibold text-gray-900">{{ formatCurrency(orderDetail.totalAmount) }}</p>
          </div>
          <div class="flex flex-col gap-2 items-start md:items-end">
            <span
              class="inline-flex items-center gap-2 text-sm font-semibold px-3 py-1 rounded-full"
              :class="getStatusMeta(orderDetail.approvalStatus).badgeClass"
            >
              {{ getStatusMeta(orderDetail.approvalStatus).label }}
            </span>
            <span class="text-sm text-gray-500">{{ getPaymentStatusLabel(orderDetail.paymentStatus) }}</span>
          </div>
        </div>

        <dl class="grid gap-4 text-sm text-gray-600 md:grid-cols-4">
          <div>
            <dt class="text-gray-500">주문일</dt>
            <dd class="font-semibold">{{ formatOrderDate(orderDetail.orderedAt) }}</dd>
          </div>
          <div>
            <dt class="text-gray-500">상품 수</dt>
            <dd class="font-semibold">{{ orderDetail.items.length }}건</dd>
          </div>
          <div>
            <dt class="text-gray-500">요청사항</dt>
            <dd class="font-semibold">{{ orderDetail.comment || '요청사항이 없어요.' }}</dd>
          </div>
        </dl>

        <div class="flex flex-col gap-2 sm:flex-row sm:justify-end">
          <button
            type="button"
            class="px-4 py-2 rounded-lg text-sm font-semibold border border-red-200 text-red-600 hover:bg-red-50 transition-colors"
            @click="handleRefundRequest"
          >
            환불 요청
          </button>
          <button
            type="button"
            class="px-4 py-2 rounded-lg text-sm font-semibold border border-green-200 text-green-700 hover:bg-green-50 transition-colors"
            @click="handleChangeAddress"
          >
            배송지 변경
          </button>
        </div>
      </section>

      <section class="bg-white border border-gray-100 rounded-2xl p-6 shadow-sm space-y-4">
        <h3 class="text-lg font-semibold text-gray-900">상품 목록</h3>
        <div class="space-y-3">
          <div
            v-for="item in orderDetail.items"
            :key="item.id"
            class="flex flex-col gap-2 rounded-xl border border-gray-100 p-4 md:flex-row md:items-center md:justify-between"
          >
            <div>
              <p class="text-sm text-gray-500">{{ item.productName }}</p>
              <p class="text-sm text-gray-600">수량 {{ item.quantity }}개 · 단가 {{ formatCurrency(item.unitPrice) }}</p>
            </div>
            <p class="text-lg font-semibold text-gray-900">{{ formatCurrency(item.totalPrice) }}</p>
          </div>
        </div>
      </section>

      <section class="grid gap-6 lg:grid-cols-2">
        <div class="bg-white border border-gray-100 rounded-2xl p-6 shadow-sm space-y-3">
          <h3 class="text-lg font-semibold text-gray-900">배송지</h3>
          <div class="text-sm text-gray-600 space-y-1">
            <p class="font-semibold text-gray-900">{{ orderDetail.address.name || '배송지 이름' }}</p>
            <p>{{ orderDetail.address.recipient }} · {{ orderDetail.address.phone }}</p>
            <p>{{ orderDetail.address.postalCode }} {{ orderDetail.address.addressLine }}</p>
            <p>{{ orderDetail.address.addressLineDetail }}</p>
            <p v-if="orderDetail.address.memo" class="text-gray-500">메모: {{ orderDetail.address.memo }}</p>
          </div>
        </div>

        <div class="bg-white border border-gray-100 rounded-2xl p-6 shadow-sm space-y-3">
          <h3 class="text-lg font-semibold text-gray-900">결제 정보</h3>
          <dl class="text-sm text-gray-600 space-y-2">
            <div class="flex items-center justify-between">
              <dt class="text-gray-500">상품 금액</dt>
              <dd class="font-semibold">{{ formatCurrency(orderDetail.merchandiseAmount) }}</dd>
            </div>
            <div class="flex items-center justify-between">
              <dt class="text-gray-500">배송비</dt>
              <dd class="font-semibold">{{ formatCurrency(orderDetail.shippingFee) }}</dd>
            </div>
            <div class="flex items-center justify-between">
              <dt class="text-gray-500">할인 금액</dt>
              <dd class="font-semibold">-{{ formatCurrency(orderDetail.discountAmount) }}</dd>
            </div>
            <div class="border-t border-gray-100 pt-3 flex items-center justify-between text-gray-900">
              <dt class="font-semibold">총 결제 금액</dt>
              <dd class="text-lg font-semibold">{{ formatCurrency(orderDetail.totalAmount) }}</dd>
            </div>
          </dl>
        </div>
      </section>
    </div>
  </section>
</template>

<script setup lang="js">
import { computed } from 'vue';
import { useRoute, RouterLink } from 'vue-router';
import { useOrderDetail } from '@/composables/useOrderDetail';

const route = useRoute();
const orderNumber = computed(() => route.params.orderNumber ?? '');

const { orderDetail, isLoading, errorMessage, loadOrderDetail } = useOrderDetail(orderNumber);

const orderStatusMeta = {
  pending_approval: { label: '승인 대기', badgeClass: 'bg-yellow-100 text-yellow-700' },
  approved: { label: '승인 완료', badgeClass: 'bg-green-100 text-green-700' },
  rejected: { label: '승인 거절', badgeClass: 'bg-red-100 text-red-600' },
  cancelled: { label: '주문 취소', badgeClass: 'bg-red-100 text-red-600' },
  shipping: { label: '배송 중', badgeClass: 'bg-blue-100 text-blue-700' },
  delivered: { label: '배송 완료', badgeClass: 'bg-green-100 text-green-700' },
};

const paymentStatusLabel = {
  PENDING: '결제 대기',
  PAID: '결제 완료',
  REFUNDED: '환불 완료',
  FAILED: '결제 실패',
};

const orderModeLabel = {
  CART: '장바구니 주문',
  DIRECT: '바로구매',
};

const getStatusMeta = (status) => orderStatusMeta[status] ?? { label: '확인 필요', badgeClass: 'bg-gray-100 text-gray-600' };

const getPaymentStatusLabel = (status) => paymentStatusLabel[status] ?? '확인 필요';

const getOrderModeLabel = (mode) => orderModeLabel[mode] ?? '확인 필요';

const formatOrderDate = (date) => {
  if (!date) return '-';
  return new Date(date).toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' });
};

const formatCurrency = (value) => `${Number(value ?? 0).toLocaleString()}원`;

const handleRefundRequest = () => {
  window.alert('환불 요청은 곧 연결할게요. 지금은 고객센터로 알려 주세요.');
};

const handleChangeAddress = () => {
  window.alert('배송지 변경은 곧 연결할게요. 필요하면 고객센터로 요청해 주세요.');
};
</script>
