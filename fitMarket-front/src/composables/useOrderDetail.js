import { onMounted, ref, watch } from 'vue';
import { fetchOrderDetail } from '@/api/ordersApi';

const normalizeAddress = (address) => ({
  id: address?.id ?? null,
  name: address?.name ?? '',
  recipient: address?.recipient ?? '',
  phone: address?.phone ?? '',
  postalCode: address?.postalCode ?? '',
  addressLine: address?.addressLine ?? '',
  addressLineDetail: address?.addressLineDetail ?? '',
  memo: address?.memo ?? '',
});

const normalizeItem = (item, index) => ({
  id: item?.productId ?? `item-${index}`,
  productId: item?.productId ?? null,
  productName: item?.productName ?? '상품',
  quantity: Number(item?.quantity ?? 0),
  unitPrice: Number(item?.unitPrice ?? 0),
  totalPrice: Number(item?.totalPrice ?? 0),
});

const normalizeOrderDetail = (order) => ({
  orderNumber: order?.orderNumber ?? '',
  orderMode: order?.orderMode ?? 'CART',
  approvalStatus: order?.approvalStatus ?? 'pending_approval',
  paymentStatus: order?.paymentStatus ?? 'PENDING',
  orderName: order?.orderName ?? '주문 상품',
  totalAmount: Number(order?.totalAmount ?? 0),
  merchandiseAmount: Number(order?.merchandiseAmount ?? 0),
  shippingFee: Number(order?.shippingFee ?? 0),
  discountAmount: Number(order?.discountAmount ?? 0),
  orderedAt: order?.orderedAt ?? null,
  comment: order?.comment ?? '',
  refundable: typeof order?.refundable === 'boolean' ? order.refundable : null,
  returnable: typeof order?.returnable === 'boolean' ? order.returnable : null,
  exchangeable: typeof order?.exchangeable === 'boolean' ? order.exchangeable : null,
  address: normalizeAddress(order?.address),
  items: Array.isArray(order?.items) ? order.items.map((item, index) => normalizeItem(item, index)) : [],
});

export function useOrderDetail(orderNumber) {
  const orderDetail = ref(null);
  const isLoading = ref(false);
  const errorMessage = ref('');

  const loadOrderDetail = async (value) => {
    const targetOrderNumber = value ?? orderNumber?.value ?? orderNumber;
    if (!targetOrderNumber) {
      orderDetail.value = null;
      errorMessage.value = '주문번호가 없어요.';
      return;
    }

    if (isLoading.value) return;

    isLoading.value = true;
    errorMessage.value = '';

    try {
      const response = await fetchOrderDetail(targetOrderNumber);
      if (!response) {
        orderDetail.value = null;
        errorMessage.value = '주문 정보를 찾지 못했어요.';
        return;
      }
      orderDetail.value = normalizeOrderDetail(response);
    } catch (error) {
      console.error(error);
      orderDetail.value = null;
      errorMessage.value = error?.message ?? '주문 정보를 불러오지 못했어요.';
    } finally {
      isLoading.value = false;
    }
  };

  onMounted(() => loadOrderDetail());

  watch(
    () => orderNumber?.value ?? orderNumber,
    (newValue) => {
      loadOrderDetail(newValue);
    },
  );

  return {
    orderDetail,
    isLoading,
    errorMessage,
    loadOrderDetail,
  };
}
