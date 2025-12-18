import { fitmarket } from '@/restapi';

const extractPayload = (response) => response?.data?.data ?? response?.data?.result ?? response?.data ?? [];

const buildError = (error, fallback) => {
  const message = error?.response?.data?.message ?? error?.response?.data?.error ?? fallback;
  const wrapped = new Error(message);
  wrapped.cause = error;
  return wrapped;
};

export async function fetchOrders(period) {
  try {
    const params = period && period !== 'ALL' ? { period } : undefined;
    const response = await fitmarket.get('/orders', { params, withCredentials: true });
    const payload = extractPayload(response);
    return Array.isArray(payload) ? payload : [];
  } catch (error) {
    throw buildError(error, '주문 내역을 불러오지 못했어요. 잠시 후 다시 시도해 주세요.');
  }
}

export async function fetchOrderDetail(orderNumber) {
  if (!orderNumber) {
    throw new Error('주문번호가 비어 있어요.');
  }

  try {
    const response = await fitmarket.get(`/orders/${orderNumber}`, { withCredentials: true });
    const payload = extractPayload(response);
    if (!payload || Array.isArray(payload)) {
      return null;
    }
    return payload;
  } catch (error) {
    throw buildError(error, '주문 상세 정보를 불러오지 못했어요. 잠시 후 다시 시도해 주세요.');
  }
}
