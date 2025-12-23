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
              :class="getOrderStatusMeta(orderDetail).badgeClass"
            >
              {{ getOrderStatusMeta(orderDetail).label }}
            </span>
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
            v-if="shouldShowRefundAction"
            type="button"
            class="px-4 py-2 rounded-lg text-sm font-semibold border border-red-200 text-red-600 hover:bg-red-50 transition-colors disabled:cursor-not-allowed disabled:opacity-60"
            :disabled="isRefunding || !canRefund"
            @click="handleRefundRequest"
          >
            {{ isRefunding ? '환불 요청 중...' : '환불 요청' }}
          </button>
        </div>
        <p v-if="shouldShowRefundAction && refundBlockMessage" class="text-xs text-gray-500">
          {{ refundBlockMessage }}
        </p>
        <p v-if="refundMessage" class="text-sm text-green-700 bg-green-50 border border-green-100 rounded-lg px-3 py-2">
          {{ refundMessage }}
        </p>
        <p v-if="refundError" class="text-sm text-red-600 bg-red-50 border border-red-100 rounded-lg px-3 py-2">
          {{ refundError }}
        </p>
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
          <div class="flex items-center justify-between">
            <h3 class="text-lg font-semibold text-gray-900">배송지</h3>
            <button
              type="button"
              class="px-3 py-2 rounded-lg text-sm font-semibold border border-green-200 text-green-700 hover:bg-green-50 transition-colors disabled:cursor-not-allowed disabled:opacity-60"
              :disabled="!canEditAddress"
              @click="toggleAddressEditor"
            >
              배송지 변경
            </button>
          </div>
          <div class="text-sm text-gray-600 space-y-1">
            <p class="font-semibold text-gray-900">{{ orderDetail.address.name || '배송지 이름' }}</p>
            <p>{{ orderDetail.address.recipient }} · {{ orderDetail.address.phone }}</p>
            <p>{{ orderDetail.address.postalCode }} {{ orderDetail.address.addressLine }}</p>
            <p>{{ orderDetail.address.addressLineDetail }}</p>
            <p v-if="orderDetail.address.memo" class="text-gray-500">메모: {{ orderDetail.address.memo }}</p>
          </div>
          <p v-if="!canEditAddress" class="text-xs text-gray-500">요청이 접수된 주문은 배송지를 바꿀 수 없어요.</p>
          <div v-if="isAddressEditorOpen && canEditAddress" class="border border-dashed border-green-200 rounded-xl p-4 space-y-3">
            <p class="text-sm text-gray-600">변경할 배송지를 선택해 주세요.</p>
            <div v-if="isAddressLoading" class="text-sm text-gray-500">배송지를 불러오는 중이에요.</div>
            <div v-else-if="addressErrorMessage" class="text-sm text-red-600">
              {{ addressErrorMessage }}
            </div>
            <div v-else-if="addresses.length === 0" class="text-sm text-gray-500">
              등록된 배송지가 없어요. 배송지를 먼저 추가해 주세요.
              <RouterLink
                :to="{ name: 'my-page-addresses' }"
                class="text-green-600 font-semibold ml-1 hover:underline"
              >
                배송지 관리로 이동
              </RouterLink>
            </div>
            <div v-else class="space-y-3">
              <select
                v-model="selectedAddressId"
                class="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:border-green-500 focus:ring-2 focus:ring-green-100"
              >
                <option value="" disabled>배송지를 선택해 주세요.</option>
                <option v-for="address in addresses" :key="address.id" :value="address.id">
                  {{ address.name || address.recipient }} · {{ address.addressLine }}
                </option>
              </select>
              <div class="flex flex-col gap-2 sm:flex-row sm:justify-end">
                <button
                  type="button"
                  class="px-4 py-2 rounded-lg text-sm font-semibold border border-gray-200 text-gray-600 hover:bg-gray-50 transition-colors"
                  @click="closeAddressEditor"
                >
                  닫기
                </button>
                <button
                  type="button"
                  class="px-4 py-2 rounded-lg text-sm font-semibold border border-green-200 text-green-700 hover:bg-green-50 transition-colors"
                  :disabled="isChangingAddress || !selectedAddressId"
                  @click="handleChangeAddress"
                >
                  {{ isChangingAddress ? '변경 요청 중...' : '배송지 변경 요청' }}
                </button>
              </div>
              <p v-if="addressChangeMessage" class="text-sm text-green-700 bg-green-50 border border-green-100 rounded-lg px-3 py-2">
                {{ addressChangeMessage }}
              </p>
              <p v-if="addressChangeError" class="text-sm text-red-600 bg-red-50 border border-red-100 rounded-lg px-3 py-2">
                {{ addressChangeError }}
              </p>
            </div>
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

      <section class="bg-white border border-gray-100 rounded-2xl p-6 shadow-sm space-y-4">
        <div class="space-y-1">
          <h3 class="text-lg font-semibold text-gray-900">반품 · 교환</h3>
          <p class="text-sm text-gray-500">필요하면 사유를 선택하고 상세 내용을 입력해 주세요.</p>
        </div>

        <p
          v-if="claimBlockMessage"
          class="text-sm text-amber-700 bg-amber-50 border border-amber-100 rounded-lg px-3 py-2"
        >
          {{ claimBlockMessage }}
        </p>

        <div class="grid gap-4 md:grid-cols-2">
          <div class="border border-gray-100 rounded-xl p-4 space-y-3">
            <h4 class="text-base font-semibold text-gray-900">반품 신청</h4>
            <label class="flex flex-col gap-2 text-sm font-semibold text-gray-700">
              사유 선택
              <select
                v-model="returnReason"
                class="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:border-green-500 focus:ring-2 focus:ring-green-100"
              >
                <option value="" disabled>사유를 선택해 주세요.</option>
                <option v-for="reason in claimReasons" :key="reason.value" :value="reason.value">
                  {{ reason.label }}
                </option>
              </select>
            </label>
            <label class="flex flex-col gap-2 text-sm font-semibold text-gray-700">
              상세 내용
              <textarea
                v-model="returnDetail"
                rows="3"
                class="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:border-green-500 focus:ring-2 focus:ring-green-100"
                :placeholder="returnDetailPlaceholder"
              ></textarea>
            </label>
            <div class="flex justify-end">
              <button
                type="button"
                class="px-4 py-2 rounded-lg text-sm font-semibold border border-green-200 text-green-700 hover:bg-green-50 transition-colors disabled:cursor-not-allowed disabled:opacity-60"
                :disabled="isReturnSubmitting || !canReturn || !isReturnFormValid"
                @click="handleReturnRequest"
              >
                {{ isReturnSubmitting ? '반품 신청 중...' : '반품 신청' }}
              </button>
            </div>
            <p v-if="returnMessage" class="text-sm text-green-700 bg-green-50 border border-green-100 rounded-lg px-3 py-2">
              {{ returnMessage }}
            </p>
            <p v-if="returnError" class="text-sm text-red-600 bg-red-50 border border-red-100 rounded-lg px-3 py-2">
              {{ returnError }}
            </p>
          </div>

          <div class="border border-gray-100 rounded-xl p-4 space-y-3">
            <h4 class="text-base font-semibold text-gray-900">교환 신청</h4>
            <label class="flex flex-col gap-2 text-sm font-semibold text-gray-700">
              사유 선택
              <select
                v-model="exchangeReason"
                class="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:border-green-500 focus:ring-2 focus:ring-green-100"
              >
                <option value="" disabled>사유를 선택해 주세요.</option>
                <option v-for="reason in claimReasons" :key="reason.value" :value="reason.value">
                  {{ reason.label }}
                </option>
              </select>
            </label>
            <label class="flex flex-col gap-2 text-sm font-semibold text-gray-700">
              상세 내용
              <textarea
                v-model="exchangeDetail"
                rows="3"
                class="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:border-green-500 focus:ring-2 focus:ring-green-100"
                :placeholder="exchangeDetailPlaceholder"
              ></textarea>
            </label>
            <div class="flex justify-end">
              <button
                type="button"
                class="px-4 py-2 rounded-lg text-sm font-semibold border border-blue-200 text-blue-700 hover:bg-blue-50 transition-colors disabled:cursor-not-allowed disabled:opacity-60"
                :disabled="isExchangeSubmitting || !canExchange || !isExchangeFormValid"
                @click="handleExchangeRequest"
              >
                {{ isExchangeSubmitting ? '교환 신청 중...' : '교환 신청' }}
              </button>
            </div>
            <p v-if="exchangeMessage" class="text-sm text-green-700 bg-green-50 border border-green-100 rounded-lg px-3 py-2">
              {{ exchangeMessage }}
            </p>
            <p v-if="exchangeError" class="text-sm text-red-600 bg-red-50 border border-red-100 rounded-lg px-3 py-2">
              {{ exchangeError }}
            </p>
          </div>
        </div>
      </section>
    </div>
  </section>
