import { computed, ref } from 'vue';
import { fetchOrderDetail, fetchOrders } from '@/api/ordersApi';

const filterOptions = [
  { label: '전체', value: 'ALL' },
  { label: '1개월', value: '1M' },
  { label: '3개월', value: '3M' },
  { label: '6개월', value: '6M' },
  { label: '1년', value: '1Y' },
];

const orderHistory = ref([]);
const selectedRange = ref(filterOptions[2].value);
const isLoading = ref(false);
const errorMessage = ref('');
const hasLoaded = ref(false);

const normalizeReturnExchange = (payload) => {
  if (!payload || typeof payload !== 'object') return null;
  return {
    type: payload?.type ? String(payload.type).toUpperCase() : '',
    status: payload?.status ? String(payload.status).toUpperCase() : '',
    requestedAt: payload?.requestedAt ?? null,
    processedAt: payload?.processedAt ?? null,
  };
};

const normalizeOrder = (order, index) => ({
  id: order?.orderNumber ?? `order-${index}`,
  orderNumber: order?.orderNumber ?? '',
  orderName: order?.orderName ?? '주문 상품',
  orderMode: order?.orderMode ?? 'CART',
  approvalStatus: order?.approvalStatus ?? 'pending_approval',
  paymentStatus: order?.paymentStatus ?? 'PENDING',
  returnExchange: normalizeReturnExchange(order?.returnExchange),
  totalAmount: Number(order?.totalAmount ?? 0),
  itemCount: Number(order?.itemCount ?? 0),
  orderedAt: order?.orderedAt ?? null,
  merchandiseAmount: order?.merchandiseAmount ?? null,
  shippingFee: order?.shippingFee ?? null,
  discountAmount: order?.discountAmount ?? null,
});

const shouldFetchOrderDetail = (order) => {
  if (!order?.orderNumber) return false;
  const claimType = order?.returnExchange?.type ?? '';
  const claimStatus = order?.returnExchange?.status ?? '';
  return !(claimType && claimStatus);
};

const hydrateOrderClaims = async (orders) => {
  const tasks = orders.map(async (order) => {
    if (!shouldFetchOrderDetail(order)) return order;
    try {
      const detail = await fetchOrderDetail(order.orderNumber);
      if (!detail?.returnExchange) return order;
      return {
        ...order,
        returnExchange: normalizeReturnExchange(detail.returnExchange),
      };
    } catch (error) {
      console.error(error);
      return order;
    }
  });

  return Promise.all(tasks);
};

const filteredOrders = computed(() => orderHistory.value);

const filterDescription = computed(() => {
  const option = filterOptions.find((opt) => opt.value === selectedRange.value);
  if (!option || option.value === 'ALL') return '전체 주문 내역을 보여드려요.';
  return `${option.label} 주문만 모아봤어요.`;
});

const loadOrders = async (period = selectedRange.value) => {
  if (isLoading.value) return;

  isLoading.value = true;
  errorMessage.value = '';

  try {
    const response = await fetchOrders(period);
    const normalizedOrders = response.map((order, index) => normalizeOrder(order, index));
    orderHistory.value = await hydrateOrderClaims(normalizedOrders);
    hasLoaded.value = true;
  } catch (error) {
    console.error(error);
    errorMessage.value = error?.message ?? '주문 내역을 불러오지 못했어요.';
    orderHistory.value = [];
    throw error;
  } finally {
    isLoading.value = false;
  }
};

const setFilter = async (value) => {
  const exists = filterOptions.some((opt) => opt.value === value);
  if (exists) {
    selectedRange.value = value;
    await loadOrders(value);
  }
};

const removeOrderByNumber = (orderNumber) => {
  if (!orderNumber) return;
  orderHistory.value = orderHistory.value.filter((order) => order.orderNumber !== orderNumber);
};

const ensureLoaded = () => {
  if (!hasLoaded.value && !isLoading.value) {
    loadOrders();
  }
};

export function useOrderHistory() {
  ensureLoaded();

  return {
    orders: orderHistory,
    filterOptions,
    selectedRange,
    filteredOrders,
    filterDescription,
    setFilter,
    loadOrders,
    removeOrderByNumber,
    isLoading,
    errorMessage,
  };
}
