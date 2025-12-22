<template>
    <div class="min-h-screen bg-gradient-to-b from-green-50 to-white">
        <AppHeader/>

        <div class="container mx-auto px-4 py-10">
            <div class="max-w-5xl mx-auto space-y-8">
                <section class="bg-white border border-green-100 rounded-2xl p-8 shadow-sm">
                    <div class="flex flex-col gap-6 md:flex-row md:items-center md:justify-between">
                        <div>
                            <p
                                class="text-sm font-semibold mb-2 flex items-center gap-2"
                                :class="isCancelled ? 'text-red-600' : 'text-green-600'"
                            >
                                <component :is="isCancelled ? AlertTriangle : CheckCircle" class="w-4 h-4"/>
                                {{ isCancelled ? '주문이 취소되었어요' : '결제가 끝났어요' }}
                            </p>
                            <h1 class="text-3xl font-bold mb-2">
                                {{ isCancelled ? '주문이 취소되었습니다' : '주문이 완료되었어요' }}
                            </h1>
                            <p class="text-gray-600">
                                주문번호 {{ orderNumber }} ·
                                <span v-if="isCancelled">취소 완료</span>
                                <span v-else>총 {{ checkoutItems.length }}개 상품</span>
                            </p>
                            <p
                                v-if="isConfirming"
                                class="text-sm text-blue-600 mt-2"
                            >
                                결제 승인 정보를 확인하고 있어요. 잠시만 기다려 주세요.
                            </p>
                            <p
                                v-else-if="confirmErrorMessage"
                                class="text-sm text-red-600 mt-2"
                            >
                                {{ confirmErrorMessage }}
                            </p>
                        </div>
                        <div class="text-right">
                            <p class="text-sm text-gray-500">{{ isCancelled ? '환불 예정 금액' : '최종 결제 금액' }}</p>
                            <p :class="['text-3xl font-bold', isCancelled ? 'text-red-500' : 'text-green-600']">
                                {{ totalPayment.toLocaleString() }}원
                            </p>
                            <p class="text-sm text-gray-500 mt-1">
                                {{ isCancelled ? '환불은 영업일 기준 3일 이내 완료돼요.' : '배송비 3,000원 포함' }}
                            </p>
                        </div>
                    </div>
                </section>

                <section class="space-y-6">
                    <div class="bg-white border border-green-100 rounded-2xl p-6">
                        <div class="flex flex-col gap-2 md:flex-row md:items-start md:justify-between mb-6">
                            <div>
                                <h2 class="text-xl font-bold">배송지 정보</h2>
                                <p class="text-sm text-gray-500">
                                    {{ isCancelled ? '주문이 취소되어 배송지를 수정할 수 없어요.' : '결제 완료 상태일 때만 배송지 변경을 요청할 수 있어요.' }}
                                </p>
                            </div>
                            <span
                                class="inline-flex items-center gap-2 text-sm px-3 py-1 rounded-full"
                                :class="canEditAddress ? 'text-green-600 bg-green-50' : 'text-gray-500 bg-gray-100'"
                            >
                <MapPin class="w-4 h-4"/>
                {{ formattedSelectedAddress?.name || formattedSelectedAddress?.recipient || '배송지' }}
                {{ canEditAddress ? '수정 가능' : isCancelled ? '배송 중단' : '수정 잠금' }}
              </span>
                        </div>

                        <p
                            v-if="isAddressLoading"
                            class="text-sm text-gray-600 bg-green-50 border border-green-100 rounded-lg px-3 py-2 mb-4"
                        >
                            배송지 정보를 불러오는 중이에요.
                        </p>
                        <p
                            v-else-if="addressErrorMessage"
                            class="text-sm text-red-600 bg-red-50 border border-red-100 rounded-lg px-3 py-2 mb-4"
                        >
                            {{ addressErrorMessage }}
                        </p>

                        <div
                            class="border border-green-100 rounded-2xl p-5 bg-gradient-to-br from-green-50/60 to-white">
                            <div class="flex flex-col gap-2">
                                <p class="text-sm font-semibold text-gray-500 uppercase tracking-wide">받는 분</p>
                                <p class="text-lg font-bold">{{ formattedSelectedAddress?.recipient }} ·
                                    {{ formattedSelectedAddress?.phone }}</p>
                                <p class="text-gray-700">
                                    {{ formattedSelectedAddress?.addressLine }}
                                    {{ formattedSelectedAddress?.addressLineDetail }}
                                    <span v-if="formattedSelectedAddress?.postalCode"
                                          class="text-gray-400">({{ formattedSelectedAddress?.postalCode }})</span>
                                </p>
                                <p class="text-sm text-gray-500">배송 메모:
                                    {{ formattedSelectedAddress?.memo || '입력된 메모가 없어요.' }}</p>
                            </div>
                        </div>

                        <div class="mt-5 space-y-3">
                            <div class="flex items-start gap-3 text-sm">
                                <AlertTriangle class="w-4 h-4 mt-0.5"
                                               :class="canEditAddress ? 'text-green-600' : 'text-gray-400'"/>
                                <p class="text-gray-600">
                                    {{
                                        canEditAddress
                                            ? '상품 준비가 시작되기 전까지 배송지 변경이 가능해요. 필요하면 아래 버튼을 눌러 요청해 주세요.'
                                            : '배송이 준비 중이거나 주문이 취소되어 배송지를 바꿀 수 없어요.'
                                    }}
                                </p>
                            </div>
                            <button
                                class="w-full md:w-auto px-5 py-3 rounded-lg font-semibold flex items-center justify-center gap-2 transition-colors"
                                :class="canEditAddress ? 'bg-green-600 hover:bg-green-700 text-white' : 'bg-gray-100 text-gray-400 cursor-not-allowed'"
                                :disabled="!canEditAddress"
                                @click="handleEditAddress"
                            >
                                <Edit3 class="w-4 h-4"/>
                                배송지 변경 요청하기
                            </button>
                        </div>
                    </div>

                    <div class="bg-white border border-green-100 rounded-2xl p-6">
                        <div class="flex items-center justify-between mb-6">
                            <div>
                                <h2 class="text-xl font-bold">결제 상태</h2>
                                <p class="text-sm text-gray-500">
                                    {{ isCancelled ? '결제와 주문이 모두 취소되었어요.' : '결제 완료 상태에서만 무료 취소가 가능해요.' }}
                                </p>
                            </div>
                            <span
                                class="inline-flex items-center gap-2 text-sm px-3 py-1 rounded-full"
                                :class="isCancelled ? 'text-red-600 bg-red-50' : 'text-blue-600 bg-blue-50'"
                            >
                <ShieldCheck class="w-4 h-4"/>
                {{ isCancelled ? '취소 완료' : '안전결제 보장' }}
              </span>
                        </div>

                        <div class="space-y-4">
                            <div
                                class="border rounded-xl p-4"
                                :class="isCancelled ? 'border-red-200 bg-red-50/80' : 'border-green-500 bg-green-50/70'"
                            >
                                <div class="flex items-center gap-2 mb-2">
                                    <BadgeCheck
                                        class="w-5 h-5"
                                        :class="isCancelled ? 'text-red-500' : 'text-green-600'"
                                    />
                                    <p class="font-semibold">{{ isCancelled ? '결제 취소 완료' : '결제 완료' }}</p>
                                    <span class="text-xs" :class="isCancelled ? 'text-red-600' : 'text-green-600'">
                    {{ isCancelled ? '환불 대기' : '무료취소 가능' }}
                  </span>
                                </div>
                                <p class="text-sm text-gray-600">
                                    {{
                                        isCancelled ? '취소 접수가 끝났어요. 영업일 기준 3일 내로 환불해 드릴게요.' : '필요하면 지금 바로 무료로 취소할 수 있어요.'
                                    }}
                                </p>
                                <button
                                    v-if="!isCancelled"
                                    class="mt-3 text-sm text-green-700 font-semibold hover:underline"
                                    @click="handleCancelRequest"
                                >
                                    결제 취소 요청하기
                                </button>
                                <p v-else class="mt-3 text-xs text-gray-500">환불 상태는 알림으로 다시 알려드릴게요.</p>
                            </div>

                            <div v-if="paymentResult" class="border rounded-xl p-4 border-blue-200 bg-blue-50/60">
                                <p class="font-semibold text-blue-700 mb-1">승인된 결제 정보</p>
                                <p class="text-sm text-gray-700">
                                    결제수단: {{ paymentResult.method || '선택된 결제수단' }}
                                </p>
                                <p class="text-sm text-gray-700">
                                    승인 금액: {{
                                        (paymentResult.totalAmount ?? paymentResult.amount ?? totalPayment).toLocaleString()
                                    }}원
                                </p>
                                <p class="text-xs text-gray-500 mt-1">결제 키: {{ paymentResult.paymentKey }}</p>
                            </div>

                            <div v-if="!isCancelled" class="border rounded-xl p-4 border-yellow-200 bg-yellow-50/70">
                                <div class="flex items-center gap-2 mb-2">
                                    <PackageCheck class="w-5 h-5 text-yellow-600"/>
                                    <p class="font-semibold">주문 접수 · 상품 준비</p>
                                </div>
                                <p class="text-sm text-gray-600">상품 준비가 시작되면 취소 시 수수료가 발생해요.</p>
                                <p class="text-xs text-gray-500 mt-2">주문 완료 이후 취소 수수료:
                                    {{ cancellationFee.toLocaleString() }}원</p>
                            </div>

                            <div v-else class="border rounded-xl p-4 border-gray-200 bg-gray-50">
                                <p class="font-semibold text-gray-700 mb-1">주문이 안전하게 취소되었어요.</p>
                                <p class="text-sm text-gray-600">배송 준비는 중단되었으며, 환불이 완료되면 알림으로 알려드릴게요.</p>
                            </div>
                        </div>
                    </div>
                </section>

                <section class="bg-white border border-green-100 rounded-2xl p-6">
                    <div class="flex items-center justify-between mb-6">
                        <h2 class="text-xl font-bold">주문한 상품</h2>
                        <p class="text-sm text-gray-500">총 {{ checkoutItems.length }}건</p>
                    </div>

                    <div v-if="isCheckoutLoading" class="py-6 text-center text-gray-500">결제 정보를 불러오는 중이에요.</div>
                    <div v-else-if="!checkoutItems.length" class="py-6 text-center text-gray-500">
                        {{ emptyItemsMessage }}
                    </div>
                    <div v-else class="divide-y divide-green-100">
                        <div
                            v-for="item in checkoutItems"
                            :key="item.cartItemId || item.productId"
                            class="py-4 flex items-center gap-4 cursor-pointer transition-colors hover:bg-green-50/70 px-2 rounded-xl"
                            @click="navigateToProduct(item.productId || item.id)"
                        >
                            <img :src="item.image" :alt="item.name"
                                 class="w-20 h-20 rounded-xl object-cover bg-green-50"/>
                            <div class="flex-1">
                                <p class="font-semibold">{{ item.name }}</p>
                                <p class="text-sm text-gray-500">{{ item.category }} · {{
                                        item.calories * item.quantity
                                    }}kcal</p>
                            </div>
                            <div class="text-right">
                                <p class="text-sm text-gray-500">수량 {{ item.quantity }}개</p>
                                <p class="text-lg font-bold">{{ (item.price * item.quantity).toLocaleString() }}원</p>
                            </div>
                        </div>
                    </div>

                    <div class="border-t border-green-100 mt-6 pt-6 space-y-2 text-sm">
                        <div class="flex justify-between text-gray-600">
                            <span>상품 금액</span>
                            <span>{{ displayTotalPrice.toLocaleString() }}원</span>
                        </div>
                        <div class="flex justify-between text-gray-600">
                            <span>배송비</span>
                            <span>{{ shippingFee.toLocaleString() }}원</span>
                        </div>
                        <div class="flex justify-between text-gray-900 font-semibold text-lg">
                            <span>{{ isCancelled ? '환불 예정 금액' : '총 결제 금액' }}</span>
                            <span>{{ totalPayment.toLocaleString() }}원</span>
                        </div>
                    </div>
                </section>
            </div>
        </div>

        <AppFooter/>
    </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import {CheckCircle, MapPin, ShieldCheck, BadgeCheck, PackageCheck, AlertTriangle, Edit3} from 'lucide-vue-next';
