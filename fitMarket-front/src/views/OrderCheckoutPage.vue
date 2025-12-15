<template>
  <div class="min-h-screen bg-gradient-to-b from-green-50 to-white">
    <AppHeader />

    <div class="container mx-auto px-4 py-10">
      <div class="max-w-5xl mx-auto space-y-8">
        <section class="bg-white border border-green-100 rounded-2xl p-8 shadow-sm">
          <div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
            <div>
              <p class="text-sm font-semibold text-green-600 mb-2 flex items-center gap-2">
                <CheckCircle class="w-4 h-4" />
                배송지 선택 → 결제 준비
              </p>
              <h1 class="text-3xl font-bold mb-2">배송지를 고르고 결제를 준비해요</h1>
              <p class="text-gray-600">상품 {{ cartItems.length }}개 · 결제 전까지 언제든 수정할 수 있어요.</p>
            </div>
            <div class="bg-green-50 border border-green-100 rounded-xl px-4 py-3 text-sm text-green-700">
              <p class="font-semibold">한눈에 보는 단계</p>
              <p>1. 배송지 선택 · 2. 결제하기 · 3. 주문 완료</p>
            </div>
          </div>
        </section>

        <div
          v-if="paymentFailureNotice"
          class="bg-red-50 border border-red-200 rounded-2xl p-4 flex items-start gap-3"
        >
          <AlertTriangle class="w-5 h-5 text-red-600 mt-0.5" />
          <div class="flex-1">
            <p class="font-semibold text-red-700">{{ paymentFailureNotice.title }}</p>
            <p class="text-sm text-red-600 mt-1">{{ paymentFailureNotice.message }}</p>
            <p v-if="paymentFailureNotice.guide" class="text-xs text-red-500 mt-1">
              {{ paymentFailureNotice.guide }}
            </p>
            <p v-if="paymentFailureNotice.orderId" class="text-[11px] text-red-400 mt-1">
              주문번호 {{ paymentFailureNotice.orderId }}
            </p>
            <p v-if="isLoadingFailure" class="text-[11px] text-red-400 mt-1">
              결제 실패 안내를 정리하는 중이에요.
            </p>
          </div>
          <button
            class="text-xs text-red-600 font-semibold hover:underline"
            @click="dismissFailureNotice"
          >
            닫기
          </button>
        </div>

        <div class="grid gap-6 lg:grid-cols-2">
          <div class="bg-white border border-green-100 rounded-2xl p-6">
            <div class="flex items-center justify-between mb-6">
              <div>
                <h2 class="text-xl font-bold">배송지 선택</h2>
                <p class="text-sm text-gray-500">기본 배송지를 먼저 적용했어요.</p>
              </div>
              <span class="inline-flex items-center gap-2 text-sm text-green-600 bg-green-50 px-3 py-1 rounded-full">
                <MapPin class="w-4 h-4" />
                {{ selectedAddress?.name || selectedAddress?.recipient || '배송지' }}
              </span>
            </div>

            <div class="space-y-4 max-h-96 overflow-y-auto pr-1">
              <div
                v-if="isAddressLoading"
                class="border border-dashed border-green-200 rounded-xl p-4 text-sm text-gray-600 bg-green-50/60"
              >
                배송지를 불러오는 중이에요. 잠시만 기다려 주세요.
              </div>
              <div
                v-else-if="addressErrorMessage"
                class="border border-red-200 bg-red-50 rounded-xl p-4 text-sm text-red-700"
              >
                {{ addressErrorMessage }}
              </div>
              <label
                v-else
                v-for="address in displayedAddresses"
                :key="address.id"
                class="block border rounded-xl p-4 cursor-pointer transition-all"
                :class="selectedAddressId === address.id ? 'border-green-500 bg-green-50/80 shadow-sm' : 'border-green-100 hover:border-green-200'"
              >
                <div class="flex items-start justify-between gap-3">
                  <div>
                    <div class="flex items-center gap-2 mb-1">
                      <input
                        type="radio"
                        name="checkout-shipping-address"
                        class="sr-only"
                        :checked="selectedAddressId === address.id"
                        @change="handleAddressChange(address.id)"
                      />
                      <p class="font-semibold">{{ address.name || address.recipient || '배송지' }}</p>
                      <span v-if="address.main" class="text-xs px-2 py-0.5 rounded-full bg-green-100 text-green-700">기본</span>
                    </div>
                    <p class="text-sm text-gray-700">{{ address.recipient }} · {{ address.phone }}</p>
                    <p class="text-sm text-gray-500">
                      {{ address.addressLine }} {{ address.addressLineDetail }}
                      <span v-if="address.postalCode" class="text-gray-400">({{ address.postalCode }})</span>
                    </p>
                    <p v-if="address.memo" class="text-xs text-gray-400 mt-2">{{ address.memo }}</p>
                  </div>
                  <CheckCircle class="w-5 h-5 text-green-500" v-if="selectedAddressId === address.id" />
                </div>
              </label>
            </div>

            <button
              v-if="addressListOverflow && !isAddressLoading && !addressErrorMessage"
              class="mt-4 w-full border border-green-200 text-green-700 font-semibold py-2 rounded-lg hover:bg-green-50 transition-colors"
              @click="toggleAddressList"
            >
              {{ showAllAddresses ? '배송지 접기' : `+${remainingAddressCount}개 더 보기` }}
            </button>
          </div>

          <div class="space-y-6">
            <div class="bg-white border border-green-100 rounded-2xl p-6">
              <div class="flex items-center justify-between mb-6">
                <div>
                  <h2 class="text-xl font-bold">포인트 · 결제</h2>
                  <p class="text-sm text-gray-500">포인트와 결제수단은 곧 연결될 예정이에요.</p>
                </div>
                <ShieldCheck class="w-5 h-5 text-green-500" />
              </div>

              <div class="space-y-4 text-sm">
                <div class="border border-dashed border-green-200 rounded-xl p-4 flex items-start gap-3">
                  <Gift class="w-5 h-5 text-green-500" />
                  <div>
                    <p class="font-semibold">포인트 사용</p>
                    <p class="text-gray-600">포인트 적립/사용 기능은 준비 중이에요.</p>
                  </div>
                  <span class="ml-auto text-gray-400">준비 중</span>
                </div>
                <div class="border border-dashed border-green-200 rounded-xl p-4 flex items-start gap-3">
                  <Wallet class="w-5 h-5 text-green-500" />
                  <div>
                    <p class="font-semibold">결제 수단</p>
                    <p class="text-gray-600">신용/체크카드, 간편결제, 계좌이체 순차 지원 예정</p>
                  </div>
                  <span class="ml-auto text-gray-400">곧 지원</span>
                </div>
              </div>

              <div class="border-t border-green-100 mt-6 pt-4 space-y-2 text-sm">
                <div class="flex justify-between text-gray-600">
                  <span>상품 금액</span>
                  <span>{{ totalPrice.toLocaleString() }}원</span>
                </div>
                <div class="flex justify-between text-gray-600">
                  <span>배송비</span>
                  <span>{{ shippingFee.toLocaleString() }}원</span>
                </div>
                <div class="flex justify-between text-gray-900 font-semibold text-lg">
                  <span>결제 예정 금액</span>
                  <span>{{ totalPayment.toLocaleString() }}원</span>
                </div>
              </div>

              <button
                class="mt-6 w-full bg-gradient-to-r from-green-500 to-green-600 hover:from-green-600 hover:to-green-700 text-white font-semibold py-4 rounded-lg transition"
                :class="isPaymentDisabled ? 'opacity-60 cursor-not-allowed' : ''"
                :disabled="isPaymentDisabled"
                @click="handlePayment"
              >
                {{ isPaymentRequesting ? '결제창을 여는 중이에요...' : '결제하기' }}
              </button>
              <p v-if="paymentErrorMessage" class="mt-2 text-xs text-red-600 text-center">
                {{ paymentErrorMessage }}
              </p>
              <p class="mt-2 text-xs text-gray-500 text-center">결제하기를 누르면 결제가 즉시 진행돼요.</p>
            </div>

            <div class="bg-white border border-green-100 rounded-2xl p-6">
              <h2 class="text-xl font-bold mb-4">주문 요약</h2>
              <div class="space-y-2 text-sm text-gray-600">
                <p>총 {{ cartItems.length }}개 상품</p>
                <p>선택한 배송지: {{ selectedAddress?.addressLine }} {{ selectedAddress?.addressLineDetail }}</p>
                <p>배송 메모: {{ selectedAddress?.memo || '입력된 메모가 없어요.' }}</p>
              </div>
            </div>
          </div>
        </div>

        <section class="bg-white border border-green-100 rounded-2xl p-6">
          <div class="flex items-center justify-between mb-6">
            <div>
              <h2 class="text-xl font-bold">담긴 상품</h2>
              <p class="text-sm text-gray-500">상품을 누르면 상세 정보로 이동할 수 있어요.</p>
            </div>
            <span class="text-sm text-gray-500">총 {{ cartItems.length }}건</span>
          </div>

          <div v-if="isCartLoading" class="py-6 text-center text-gray-500">장바구니를 불러오는 중이에요.</div>
          <div v-else-if="cartItems.length" class="divide-y divide-green-100">
            <div
              v-for="item in cartItems"
              :key="item.cartItemId || item.productId"
              class="py-4 flex items-center gap-4 cursor-pointer transition-colors hover:bg-green-50/70 px-2 rounded-xl"
              @click="navigateToProduct(item.productId || item.id)"
            >
              <img :src="item.image" :alt="item.name" class="w-20 h-20 rounded-xl object-cover bg-green-50" />
              <div class="flex-1">
                <p class="font-semibold">{{ item.name }}</p>
                <p class="text-sm text-gray-500">{{ item.category }} · {{ item.calories * item.quantity }}kcal</p>
              </div>
              <div class="text-right">
                <p class="text-sm text-gray-500">수량 {{ item.quantity }}개</p>
                <p class="text-lg font-bold">{{ (item.price * item.quantity).toLocaleString() }}원</p>
              </div>
            </div>
          </div>
          <div v-else class="text-center py-12 text-gray-500">
            장바구니가 비어 있어요. 상품을 담고 다시 시도해 주세요.
          </div>
        </section>
      </div>
    </div>

    <AppFooter />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { MapPin, ShieldCheck, Gift, Wallet, CheckCircle, AlertTriangle } from 'lucide-vue-next';
