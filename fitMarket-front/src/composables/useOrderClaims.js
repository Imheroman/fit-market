import { computed, ref, watch } from 'vue';
import { fetchRefundEligibility, requestOrderExchange, requestOrderRefund, requestOrderReturn } from '@/api/ordersApi';

const REFUND_BLOCK_MESSAGE = '환불은 결제 후 3일 이내이고 배송 전일 때만 가능해요.';
const CLAIM_BLOCK_MESSAGE = '반품과 교환은 주문 후 7일 이내에 신청할 수 있어요.';
const CLAIM_DELIVERY_MESSAGE = '배송이 완료된 뒤에 반품이나 교환을 신청할 수 있어요.';

const CLAIM_REASONS = [
  { value: 'CHANGE_OF_MIND', label: '단순 변심', placeholder: '어떤 점이 아쉬웠나요?' },
  { value: 'DEFECTIVE', label: '상품 파손', placeholder: '파손된 상태를 자세히 알려주세요.' },
  { value: 'WRONG_ITEM', label: '오배송', placeholder: '받은 상품 정보를 알려주세요.' },
  { value: 'QUALITY_ISSUE', label: '품질 문제', placeholder: '어떤 문제가 있었나요?' },
  { value: 'OTHER', label: '기타', placeholder: '상황을 자세히 알려주세요.' },
];

const resolveStatus = (error) => {
  if (error?.response?.status) return error.response.status;
  if (error?.cause?.response?.status) return error.cause.response.status;
  if (error?.status) return error.status;
  if (error?.cause?.status) return error.cause.status;
  return null;
};

const resolveEligibility = (payload) => {
  if (typeof payload === 'boolean') {
    return { available: payload, message: '' };
  }
  if (!payload || typeof payload !== 'object') {
    return { available: true, message: '' };
  }
  const available =
    payload.available ??
    payload.refundable ??
    payload.eligible ??
    payload.isAvailable ??
    payload.canRefund ??
    payload.canExchange ??
    payload.canReturn;
  return {
    available: typeof available === 'boolean' ? available : true,
    message: payload.message ?? payload.reason ?? payload.note ?? '',
  };
};

const resolveOrderNumber = (orderNumber) => orderNumber?.value ?? orderNumber ?? '';
const resolveOrderDetail = (detail) => detail?.value ?? detail ?? null;

const buildClaimPayload = (reason, detail) => ({
  reason,
  detail: detail.trim(),
});