import AppHeader from '@/components/AppHeader.vue';
import AppFooter from '@/components/AppFooter.vue';
import {useCart} from '@/composables/useCart';
import {useAddresses} from '@/composables/useAddresses';
import {useOrderStatus} from '@/composables/useOrderStatus';
import {usePaymentCallbacks} from '@/composables/usePaymentCallbacks';
import {useProductDetail} from '@/composables/useProductDetail';
import {formatPhoneNumber} from '@/utils/phone';
import {clearPendingOrderRequest, readPendingOrderRequest} from '@/utils/paymentRequestStorage';

const router = useRouter();
const route = useRoute();
const {cartItems, isLoading: isCartLoading, loadCart} = useCart();
const {selectedAddress, loadAddresses, isLoading: isAddressLoading, errorMessage: addressErrorMessage} = useAddresses();
const {
    orderNumber,
    shippingFee,
    isCancelled,
    cancellationFee,
    canFreeCancel,
    cancelOrder,
    completePayment,
    setOrderNumber,
} = useOrderStatus();
const {confirmPaymentFromQuery, confirmErrorMessage, isConfirming} = usePaymentCallbacks();
const paymentResult = ref(null);
const pendingOrderRequest = ref(null);
const pendingCheckoutItems = ref([]);
const orderMode = ref('cart');