</template>

<script setup lang="js">
import { computed, ref, watch, onMounted } from 'vue';
import { useRoute, RouterLink, useRouter } from 'vue-router';
import { useOrderDetail } from '@/composables/useOrderDetail';
import { useAddresses } from '@/composables/useAddresses';
import { requestOrderAddressChange } from '@/api/ordersApi';
import { useOrderClaims } from '@/composables/useOrderClaims';
import { usePaymentCallbacks } from '@/composables/usePaymentCallbacks';
import { clearPendingOrderRequest, readPendingOrderRequest } from '@/utils/paymentRequestStorage';
import { getOrderStatusMeta } from '@/utils/orderStatus';

const route = useRoute();
const router = useRouter();
const orderNumber = computed(() => route.params.orderNumber ?? '');

const { orderDetail, isLoading, errorMessage, loadOrderDetail } = useOrderDetail(orderNumber);
const { confirmPaymentFromQuery, confirmErrorMessage } = usePaymentCallbacks();
const {
  addresses,
  loadAddresses,
  isLoading: isAddressLoading,
  errorMessage: addressErrorMessage,
} = useAddresses();

const isAddressEditorOpen = ref(false);
const selectedAddressId = ref('');
const isChangingAddress = ref(false);
const addressChangeMessage = ref('');
const addressChangeError = ref('');

