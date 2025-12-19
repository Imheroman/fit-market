import { computed, ref } from 'vue';
import { fetchOrders } from '@/api/ordersApi';

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

const normalizeOrder = (order, index) => ({
  id: order?.orderNumber ?? `order-${index}`,
  orderNumber: order?.orderNumber ?? '',
  orderName: order?.orderName ?? '주문 상품',
  orderMode: order?.orderMode ?? 'CART',
  approvalStatus: order?.approvalStatus ?? 'pending_approval',
  paymentStatus: order?.paymentStatus ?? 'PENDING',
  totalAmount: Number(order?.totalAmount ?? 0),
  itemCount: Number(order?.itemCount ?? 0),
  orderedAt: order?.orderedAt ?? null,
  merchandiseAmount: order?.merchandiseAmount ?? null,
  shippingFee: order?.shippingFee ?? null,
  discountAmount: order?.discountAmount ?? null,
});

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
    orderHistory.value = response.map((order, index) => normalizeOrder(order, index));
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
    isLoading,
    errorMessage,
  };
}