const normalizeOrderMode = (value) => {
    const normalized = typeof value === 'string' ? value.toLowerCase() : '';
    return normalized === 'direct' ? 'direct' : 'cart';
};

const toNumberSafe = (value) => {
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : null;
};

const normalizeQuantity = (value) => {
    const parsed = Math.floor(Number(value) || 0);
    if (!Number.isFinite(parsed) || parsed < 1) return 1;
    if (parsed > 100) return 100;
    return parsed;
};

const normalizeCheckoutItem = (item) => ({
    cartItemId: item?.cartItemId ?? null,
    productId: item?.productId ?? item?.id ?? null,
    name: item?.name ?? '',
    category: item?.category ?? '',
    price: Number(item?.price ?? 0),
    quantity: normalizeQuantity(item?.quantity),
    image: item?.image ?? '',
    calories: Number(item?.calories ?? 0),
    protein: Number(item?.protein ?? 0),
    carbs: Number(item?.carbs ?? 0),
    fat: Number(item?.fat ?? 0),
});

const directProductId = computed(() => toNumberSafe(pendingOrderRequest.value?.productId));
const directQuantity = computed(() => normalizeQuantity(pendingOrderRequest.value?.quantity));
const {
    product: directProduct,
    isLoading: isDirectLoading,
    errorMessage: directErrorMessage,
} = useProductDetail(directProductId);

