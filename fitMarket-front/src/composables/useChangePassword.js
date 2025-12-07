import { ref } from 'vue';
import { updateUserPassword } from '@/api/userApi';

const createDefaultErrors = () => ({
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
});

export function useChangePassword() {
  const currentPassword = ref('');
  const newPassword = ref('');
  const confirmPassword = ref('');
  const errors = ref(createDefaultErrors());
  const serverError = ref('');
  const successMessage = ref('');
  const isSubmitting = ref(false);

  const resetErrors = () => {
    errors.value = createDefaultErrors();
  };

  const validate = () => {
    resetErrors();
    let valid = true;

    if (!currentPassword.value) {
      errors.value.currentPassword = '현재 비밀번호를 입력해주세요.';
      valid = false;
    }

    if (!newPassword.value) {
      errors.value.newPassword = '새 비밀번호를 입력해주세요.';
      valid = false;
    } else if (newPassword.value.length < 8) {
      errors.value.newPassword = '8자 이상 입력해주세요.';
      valid = false;
    } else if (newPassword.value.length > 16) {
      errors.value.newPassword = '비밀번호는 최대 16자까지 입력할 수 있어요.';
      valid = false;
    } else if (!/[0-9]/.test(newPassword.value) || !/[A-Za-z]/.test(newPassword.value)) {
      errors.value.newPassword = '영문과 숫자를 모두 포함해주세요.';
      valid = false;
    }

    if (newPassword.value && newPassword.value === currentPassword.value) {
      errors.value.newPassword = '현재 비밀번호와 다르게 설정해주세요.';
      valid = false;
    }

    if (!confirmPassword.value) {
      errors.value.confirmPassword = '한 번 더 입력해주세요.';
      valid = false;
    } else if (confirmPassword.value !== newPassword.value) {
      errors.value.confirmPassword = '새 비밀번호와 일치하지 않아요.';
      valid = false;
    }

    return valid;
  };

  const resetForm = () => {
    currentPassword.value = '';
    newPassword.value = '';
    confirmPassword.value = '';
    resetErrors();
    serverError.value = '';
    successMessage.value = '';
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
      const result = await updateUserPassword({
        currentPassword: currentPassword.value,
        newPassword: newPassword.value,
      });
      resetForm();
      successMessage.value = '새 비밀번호로 잠갔어요.';
      return result;
    } catch (errorResponse) {
      serverError.value = errorResponse?.message ?? '비밀번호를 바꾸지 못했어요.';
      return null;
    } finally {
      isSubmitting.value = false;
    }
  };

  return {
    currentPassword,
    newPassword,
    confirmPassword,
    errors,
    serverError,
    successMessage,
    isSubmitting,
    submit,
    resetForm,
  };
}
