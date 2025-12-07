import { computed, ref } from 'vue';
import { updateUserName } from '@/api/userApi';
import { useAuth } from '@/composables/useAuth';

const namePattern = /^[A-Za-z가-힣\s]+$/;
const MAX_NAME_LENGTH = 30;

export function useChangeName() {
  const { hydrateProfile } = useAuth();

  const name = ref('');
  const initialName = ref('');
  const error = ref('');
  const serverError = ref('');
  const successMessage = ref('');
  const isSubmitting = ref(false);

  const isDirty = computed(() => name.value.trim() !== initialName.value.trim());

  const setInitialValue = (value = '') => {
    const next = typeof value === 'string' ? value.trim() : '';
    name.value = next;
    initialName.value = next;
    error.value = '';
    serverError.value = '';
    successMessage.value = '';
  };

  const validate = () => {
    error.value = '';
    if (!name.value.trim()) {
      error.value = '이름을 입력해주세요.';
      return false;
    }
    const trimmed = name.value.trim();
    if (trimmed.length < 2) {
      error.value = '이름은 두 글자 이상 입력해주세요.';
      return false;
    }
    if (trimmed.length > MAX_NAME_LENGTH) {
      error.value = '이름은 최대 30자까지 입력할 수 있어요.';
      return false;
    }
    if (!namePattern.test(trimmed)) {
      error.value = '이름은 한글과 영어만 사용할 수 있어요.';
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
      const profile = await updateUserName(name.value.trim());
      hydrateProfile(profile);
      setInitialValue(profile?.name ?? name.value.trim());
      successMessage.value = '새 이름으로 저장했어요.';
      return profile;
    } catch (errorResponse) {
      serverError.value = errorResponse?.message ?? '이름을 바꾸지 못했어요.';
      return null;
    } finally {
      isSubmitting.value = false;
    }
  };

  const reset = () => {
    name.value = initialName.value;
    error.value = '';
    serverError.value = '';
    successMessage.value = '';
  };

  return {
    name,
    error,
    serverError,
    successMessage,
    isSubmitting,
    isDirty,
    setInitialValue,
    submit,
    reset,
  };
}
