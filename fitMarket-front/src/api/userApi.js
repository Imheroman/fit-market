import { fitmarket } from '@/restapi';

const extractUserPayload = (response) => response?.data?.data ?? response?.data?.result ?? response?.data;

const buildErrorMessage = (error, fallback) => {
  if (error?.response?.data?.message) return error.response.data.message;
  if (error?.response?.data?.error) return error.response.data.error;
  return fallback;
};

export async function fetchUserProfile() {
  try {
    const response = await fitmarket.get('/users', { withCredentials: true });
    const profile = extractUserPayload(response);

    if (!profile) {
      throw new Error('회원 정보를 불러오지 못했어요. 잠시 후 다시 시도해 주세요.');
    }

    return profile;
  } catch (error) {
    const message = buildErrorMessage(error, '회원 정보를 불러오지 못했어요. 다시 시도해 주세요.');
    const wrappedError = new Error(message);
    wrappedError.cause = error;
    throw wrappedError;
  }
}

export async function updateUserProfile(payload) {
  try {
    const response = await fitmarket.put('/users', payload, { withCredentials: true });
    const profile = extractUserPayload(response);

    if (!profile) {
      throw new Error('수정된 정보를 확인할 수 없어요. 다시 시도해 주세요.');
    }

    return profile;
  } catch (error) {
    const message = buildErrorMessage(error, '회원 정보 수정에 실패했어요. 잠시 후 다시 시도해 주세요.');
    const wrappedError = new Error(message);
    wrappedError.cause = error;
    throw wrappedError;
  }
}

export async function updateUserName(name) {
  try {
    const response = await fitmarket.patch('/users/name', { value: name }, { withCredentials: true });
    const profile = extractUserPayload(response);

    return profile ?? { name };
  } catch (error) {
    const message = buildErrorMessage(error, '이름을 바꾸지 못했어요. 잠시 후 다시 시도해 주세요.');
    const wrappedError = new Error(message);
    wrappedError.cause = error;
    throw wrappedError;
  }
}

export async function updateUserPhone(phone) {
  try {
    const response = await fitmarket.patch('/users/phone', { value: phone }, { withCredentials: true });
    const profile = extractUserPayload(response);

    if (!profile) {
      throw new Error('연락처 변경 결과를 확인할 수 없어요. 다시 시도해 주세요.');
    }

    return profile;
  } catch (error) {
    const message = buildErrorMessage(error, '연락처를 바꾸지 못했어요. 잠시 후 다시 시도해 주세요.');
    const wrappedError = new Error(message);
    wrappedError.cause = error;
    throw wrappedError;
  }
}

export async function updateUserPassword(payload) {
  try {
    const response = await fitmarket.patch('/users/password', {value: payload.newPassword}, { withCredentials: true });
    return extractUserPayload(response) ?? { success: true };
  } catch (error) {
    const message = buildErrorMessage(error, '비밀번호를 바꾸지 못했어요. 잠시 후 다시 시도해 주세요.');
    const wrappedError = new Error(message);
    wrappedError.cause = error;
    throw wrappedError;
  }
}

export async function deleteUserAccount() {
  try {
    const response = await fitmarket.delete('/users', { withCredentials: true });
    return response?.data ?? { success: true };
  } catch (error) {
    const message = buildErrorMessage(error, '회원 탈퇴에 실패했어요. 잠시 후 다시 시도해 주세요.');
    const wrappedError = new Error(message);
    wrappedError.cause = error;
    throw wrappedError;
  }
}
