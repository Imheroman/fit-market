import { fitmarket } from '@/restapi';

const extractPayload = (response) => response?.data?.data ?? response?.data?.result ?? response?.data ?? [];
const buildClaimPayload = (type, payload) => ({
  type,
  reason: payload?.reason ?? payload?.reasonCode ?? '',
  detail: payload?.detail ?? '',
});

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

export async function requestOrderRefund(orderNumber) {
  if (!orderNumber) {
    throw new Error('주문번호가 비어 있어요.');
  }

  try {
    // TODO: 환불 요청 메소드와 URL을 전달받으면 수정해 주세요.
    const response = await fitmarket.post(`/orders/${orderNumber}/refund`, null, { withCredentials: true });
    const payload = extractPayload(response);
    return payload || null;
  } catch (error) {
    throw buildError(error, '환불 요청을 접수하지 못했어요. 잠시 후 다시 시도해 주세요.');
  }
}

export async function fetchRefundEligibility(orderNumber) {
  if (!orderNumber) {
    throw new Error('주문번호가 비어 있어요.');
  }

  try {
    const response = await fitmarket.get(`/orders/${orderNumber}/refund/eligibility`, { withCredentials: true });
    return extractPayload(response) ?? null;
  } catch (error) {
    throw buildError(error, '환불 가능 여부를 확인하지 못했어요. 잠시 후 다시 시도해 주세요.');
  }
}

export async function requestOrderReturn(orderNumber, payload) {
  if (!orderNumber) {
    throw new Error('주문번호가 비어 있어요.');
  }

  try {
    const response = await fitmarket.post(
      `/orders/${orderNumber}/return-exchange`,
      buildClaimPayload('RETURN', payload),
      { withCredentials: true },
    );
    const responsePayload = extractPayload(response);
    return responsePayload ?? null;
  } catch (error) {
    throw buildError(error, '반품 요청을 접수하지 못했어요. 잠시 후 다시 시도해 주세요.');
  }
}

export async function requestOrderExchange(orderNumber, payload) {
  if (!orderNumber) {
    throw new Error('주문번호가 비어 있어요.');
  }

  try {
    const response = await fitmarket.post(
      `/orders/${orderNumber}/return-exchange`,
      buildClaimPayload('EXCHANGE', payload),
      { withCredentials: true },
    );
    const responsePayload = extractPayload(response);
    return responsePayload ?? null;
  } catch (error) {
    throw buildError(error, '교환 요청을 접수하지 못했어요. 잠시 후 다시 시도해 주세요.');
  }
}

export async function requestOrderAddressChange(orderNumber, addressId) {
  if (!orderNumber) {
    throw new Error('주문번호가 비어 있어요.');
  }
  if (!addressId) {
    throw new Error('배송지를 선택해 주세요.');
  }

  try {
    // TODO: 배송지 변경 메소드와 URL을 전달받으면 수정해 주세요.
    const response = await fitmarket.patch(`/orders/${orderNumber}/address`, { addressId }, { withCredentials: true });
    const payload = extractPayload(response);
    return payload || null;
  } catch (error) {
    throw buildError(error, '배송지 변경 요청을 접수하지 못했어요. 잠시 후 다시 시도해 주세요.');
  }
}

export async function deleteOrder(orderNumber) {
  if (!orderNumber) {
    throw new Error('주문번호가 비어 있어요.');
  }

  try {
    // TODO: 주문 삭제 메소드와 URL을 전달받으면 수정해 주세요.
    await fitmarket.delete(`/orders/${orderNumber}`, { withCredentials: true });
    return true;
  } catch (error) {
    throw buildError(error, '주문 내역을 삭제하지 못했어요. 잠시 후 다시 시도해 주세요.');
  }
}
