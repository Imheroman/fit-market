import { ref } from 'vue';
import { confirmPayment, fetchPaymentFailure } from '@/api/paymentApi';

const normalizeParam = (value) => (Array.isArray(value) ? value[0] : value);

const toStringSafe = (value) => {
  const normalized = normalizeParam(value);
  return normalized === undefined || normalized === null ? '' : String(normalized);
};

const toNumberSafe = (value) => {
  const normalized = normalizeParam(value);
  const parsed = Number(normalized);
  return Number.isFinite(parsed) ? parsed : NaN;
};

export function usePaymentCallbacks() {
  const isConfirming = ref(false);
  const isLoadingFailure = ref(false);
  const confirmErrorMessage = ref('');
  const failureErrorMessage = ref('');
  const failureGuide = ref(null);

  const confirmPaymentFromQuery = async (query, options = {}) => {
    const paymentKey = toStringSafe(query?.paymentKey);
    const orderId = toStringSafe(query?.orderId);
    const amount = toNumberSafe(query?.amount);

    if (!paymentKey || !orderId || Number.isNaN(amount)) {
      throw new Error('결제 완료 정보를 불러오지 못했어요.');
    }

    isConfirming.value = true;
    confirmErrorMessage.value = '';

    try {
      const payload = await confirmPayment({
        paymentKey,
        orderId,
        amount,
        orderRequest: options.orderRequest,
      });
      return payload;
    } catch (error) {
      confirmErrorMessage.value =
        error?.message ?? '결제 확인 중 문제가 발생했어요. 잠시 후 다시 시도해 주세요.';
      throw error;
    } finally {
      isConfirming.value = false;
    }
  };

  const loadFailureGuide = async (query) => {
    const params = {
      errorCode: toStringSafe(query?.errorCode),
      errorMessage: toStringSafe(query?.errorMessage),
      code: toStringSafe(query?.code),
      message: toStringSafe(query?.message),
      orderId: toStringSafe(query?.orderId),
    };

    isLoadingFailure.value = true;
    failureErrorMessage.value = '';

    try {
      const payload = await fetchPaymentFailure(params);
      failureGuide.value = {
        orderId: payload?.orderId ?? params.orderId ?? '',
        errorCode: payload?.errorCode ?? payload?.code ?? params.errorCode ?? params.code ?? '',
        message:
          payload?.message ??
          payload?.errorMessage ??
          params.errorMessage ??
          params.message ??
          '',
        guide: payload?.guide ?? '',
      };
      return failureGuide.value;
    } catch (error) {
      failureErrorMessage.value =
        error?.message ??
        '결제 실패 사유를 불러오지 못했어요. 다시 시도해 주세요.';
      failureGuide.value = null;
      throw error;
    } finally {
      isLoadingFailure.value = false;
    }
  };

  const hasFailureParams = (query) =>
    Boolean(query?.errorCode || query?.errorMessage || query?.code || query?.message);

  return {
    isConfirming,
    isLoadingFailure,
    confirmErrorMessage,
    failureErrorMessage,
    failureGuide,
    confirmPaymentFromQuery,
    loadFailureGuide,
    hasFailureParams,
  };
}