import AppHeader from '@/components/AppHeader.vue';
import AppFooter from '@/components/AppFooter.vue';
import { useCart } from '@/composables/useCart';
import { useAddresses } from '@/composables/useAddresses';
import { useOrderStatus } from '@/composables/useOrderStatus';
import { useTossPayments } from '@/composables/useTossPayments';
import { usePaymentCallbacks } from '@/composables/usePaymentCallbacks';
import { formatPhoneNumber, sanitizePhoneDigits } from '@/utils/phone';

const router = useRouter();
const route = useRoute();
const MAX_VISIBLE_ADDRESSES = 3;
const TOSS_CLIENT_KEY = 'test_ck_6bJXmgo28eDWxw4yY4oyrLAnGKWx';
const TOSS_CUSTOMER_KEY = 'A_I811IPruggOPKpP-5ee';

const { cartItems, totalPrice, isLoading: isCartLoading, loadCart } = useCart();
const {
  addresses,
  selectedAddress,
  selectedAddressId,
  selectAddress,
  loadAddresses,
  isLoading: isAddressLoading,
  errorMessage: addressErrorMessage,
} = useAddresses();
const { orderNumber, shippingFee, resetOrderStatus, setOrderNumber } = useOrderStatus();
const {
  requestCardPayment,
  isRequesting: isPaymentRequesting,
  errorMessage: paymentErrorMessage,
  prepareSdk,
} = useTossPayments(TOSS_CLIENT_KEY, TOSS_CUSTOMER_KEY);
const {
  loadFailureGuide,
  hasFailureParams,
  failureGuide,
  failureErrorMessage,
  isLoadingFailure,
} = usePaymentCallbacks();