export function useOrderClaims(orderNumber, orderDetail) {
  const orderFlags = computed(() => {
    const detail = resolveOrderDetail(orderDetail);
    const resolveFlag = (key) => (typeof detail?.[key] === 'boolean' ? detail[key] : null);
    return {
      refundable: resolveFlag('refundable'),
      returnable: resolveFlag('returnable'),
      exchangeable: resolveFlag('exchangeable'),
    };
  });
  const isDelivered = computed(() => {
    const detail = resolveOrderDetail(orderDetail);
    return (detail?.approvalStatus ?? '') === 'delivered';
  });
  const isClaimLocked = computed(() => {
    if (isRefundRequested.value) return true;
    const { refundable, returnable, exchangeable } = orderFlags.value;
    const flags = [refundable, returnable, exchangeable];
    const hasFlags = flags.some((flag) => typeof flag === 'boolean');
    if (!hasFlags) return false;
    return flags.every((flag) => flag === false);
  });
  const refundEligibility = ref({ available: false, message: '' });
  const isRefundEligibilityLoading = ref(false);
  const isRefundEligibilityChecked = ref(false);
  const isRefunding = ref(false);
  const isRefundRequested = ref(false);
  const refundMessage = ref('');
  const refundError = ref('');

  const claimEligibility = ref({ available: false, message: '' });
  const isClaimEligibilityChecked = ref(false);
  const returnReason = ref('');
  const returnDetail = ref('');
  const exchangeReason = ref('');
  const exchangeDetail = ref('');
  const isReturnSubmitting = ref(false);
  const isExchangeSubmitting = ref(false);
  const returnMessage = ref('');
  const returnError = ref('');
  const exchangeMessage = ref('');
  const exchangeError = ref('');

  const fallbackRefundEligible = computed(() => {
    const detail = resolveOrderDetail(orderDetail);
    if (!detail?.orderedAt) return false;
    const orderedAt = new Date(detail.orderedAt).getTime();
    if (!Number.isFinite(orderedAt)) return false;
    const diffDays = (Date.now() - orderedAt) / (1000 * 60 * 60 * 24);
    if (diffDays < 0 || diffDays > 3) return false;
    const approvalStatus = detail.approvalStatus ?? '';
    const paymentStatus = detail.paymentStatus ?? '';
    if (paymentStatus === 'REFUNDED' || approvalStatus === 'cancelled') return false;
    return !['shipping', 'delivered'].includes(approvalStatus);
  });

  const fallbackClaimEligible = computed(() => {
    const detail = resolveOrderDetail(orderDetail);
    if (!detail?.orderedAt) return false;
    const orderedAt = new Date(detail.orderedAt).getTime();
    if (!Number.isFinite(orderedAt)) return false;
    const diffDays = (Date.now() - orderedAt) / (1000 * 60 * 60 * 24);
    return diffDays >= 0 && diffDays <= 7;
  });

  const canRefund = computed(() => {
    const flag = orderFlags.value.refundable;
    if (typeof flag === 'boolean') return flag;
    return isRefundEligibilityChecked.value ? refundEligibility.value.available : fallbackRefundEligible.value;
  });

  const refundBlockMessage = computed(() => {
    if (!canRefund.value) {
      return refundEligibility.value.message || REFUND_BLOCK_MESSAGE;
    }
    return '';
  });

  const baseClaimEligible = computed(() =>
    isClaimEligibilityChecked.value ? claimEligibility.value.available : fallbackClaimEligible.value,
  );
  const canReturn = computed(() => {
    const flag = orderFlags.value.returnable;
    if (typeof flag === 'boolean') return flag && isDelivered.value;
    return isDelivered.value && baseClaimEligible.value;
  });
  const canExchange = computed(() => {
    const flag = orderFlags.value.exchangeable;
    if (typeof flag === 'boolean') return flag && isDelivered.value;
    return isDelivered.value && baseClaimEligible.value;
  });
  const canRequestClaim = computed(() => canReturn.value || canExchange.value);

  const claimBlockMessage = computed(() => {
    if (canReturn.value || canExchange.value) {
      return '';
    }
    if (!isDelivered.value) {
      return CLAIM_DELIVERY_MESSAGE;
    }
    if (!baseClaimEligible.value) {
      return claimEligibility.value.message || CLAIM_BLOCK_MESSAGE;
    }
    return claimEligibility.value.message || CLAIM_BLOCK_MESSAGE;
  });

  const isReturnFormValid = computed(() => Boolean(returnReason.value && returnDetail.value.trim()));
  const isExchangeFormValid = computed(() => Boolean(exchangeReason.value && exchangeDetail.value.trim()));

  const returnDetailPlaceholder = computed(() => {
    const matched = CLAIM_REASONS.find((reason) => reason.value === returnReason.value);
    return matched?.placeholder ?? '상황을 자세히 알려주세요.';
  });

  const exchangeDetailPlaceholder = computed(() => {
    const matched = CLAIM_REASONS.find((reason) => reason.value === exchangeReason.value);
    return matched?.placeholder ?? '필요한 정보를 알려주세요.';
  });

  const loadRefundEligibility = async () => {
    const targetOrderNumber = resolveOrderNumber(orderNumber);
    if (!targetOrderNumber || isRefundEligibilityLoading.value) return;

    isRefundEligibilityLoading.value = true;

    try {
      const response = await fetchRefundEligibility(targetOrderNumber);
      refundEligibility.value = resolveEligibility(response);
    } catch (error) {
      if (resolveStatus(error) === 400) {
        refundEligibility.value = { available: false, message: REFUND_BLOCK_MESSAGE };
      } else {
        refundEligibility.value = { available: false, message: error?.message ?? REFUND_BLOCK_MESSAGE };
      }
    } finally {
      isRefundEligibilityLoading.value = false;
      isRefundEligibilityChecked.value = true;
    }
  };

  const applyClaimEligibility = (response) => {
    if (!response || typeof response !== 'object' || !('eligible' in response)) return;
    claimEligibility.value = resolveEligibility(response);
    isClaimEligibilityChecked.value = true;
  };

  const requestRefund = async () => {
    const targetOrderNumber = resolveOrderNumber(orderNumber);
    if (!targetOrderNumber || isRefunding.value) return false;

    refundMessage.value = '';
    refundError.value = '';

    if (!canRefund.value) {
      refundError.value = refundBlockMessage.value || REFUND_BLOCK_MESSAGE;
      return false;
    }

    isRefunding.value = true;

    try {
      await requestOrderRefund(targetOrderNumber);
      refundMessage.value = '환불 요청을 접수했어요. 진행 상황은 주문 상태에서 확인해 주세요.';
      isRefundRequested.value = true;
      return true;
    } catch (error) {
      if (resolveStatus(error) === 400) {
        refundError.value = '환불이 불가능해요. 결제 후 3일 이내이고 배송 전일 때만 가능해요.';
      } else {
        refundError.value = error?.message ?? '환불 요청을 접수하지 못했어요. 잠시 후 다시 시도해 주세요.';
      }
      return false;
    } finally {
      isRefunding.value = false;
    }
  };

  const requestReturn = async () => {
    const targetOrderNumber = resolveOrderNumber(orderNumber);
    if (!targetOrderNumber || isReturnSubmitting.value) return false;

    returnMessage.value = '';
    returnError.value = '';

    if (!canReturn.value) {
      returnError.value = claimBlockMessage.value || '반품 신청이 어려워요.';
      return false;
    }
    if (!isReturnFormValid.value) {
      returnError.value = '반품 사유와 상세 내용을 입력해 주세요.';
      return false;
    }

    isReturnSubmitting.value = true;

    try {
      const response = await requestOrderReturn(
        targetOrderNumber,
        buildClaimPayload(returnReason.value, returnDetail.value),
      );
      applyClaimEligibility(response);
      returnMessage.value = '반품 신청을 접수했어요. 진행 상황은 주문 상태에서 확인해 주세요.';
      return true;
    } catch (error) {
      returnError.value = error?.message ?? '반품 신청을 접수하지 못했어요. 잠시 후 다시 시도해 주세요.';
      return false;
    } finally {
      isReturnSubmitting.value = false;
    }
  };

  const requestExchange = async () => {
    const targetOrderNumber = resolveOrderNumber(orderNumber);
    if (!targetOrderNumber || isExchangeSubmitting.value) return false;

    exchangeMessage.value = '';
    exchangeError.value = '';

    if (!canExchange.value) {
      exchangeError.value = claimBlockMessage.value || '교환 신청이 어려워요.';
      return false;
    }
    if (!isExchangeFormValid.value) {
      exchangeError.value = '교환 사유와 상세 내용을 입력해 주세요.';
      return false;
    }

    isExchangeSubmitting.value = true;

    try {
      const response = await requestOrderExchange(
        targetOrderNumber,
        buildClaimPayload(exchangeReason.value, exchangeDetail.value),
      );
      applyClaimEligibility(response);
      exchangeMessage.value = '교환 신청을 접수했어요. 진행 상황은 주문 상태에서 확인해 주세요.';
      return true;
    } catch (error) {
      exchangeError.value = error?.message ?? '교환 신청을 접수하지 못했어요. 잠시 후 다시 시도해 주세요.';
      return false;
    } finally {
      isExchangeSubmitting.value = false;
    }
  };

  watch(
    () => resolveOrderNumber(orderNumber),
    (value) => {
      if (!value) return;
      isRefundRequested.value = false;
      loadRefundEligibility();
    },
    { immediate: true },
  );

  return {
    claimReasons: CLAIM_REASONS,
    returnReason,
    returnDetail,
    exchangeReason,
    exchangeDetail,
    returnDetailPlaceholder,
    exchangeDetailPlaceholder,
    canRefund,
    refundBlockMessage,
    isRefunding,
    refundMessage,
    refundError,
    canRequestClaim,
    claimBlockMessage,
    canReturn,
    canExchange,
    isDelivered,
    isClaimLocked,
    isReturnSubmitting,
    isExchangeSubmitting,
    isReturnFormValid,
    isExchangeFormValid,
    returnMessage,
    returnError,
    exchangeMessage,
    exchangeError,
    requestRefund,
    requestReturn,
    requestExchange,
    loadRefundEligibility,
  };
}
