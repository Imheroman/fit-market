import { reactive, ref } from 'vue';
import { loginUser } from '@/api/authApi';
import { useAuth } from '@/composables/useAuth';

const createDefaultForm = () => ({
  email: '',
  password: '',
});

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

const resolveLoginErrorMessage = (error) => {
  const message = error?.message ?? '';
  if (message.toLowerCase().includes('unauthorized')) {
    return '로그인 정보가 올바르지 않습니다. 다시 시도해주세요.';
  }
  return message || '로그인에 실패했어요. 잠시 후 다시 시도해주세요.';
};

export function useLoginForm() {
  const { login, loadUserProfile } = useAuth();

  const form = reactive(createDefaultForm());
  const errors = reactive({
    email: '',
    password: '',
  });
  const isSubmitting = ref(false);
  const serverError = ref('');
  const successMessage = ref('');

  const resetErrors = () => {
    Object.keys(errors).forEach((key) => {
      errors[key] = '';
    });
  };

  const validate = () => {
    resetErrors();
    let isValid = true;

    if (!form.email.trim() || !emailPattern.test(form.email)) {
      errors.email = '올바른 이메일을 입력해주세요.';
      isValid = false;
    }

    if (!form.password.trim()) {
      errors.password = '비밀번호를 입력해주세요.';
      isValid = false;
    }

    return isValid;
  };

  const submitLogin = async () => {
    if (!validate()) {
      serverError.value = '';
      successMessage.value = '';
      return null;
    }

    isSubmitting.value = true;
    serverError.value = '';
    successMessage.value = '';

    try {
      const session = await loginUser({ ...form });
      login(session);
      try {
        await loadUserProfile();
      } catch (error) {
        console.warn('Failed to load user profile after login', error);
      }
      successMessage.value = '안전하게 로그인되었어요.';
      return session;
    } catch (error) {
      console.error(error);
      serverError.value = resolveLoginErrorMessage(error);
      return null;
    } finally {
      isSubmitting.value = false;
    }
  };

  return {
    form,
    errors,
    isSubmitting,
    serverError,
    successMessage,
    submitLogin,
  };
}
