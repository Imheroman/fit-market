import { ref } from 'vue';

const TOSS_SDK_URL = 'https://js.tosspayments.com/v2/standard';

let sdkLoadPromise = null;
let tossPaymentsInstance = null;

const loadTossSdk = () => {
  if (sdkLoadPromise) return sdkLoadPromise;

  sdkLoadPromise = new Promise((resolve, reject) => {
    if (window?.TossPayments) {
      resolve();
      return;
    }

    const script = document.createElement('script');
    script.src = TOSS_SDK_URL;
    script.async = true;
    script.onload = () => resolve();
    script.onerror = () => {
      sdkLoadPromise = null;
      reject(new Error('토스 결제 도구를 불러오지 못했어요.'));
    };
    document.head.appendChild(script);
  });

  return sdkLoadPromise;
};

const getTossPayments = async (clientKey) => {
  await loadTossSdk();

  if (!window?.TossPayments) {
    throw new Error('결제창을 준비하지 못했어요. 네트워크를 확인해 주세요.');
  }

  if (!tossPaymentsInstance) {
    tossPaymentsInstance = window.TossPayments(clientKey);
  }

  return tossPaymentsInstance;
};

export function useTossPayments(clientKey, customerKey) {
  const isReady = ref(false);
  const isRequesting = ref(false);
  const errorMessage = ref('');

  const prepareSdk = async () => {
    if (isReady.value) return true;
    try {
      await loadTossSdk();
      isReady.value = true;
      errorMessage.value = '';
      return true;
    } catch (error) {
      console.error(error);
      errorMessage.value = error?.message ?? '결제창을 준비하지 못했어요.';
      return false;
    }
  };

  const requestCardPayment = async (paymentPayload) => {
    if (!clientKey || !customerKey) {
      const error = new Error('결제 키 정보를 찾지 못했어요.');
      errorMessage.value = error.message;
      throw error;
    }

    isRequesting.value = true;
    errorMessage.value = '';

    try {
      const tossPayments = await getTossPayments(clientKey);
      const payment = tossPayments.payment({ customerKey });

      await payment.requestPayment({
        method: 'CARD',
        ...paymentPayload,
      });
    } catch (error) {
      console.error(error);
      const message = error?.message ?? '결제창을 열지 못했어요. 잠시 후 다시 시도해 주세요.';
      errorMessage.value = message;
      throw error;
    } finally {
      isRequesting.value = false;
    }
  };

  return {
    isReady,
    isRequesting,
    errorMessage,
    prepareSdk,
    requestCardPayment,
  };
}