const {
  claimReasons,
  returnReason,
  returnDetail,
  exchangeReason,
  exchangeDetail,
  returnDetailPlaceholder,
  exchangeDetailPlaceholder,
  canRefund,
  refundBlockMessage,
  isRefunding,
  refundMessage,
  refundError,
  claimBlockMessage,
  canReturn,
  canExchange,
  isDelivered,
  isClaimLocked,
  isReturnSubmitting,
  isExchangeSubmitting,
  isReturnFormValid,
  isExchangeFormValid,
  returnMessage,
  returnError,
  exchangeMessage,
  exchangeError,
  requestRefund,
  requestReturn,
  requestExchange,
  loadRefundEligibility,
} = useOrderClaims(orderNumber, orderDetail);

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

const getPaymentStatusLabel = (status) => paymentStatusLabel[status] ?? '확인 필요';

const getOrderModeLabel = (mode) => orderModeLabel[mode] ?? '확인 필요';

const formatOrderDate = (date) => {
  if (!date) return '-';
  return new Date(date).toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' });
};

const formatCurrency = (value) => `${Number(value ?? 0).toLocaleString()}원`;

const shouldShowRefundAction = computed(() => !isClaimLocked.value);
const canEditAddress = computed(() => !isClaimLocked.value);

const normalizeQueryValue = (value) => (Array.isArray(value) ? value[0] : value);

const getStoredOrderPayload = (orderId) => {
  if (!orderId) return null;
  const stored = readPendingOrderRequest();
  if (!stored || stored.orderId !== orderId) return null;
  return {
    orderRequest: stored.orderRequest ?? null,
    checkoutItems: Array.isArray(stored.checkoutItems) ? stored.checkoutItems : [],
  };
};

