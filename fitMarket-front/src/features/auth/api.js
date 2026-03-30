import { formatPhoneNumber, sanitizePhoneDigits } from '@/utils/phone';
import { fitmarket } from '@/lib/axios';

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

const extractPayload = (response) =>
  response?.data?.data ?? response?.data?.result ?? response?.data;

const buildErrorMessage = (error, fallback) => {
  if (error?.response?.data?.message) return error.response.data.message;
  if (error?.response?.data?.error) return error.response.data.error;
  return fallback;
};

export async function loginUser(payload) {
  if (!payload) {
    throw new Error('로그인 정보를 전달받지 못했어요.')
  }

  const { email, password } = payload

  if (!email?.trim() || !emailPattern.test(email)) {
    throw new Error('등록된 이메일을 다시 확인해 주세요.')
  }

  if (!password?.trim()) {
    throw new Error('비밀번호를 입력해주세요.')
  }

  try {
    const response = await fitmarket.post('/auth/login', {
      email: email.trim(),
      password,
    });

    const session = extractPayload(response);
    if (!session) {
      throw new Error('로그인 결과를 불러오지 못했어요. 잠시 후 다시 시도해주세요.');
    }
    return session;
  } catch (error) {
    if (error.message && !error.response) throw error;
    const message = buildErrorMessage(error, '로그인에 실패했어요. 입력한 정보를 다시 확인해주세요.');
    throw new Error(message);
  }
}

export async function registerUser(payload) {
  if (!payload) {
    throw new Error('회원가입 정보를 확인할 수 없어요.')
  }

  const { email, password, name, phone } = payload
  const trimmedEmail = email?.trim() ?? ''
  const trimmedName = name?.trim() ?? ''

  if (!trimmedEmail || !emailPattern.test(trimmedEmail)) {
    throw new Error('올바른 이메일을 입력해주세요.')
  }
  if (!password?.trim()) {
    throw new Error('비밀번호를 입력해주세요.')
  }
  if (!trimmedName) {
    throw new Error('이름을 입력해주세요.')
  }

  const normalizedPhone = sanitizePhoneDigits(phone ?? '')
  if (normalizedPhone.length < 10) {
    throw new Error('휴대폰 번호를 정확하게 입력해주세요.')
  }

  try {
    const response = await fitmarket.post('/users/signup', {
      email: trimmedEmail,
      password,
      name: trimmedName,
      phone: normalizedPhone,
    });

    const createdUser = extractPayload(response) ?? {
      email: trimmedEmail,
      name: trimmedName,
      phone: formatPhoneNumber(phone),
    };
    return createdUser;
  } catch (error) {
    if (error.message && !error.response) throw error;
    const message = buildErrorMessage(error, '회원가입에 실패했어요. 입력한 정보를 다시 확인해주세요.');
    throw new Error(message);
  }
}

export async function logoutUser() {
  try {
    const response = await fitmarket.post('/logout');
    return response?.data ?? { success: true };
  } catch (error) {
    const message = buildErrorMessage(error, '로그아웃에 실패했어요. 잠시 후 다시 시도해주세요.');
    const wrappedError = new Error(message);
    wrappedError.cause = error;
    throw wrappedError;
  }
}

export { formatPhoneNumber }
