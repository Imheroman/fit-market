import { fitmarket } from '@/restapi';

const unwrap = (response) => response?.data?.data ?? response?.data;

const buildError = (error, fallback) => {
  const message = error?.response?.data?.message ?? error?.response?.data?.error ?? fallback;
  const wrapped = new Error(message);
  wrapped.cause = error;
  return wrapped;
};

export async function confirmPayment({ paymentKey, orderId, amount, orderRequest }) {
  if (!paymentKey || !orderId || amount === undefined || amount === null) {
    throw new Error('결제 정보를 다시 불러오지 못했어요.');
  }

  const normalizedAmount = Number(amount);
  if (!Number.isFinite(normalizedAmount)) {
    throw new Error('결제 금액을 확인할 수 없어요.');
  }

  try {
    const requestBody = {
      paymentKey,
      orderId,
      amount: normalizedAmount,
      ...(orderRequest ? { orderRequest } : {}),
    };
    const response = await fitmarket.post('/payments/success', requestBody, { withCredentials: true });
    return unwrap(response);
  } catch (error) {
    throw buildError(error, '결제 확인에 실패했어요. 결제 상태를 다시 확인해 주세요.');
  }
}

export async function fetchPaymentFailure(params = {}) {
  try {
    const response = await fitmarket.get('/payments/fail', { params, withCredentials: true });
    return unwrap(response);
  } catch (error) {
    throw buildError(error, '결제 실패 사유를 불러오지 못했어요. 잠시 후 다시 시도해 주세요.');
  }
}
