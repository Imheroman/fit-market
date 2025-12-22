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

export async function fetchSellerOrders(period) {
  try {
    const params = period && period !== 'ALL' ? { period } : undefined;
    const response = await fitmarket.get('/seller/orders', { params, withCredentials: true });
    const payload = extractPayload(response);
    return Array.isArray(payload) ? payload : [];
  } catch (error) {
    throw buildError(error, '판매자 주문 내역을 불러오지 못했어요. 잠시 후 다시 시도해 주세요.');
  }
}

export async function fetchSellerOrderDetail(orderNumber) {
  if (!orderNumber) {
    throw new Error('주문번호가 비어 있어요.');
  }

  try {
    const response = await fitmarket.get(`/seller/orders/${orderNumber}`, { withCredentials: true });
    const payload = extractPayload(response);
    if (!payload || Array.isArray(payload)) {
      return null;
    }
    return payload;
  } catch (error) {
    throw buildError(error, '판매자 주문 상세 정보를 불러오지 못했어요. 잠시 후 다시 시도해 주세요.');
  }
}

export async function updateOrderStatus(orderNumber, approvalStatus) {
  if (!orderNumber) {
    throw new Error('주문번호가 비어 있어요.');
  }
  if (!approvalStatus) {
    throw new Error('변경할 주문 상태가 없어요.');
  }

  try {
    const response = await fitmarket.patch(
      `/orders/${orderNumber}/status`,
      { approvalStatus },
      { withCredentials: true },
    );
    return response?.data ?? null;
  } catch (error) {
    throw buildError(error, '주문 상태를 변경하지 못했어요. 잠시 후 다시 시도해 주세요.');
  }
}

export async function updateSellerOrderStatus(orderNumber, approvalStatus) {
  if (!orderNumber) {
    throw new Error('주문번호가 비어 있어요.');
  }
  if (!approvalStatus) {
    throw new Error('변경할 주문 상태가 없어요.');
  }

  try {
    const response = await fitmarket.patch(
      `/seller/orders/${orderNumber}/status`,
      { approvalStatus },
      { withCredentials: true },
    );
    return response?.data ?? null;
  } catch (error) {
    throw buildError(error, '판매자 주문 상태를 변경하지 못했어요. 잠시 후 다시 시도해 주세요.');
  }
}