const directItem = computed(() => {
    if (!directProduct.value?.id) return null;
    return {
        productId: directProduct.value.id,
        name: directProduct.value.name ?? '',
        category: directProduct.value.category ?? '',
        price: Number(directProduct.value.price) || 0,
        quantity: directQuantity.value,
        image: directProduct.value.image ?? '',
        calories: Number(directProduct.value.nutrition?.calories) || 0,
        protein: Number(directProduct.value.nutrition?.protein) || 0,
        carbs: Number(directProduct.value.nutrition?.carbs) || 0,
        fat: Number(directProduct.value.nutrition?.fat) || 0,
    };
});

const storedCartItems = computed(() => pendingCheckoutItems.value.map((item) => normalizeCheckoutItem(item)));
const requestedCartItemIds = computed(() => {
    const ids = pendingOrderRequest.value?.cartItemIds;
    return Array.isArray(ids) ? ids.filter((id) => Number.isFinite(Number(id))) : [];
});
const filteredCartItems = computed(() => {
    if (!requestedCartItemIds.value.length) return cartItems.value;
    const requestedSet = new Set(requestedCartItemIds.value.map((id) => Number(id)));
    return cartItems.value.filter((item) => requestedSet.has(Number(item.cartItemId)));
});
const cartCheckoutItems = computed(() =>
    storedCartItems.value.length ? storedCartItems.value : filteredCartItems.value,
);
const checkoutItems = computed(() =>
    orderMode.value === 'direct' ? (directItem.value ? [directItem.value] : []) : cartCheckoutItems.value,
);
const displayTotalPrice = computed(() => {
    if (orderMode.value === 'direct') {
        if (!directItem.value) return 0;
        return (directItem.value.price ?? 0) * (directItem.value.quantity ?? 0);
    }
    return checkoutItems.value.reduce((sum, item) => sum + (item.price ?? 0) * (item.quantity ?? 0), 0);
});
const totalPayment = computed(() => displayTotalPrice.value + shippingFee);
const isCheckoutLoading = computed(() => (orderMode.value === 'direct' ? isDirectLoading.value : isCartLoading.value));
const emptyItemsMessage = computed(() =>
    orderMode.value === 'direct'
        ? directErrorMessage.value || '결제된 상품 정보를 찾지 못했어요.'
        : '결제된 상품이 없어요.',
);