const showAllAddresses = ref(false);
const paymentFailureFallback = ref('');

const addressListOverflow = computed(() => addresses.value.length > MAX_VISIBLE_ADDRESSES);
const displayedAddresses = computed(() => {
  const source = showAllAddresses.value ? addresses.value : addresses.value.slice(0, MAX_VISIBLE_ADDRESSES);
  return source.map((address) => ({
    ...address,
    phone: formatPhoneNumber(address.phone),
  }));
});
const remainingAddressCount = computed(() => Math.max(addresses.value.length - MAX_VISIBLE_ADDRESSES, 0));

const totalPayment = computed(() => totalPrice.value + shippingFee);
const orderName = computed(() => {
  if (!cartItems.value.length) return '핏마켓 주문';
  if (cartItems.value.length === 1) return cartItems.value[0].name;
  return `${cartItems.value[0].name} 외 ${cartItems.value.length - 1}건`;
});

const baseUrl = typeof window !== 'undefined' ? window.location.origin : '';
const successUrl = computed(() => `${baseUrl}/order/complete?paymentStatus=success`);
const failUrl = computed(() => `${baseUrl}/order/checkout?paymentStatus=fail`);
const customerName = computed(() => selectedAddress.value?.recipient || selectedAddress.value?.name || '핏마켓 고객');
const customerPhone = computed(() => sanitizePhoneDigits(selectedAddress.value?.phone));
const isPaymentDisabled = computed(
  () =>
    isPaymentRequesting.value || isCartLoading.value || isAddressLoading.value || !cartItems.value.length,
);
const paymentFailureNotice = computed(() => {
  if (failureGuide.value) {
    return {
      title: '결제가 중단되었어요.',
      message: failureGuide.value.message || '결제가 정상적으로 완료되지 않았어요.',
      guide: failureGuide.value.guide,
      orderId: failureGuide.value.orderId,
    };
  }
  if (paymentFailureFallback.value || failureErrorMessage.value) {
    return {
      title: '결제에 실패했어요.',
      message: paymentFailureFallback.value || failureErrorMessage.value,
      guide: '결제를 다시 시도해 주세요.',
      orderId: route.query.orderId ? String(route.query.orderId) : '',
    };
  }
  return null;
});

