import { fitmarket } from '@/restapi';

const extractPayload = (response) => response?.data?.data ?? response?.data?.result ?? response?.data;

const resolveErrorMessage = (error, fallback) => {
  if (error?.response?.data?.message) return error.response.data.message;
  if (error?.response?.data?.error) return error.response.data.error;
  return fallback;
};

export async function fetchAddresses() {
  try {
    const response = await fitmarket.get('/addresses', { withCredentials: true });
    const payload = extractPayload(response);
    return Array.isArray(payload) ? payload : [];
  } catch (error) {
    const message = resolveErrorMessage(error, '배송지를 불러오지 못했어요. 잠시 후 다시 시도해 주세요.');
    const wrapped = new Error(message);
    wrapped.cause = error;
    throw wrapped;
  }
}

export async function createAddress(payload) {
  try {
    const response = await fitmarket.post('/addresses', payload, { withCredentials: true });
    const created = extractPayload(response);
    if (created) {
      return created;
    }

    const isSuccessStatus = response?.status >= 200 && response.status < 300;
    if (isSuccessStatus) {
      return { success: true };
    }

    throw new Error('새 배송지 정보를 확인할 수 없어요.');
  } catch (error) {
    const message = resolveErrorMessage(error, '배송지 추가에 실패했어요. 입력값을 확인하고 다시 시도해 주세요.');
    const wrapped = new Error(message);
    wrapped.cause = error;
    throw wrapped;
  }
}

export async function updateAddress(addressId, payload) {
  if (!addressId) {
    throw new Error('수정할 배송지 정보를 찾지 못했어요.');
  }

  try {
    const response = await fitmarket.patch(`/addresses/${addressId}`, payload, { withCredentials: true });
    const updated = extractPayload(response);
    if (updated) {
      return updated;
    }
    return { id: addressId, ...payload };
  } catch (error) {
    const message = resolveErrorMessage(error, '배송지 수정에 실패했어요. 잠시 후 다시 시도해 주세요.');
    const wrapped = new Error(message);
    wrapped.cause = error;
    throw wrapped;
  }
}

export async function deleteAddress(addressId) {
  if (!addressId) {
    throw new Error('삭제할 배송지를 찾지 못했어요.');
  }

  try {
    const response = await fitmarket.delete(`/addresses/${addressId}`, { withCredentials: true });
    return extractPayload(response) ?? { success: true };
  } catch (error) {
    const message = resolveErrorMessage(error, '배송지 삭제에 실패했어요. 잠시 후 다시 시도해 주세요.');
    const wrapped = new Error(message);
    wrapped.cause = error;
    throw wrapped;
  }
}

export async function setMainAddress(addressId) {
  if (!addressId) {
    throw new Error('기본 배송지로 설정할 주소를 찾지 못했어요.');
  }

  try {
    const response = await fitmarket.patch(`/addresses/${addressId}/main`, null, { withCredentials: true });
    return extractPayload(response) ?? { success: true };
  } catch (error) {
    const message = resolveErrorMessage(error, '기본 배송지 설정에 실패했어요. 잠시 후 다시 시도해 주세요.');
    const wrapped = new Error(message);
    wrapped.cause = error;
    throw wrapped;
  }
}