const canEditAddress = computed(() => canFreeCancel.value && !isCancelled.value);

const getStoredOrderPayload = (orderId) => {
    if (!orderId) return null;
    const stored = readPendingOrderRequest();
    if (!stored || stored.orderId !== orderId) return null;
    return {
        orderRequest: stored.orderRequest ?? null,
        checkoutItems: Array.isArray(stored.checkoutItems) ? stored.checkoutItems : [],
    };
};

const formattedSelectedAddress = computed(() => {
    if (!selectedAddress.value) return null;
    return {
        ...selectedAddress.value,
        name: selectedAddress.value.name ?? selectedAddress.value.label ?? '',
        addressLineDetail:
            selectedAddress.value.addressLineDetail ?? selectedAddress.value.detailAddress ?? '',
        memo: selectedAddress.value.memo ?? selectedAddress.value.instructions ?? '',
        postalCode: selectedAddress.value.postalCode ?? '',
        phone: formatPhoneNumber(selectedAddress.value.phone),
    };
});

const handleEditAddress = () => {
    if (!canEditAddress.value) return;
    window.alert('배송지 변경 기능을 곧 연결할게요. 지금은 고객센터로 요청해 주세요.');
};

const handleCancelRequest = () => {
    if (isCancelled.value) return;
    const confirmed = window.confirm('결제를 취소할까요? 취소하면 배송도 함께 중단돼요.');
    if (!confirmed) return;
    cancelOrder();
    window.alert('주문과 결제가 모두 취소되었어요.');
};

const navigateToProduct = (productId) => {
    if (!productId) return;
    router.push({name: 'product-detail', params: {id: productId}});
};

const redirectToOrderDetail = async (orderId) => {
    if (!orderId) return false;
    await router.replace({name: 'my-page-order-detail', params: {orderNumber: orderId}});
    return true;
};

const processPaymentResult = async () => {
    // [수정 전]
    // const orderIdFromQuery = route.query.orderId ? String(route.query.orderId) : '';
    const rawOrderId = route.query.orderId;
    const orderIdFromQuery = Array.isArray(rawOrderId) ? rawOrderId[0] : (rawOrderId || '');

    const paymentStatusFromQuery = route.query.paymentStatus;
    const hasSuccessParams = Boolean(route.query.paymentKey && route.query.amount !== undefined && orderIdFromQuery);

    if (paymentStatusFromQuery === 'fail') {
        await router.replace({
            name: 'order-checkout',
            query: {paymentStatus: 'fail', orderId: orderIdFromQuery},
        });
        return false;
    }

    if (!hasSuccessParams) {
        if (paymentStatusFromQuery === 'success') {
            confirmErrorMessage.value = '결제 정보를 찾지 못했어요. 다시 결제를 시작해 주세요.';
            await router.replace({
                name: 'order-checkout',
                query: {paymentStatus: 'fail', orderId: orderIdFromQuery},
            });
            return false;
        }
        if (orderIdFromQuery) {
            setOrderNumber(orderIdFromQuery);
        }
        return true;
    }

    try {
        const storedPayload = getStoredOrderPayload(orderIdFromQuery);
        const orderRequest = storedPayload?.orderRequest ?? null;
        pendingOrderRequest.value = orderRequest;
        pendingCheckoutItems.value = storedPayload?.checkoutItems ?? [];
        orderMode.value = normalizeOrderMode(orderRequest?.mode);
        const result = await confirmPaymentFromQuery(route.query, {orderRequest});
        paymentResult.value = result;
        const resolvedOrderId = result?.orderId ?? orderIdFromQuery;
        completePayment(resolvedOrderId);
        clearPendingOrderRequest();
        if (await redirectToOrderDetail(resolvedOrderId)) return false;
        return true;
    } catch (error) {
        console.error(error);
        await router.replace({
            name: 'order-checkout',
            query: {paymentStatus: 'fail', orderId: orderIdFromQuery},
        });
        return false;
    }
};

onMounted(() => {
    processPaymentResult()
        .then((isReady) => {
            if (!isReady) return;
            if (orderMode.value !== 'direct' && !pendingCheckoutItems.value.length) {
                loadCart({force: true}).catch((error) => console.error(error));
            }
            loadAddresses().catch((error) => {
                console.error(error);
            });
        })
        .catch((error) => console.error(error));
});
</script>