const handleAddressChange = (addressId) => {
  selectAddress(addressId);
};

const toggleAddressList = () => {
  showAllAddresses.value = !showAllAddresses.value;
};

const processPaymentFailure = async () => {
  const query = route.query;
  if (!hasFailureParams(query) && query.paymentStatus !== 'fail') return;

  if (hasFailureParams(query)) {
    try {
      const guide = await loadFailureGuide(query);
      if (guide?.orderId) {
        setOrderNumber(String(guide.orderId));
      }
    } catch (error) {
      console.error(error);
    }
  } else {
    paymentFailureFallback.value = '결제가 완료되지 않았어요. 다시 시도해 주세요.';
    if (query?.orderId) {
      setOrderNumber(String(query.orderId));
    }
  }

  router.replace({ name: 'order-checkout' });
};

const dismissFailureNotice = () => {
  failureGuide.value = null;
  failureErrorMessage.value = '';
  paymentFailureFallback.value = '';
};

const handlePayment = async () => {
  if (!cartItems.value.length) {
    window.alert('장바구니가 비어 있어요. 상품을 담고 다시 시도해 주세요.');
    router.push({ name: 'home' });
    return;
  }
  if (!selectedAddress.value) {
    window.alert('배송지를 먼저 선택해 주세요.');
    return;
  }

  const sdkReady = await prepareSdk();
  if (!sdkReady) {
    window.alert(paymentErrorMessage.value || '결제창을 준비하지 못했어요. 잠시 후 다시 시도해 주세요.');
    return;
  }

  try {
    await requestCardPayment({
      amount: {
        currency: 'KRW',
        value: totalPayment.value,
      },
      orderId: orderNumber.value,
      orderName: orderName.value,
      successUrl: successUrl.value,
      failUrl: failUrl.value,
      customerName: customerName.value,
      customerMobilePhone: customerPhone.value || undefined,
      card: {
        useEscrow: false,
        flowMode: 'DEFAULT',
        useCardPoint: false,
        useAppCardOnly: false,
      },
    });
  } catch (error) {
    const message = error?.message ?? '결제창을 열지 못했어요. 잠시 후 다시 시도해 주세요.';
    window.alert(message);
  }
};

const navigateToProduct = (productId) => {
  if (!productId) return;
  router.push({ name: 'product-detail', params: { id: productId } });
};

onMounted(() => {
  resetOrderStatus();
  loadCart({ force: true }).catch((error) => console.error(error));
  loadAddresses().catch((error) => {
    console.error(error);
  });
  processPaymentFailure().catch((error) => console.error(error));
  prepareSdk().catch((error) => console.error(error));
});
</script>