const processPaymentResult = async () => {
  const paymentStatusFromQuery = route.query.paymentStatus;
  const orderIdFromQuery = normalizeQueryValue(route.query.orderId);
  const normalizedOrderId = orderIdFromQuery ? String(orderIdFromQuery) : '';
  const hasSuccessParams = Boolean(route.query.paymentKey && route.query.amount !== undefined && normalizedOrderId);

  if (paymentStatusFromQuery === 'fail') {
    await router.replace({
      name: 'order-checkout',
      query: { paymentStatus: 'fail', orderId: normalizedOrderId || orderNumber.value },
    });
    return;
  }

  if (!hasSuccessParams) {
    if (paymentStatusFromQuery === 'success') {
      confirmErrorMessage.value = '결제 정보를 찾지 못했어요. 다시 결제를 시작해 주세요.';
      await router.replace({
        name: 'order-checkout',
        query: { paymentStatus: 'fail', orderId: normalizedOrderId || orderNumber.value },
      });
    }
    return;
  }

  try {
    const storedPayload = getStoredOrderPayload(normalizedOrderId);
    const orderRequest = storedPayload?.orderRequest ?? null;
    await confirmPaymentFromQuery(route.query, { orderRequest });
    clearPendingOrderRequest();
    const resolvedOrderId = normalizedOrderId || orderNumber.value;
    await router.replace({ name: 'my-page-order-detail', params: { orderNumber: resolvedOrderId } });
    await loadOrderDetail(resolvedOrderId);
  } catch (error) {
    console.error(error);
    await router.replace({
      name: 'order-checkout',
      query: { paymentStatus: 'fail', orderId: normalizedOrderId || orderNumber.value },
    });
  }
};

const handleRefundRequest = async () => {
  if (!orderNumber.value || isRefunding.value) return;

  const shouldRequest = window.confirm('환불 요청을 접수할까요?');
  if (!shouldRequest) return;

  const isCompleted = await requestRefund();
  if (isCompleted) {
    await loadOrderDetail();
    await loadRefundEligibility();
  }
};

const closeAddressEditor = () => {
  isAddressEditorOpen.value = false;
  addressChangeMessage.value = '';
  addressChangeError.value = '';
};

const ensureAddressesLoaded = async () => {
  if (addresses.value.length > 0 || isAddressLoading.value) return;
  try {
    await loadAddresses();
  } catch (error) {
    return;
  }
};

const toggleAddressEditor = async () => {
  if (!canEditAddress.value) return;
  isAddressEditorOpen.value = !isAddressEditorOpen.value;
  if (!isAddressEditorOpen.value) return;
  await ensureAddressesLoaded();
};

const handleChangeAddress = async () => {
  if (!orderNumber.value || isChangingAddress.value) return;
  const addressId = Number(selectedAddressId.value);
  if (!Number.isFinite(addressId)) {
    addressChangeError.value = '배송지를 선택해 주세요.';
    return;
  }

  isChangingAddress.value = true;
  addressChangeMessage.value = '';
  addressChangeError.value = '';

  try {
    await requestOrderAddressChange(orderNumber.value, addressId);
    addressChangeMessage.value = '배송지 변경 요청을 접수했어요. 반영되면 알려드릴게요.';
    await loadOrderDetail();
    closeAddressEditor();
  } catch (error) {
    addressChangeError.value = error?.message ?? '배송지 변경 요청을 접수하지 못했어요.';
  } finally {
    isChangingAddress.value = false;
  }
};

const handleReturnRequest = async () => {
  if (!orderNumber.value || isReturnSubmitting.value) return;
  const shouldRequest = window.confirm('반품 신청을 접수할까요?');
  if (!shouldRequest) return;

  const isCompleted = await requestReturn();
  if (isCompleted) {
    await loadOrderDetail();
  }
};

const handleExchangeRequest = async () => {
  if (!orderNumber.value || isExchangeSubmitting.value) return;
  const shouldRequest = window.confirm('교환 신청을 접수할까요?');
  if (!shouldRequest) return;

  const isCompleted = await requestExchange();
  if (isCompleted) {
    await loadOrderDetail();
  }
};

watch(
  () => orderDetail.value?.address?.id,
  (value) => {
    if (!value) return;
    if (String(selectedAddressId.value || '') === String(value)) return;
    selectedAddressId.value = value;
  },
  { immediate: true },
);

watch(
  () => isAddressEditorOpen.value,
  (isOpen) => {
    if (!isOpen) return;
    if (selectedAddressId.value) return;
    if (addresses.value.length > 0) {
      selectedAddressId.value = addresses.value[0]?.id ?? '';
    }
  },
);

watch(
  () => canEditAddress.value,
  (value) => {
    if (value) return;
    closeAddressEditor();
  },
);

onMounted(() => {
  processPaymentResult().catch((error) => console.error(error));
});
</script>
