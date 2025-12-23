import { computed, ref } from 'vue';
import { updateUserPhone } from '@/api/userApi';
import { useAuth } from '@/composables/useAuth';
import { formatPhoneNumber, sanitizePhoneDigits } from '@/utils/phone';

export function useChangePhone() {
  const { hydrateProfile } = useAuth();

  const phone = ref('');
  const initialPhone = ref('');
  const error = ref('');
  const serverError = ref('');
  const successMessage = ref('');
  const isSubmitting = ref(false);

  const isDirty = computed(() => phone.value.trim() !== initialPhone.value.trim());

  const setInitialValue = (value = '') => {
    const formatted = formatPhoneNumber(value ?? '');
    phone.value = formatted;
    initialPhone.value = formatted;
    error.value = '';
    serverError.value = '';
    successMessage.value = '';
  };

  const validate = () => {
    error.value = '';
    const digits = sanitizePhoneDigits(phone.value);
    if (!digits) {
      error.value = '연락처를 입력해주세요.';
      return false;
    }
    if (digits.length < 10 || digits.length > 11) {
      error.value = '연락처는 10~11자리 숫자로 입력해주세요.';
      return false;
    }
    return true;
  };

  const submit = async () => {
    if (!validate()) {
      successMessage.value = '';
      return null;
    }

    isSubmitting.value = true;
    serverError.value = '';
    successMessage.value = '';

    try {
      const normalized = sanitizePhoneDigits(phone.value);
      const formatted = formatPhoneNumber(normalized);
      const response = await updateUserPhone(normalized);
      const profile = response?.profile ?? response ?? null;
      const messageFromApi = response?.message ?? '';
      const message = messageFromApi
        ? messageFromApi.includes(normalized)
          ? messageFromApi.replace(normalized, formatted)
          : messageFromApi
        : `${formatted}으로 저장했어요.`;
      hydrateProfile(profile);
      setInitialValue(profile?.phone ?? normalized);
      successMessage.value = message;

      return profile;
    } catch (errorResponse) {
      serverError.value = errorResponse?.message ?? '연락처를 바꾸지 못했어요.';
      return null;
    } finally {
      isSubmitting.value = false;
    }
  };

  const reset = () => {
    phone.value = initialPhone.value;
    error.value = '';
    serverError.value = '';
    successMessage.value = '';
  };

  const formatPhone = (value) => formatPhoneNumber(value);

  const clear = () => {
    phone.value = '';
    error.value = '';
    serverError.value = '';
    successMessage.value = '';
  };

  return {
    phone,
    error,
    serverError,
    successMessage,
    isSubmitting,
    isDirty,
    setInitialValue,
    submit,
    reset,
    formatPhone,
    clear,
  };
}
