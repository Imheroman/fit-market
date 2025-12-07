import { computed, reactive, ref } from 'vue';
import { registerUser } from '@/api/authApi';
import { formatPhoneNumber, sanitizePhoneDigits } from '@/utils/phone';

const createDefaultForm = () => ({
  email: '',
  password: '',
  confirmPassword: '',
  name: '',
  phone: '',
});

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const namePattern = /^[A-Za-z가-힣\s]+$/;
const hasLetterAndNumber = (value = '') => /[A-Za-z]/.test(value) && /[0-9]/.test(value);

export function useRegisterForm() {
  const form = reactive(createDefaultForm());
  const errors = reactive({
    email: '',
    password: '',
    confirmPassword: '',
    name: '',
    phone: '',
  });
  const isSubmitting = ref(false);
  const serverError = ref('');
  const successMessage = ref('');
  const passwordMismatchMessage = computed(() => {
    if (!form.password || !form.confirmPassword) {
      return '';
    }
    if (form.password !== form.confirmPassword) {
      return '비밀번호가 일치하지 않아요. 동일하게 입력해주세요.';
    }
    return '';
  });
  const isSubmitDisabled = computed(() => isSubmitting.value || Boolean(passwordMismatchMessage.value));

  const resetErrors = () => {
    Object.keys(errors).forEach((key) => {
      errors[key] = '';
    });
  };

  const handlePhoneInput = (value) => {
    form.phone = formatPhoneNumber(value);
  };

  const validate = () => {
    resetErrors();
    let isValid = true;
    const trimmedEmail = form.email.trim();
    const trimmedName = form.name.trim();
    const digits = sanitizePhoneDigits(form.phone);

    if (!trimmedEmail || !emailPattern.test(trimmedEmail)) {
      errors.email = '올바른 이메일을 입력해주세요.';
      isValid = false;
    }

    if (!form.password.trim()) {
      errors.password = '비밀번호를 입력해주세요.';
      isValid = false;
    } else if (form.password.length < 8 || form.password.length > 16 || !hasLetterAndNumber(form.password)) {
      errors.password = '비밀번호는 영문과 숫자를 포함해 8~16자로 입력해주세요.';
      isValid = false;
    }

    if (!form.confirmPassword.trim()) {
      errors.confirmPassword = '비밀번호 확인을 입력해주세요.';
      isValid = false;
    } else if (form.password !== form.confirmPassword) {
      errors.confirmPassword = '비밀번호가 일치하지 않아요. 동일하게 입력해주세요.';
      isValid = false;
    }

    if (!trimmedName) {
      errors.name = '이름을 입력해주세요.';
      isValid = false;
    } else if (trimmedName.length < 2 || trimmedName.length > 30) {
      errors.name = '이름은 한글 또는 영문 2~30자까지 입력해주세요.';
      isValid = false;
    } else if (!namePattern.test(trimmedName)) {
      errors.name = '이름은 한글과 영어만 사용할 수 있어요.';
      isValid = false;
    }

    if (!digits) {
      errors.phone = '휴대폰 번호를 입력해주세요.';
      isValid = false;
    } else if (digits.length < 10) {
      errors.phone = '휴대폰 번호를 정확하게 입력해주세요.';
      isValid = false;
    }

    return isValid;
  };

  const submitRegister = async () => {
    if (!validate()) {
      serverError.value = '';
      successMessage.value = '';
      return null;
    }

    isSubmitting.value = true;
    serverError.value = '';
    successMessage.value = '';

    try {
      const { email, password, name, phone } = form;
      const createdUser = await registerUser({
        email,
        password,
        name: name.trim(),
        phone: sanitizePhoneDigits(phone),
      });
      const safeEmail = createdUser?.email ?? email.trim();
      successMessage.value = '회원가입이 완료되었어요.';
      return {
        ...createdUser,
        email: safeEmail,
      };
    } catch (error) {
      console.error(error);
      serverError.value = error.message ?? '회원가입에 실패했어요. 잠시 후 다시 시도해주세요.';
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
    passwordMismatchMessage,
    isSubmitDisabled,
    handlePhoneInput,
    submitRegister,
  };
}
