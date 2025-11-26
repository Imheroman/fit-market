import { computed, reactive, ref } from 'vue'
import { registerUser, formatPhoneNumber } from '@/api/authApi'

const createDefaultForm = () => ({
  email: '',
  password: '',
  confirmPassword: '',
  name: '',
  phone: '',
})

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

export function useRegisterForm() {
  const form = reactive(createDefaultForm())
  const errors = reactive({
    email: '',
    password: '',
    confirmPassword: '',
    name: '',
    phone: '',
  })
  const isSubmitting = ref(false)
  const serverError = ref('')
  const successMessage = ref('')
  const passwordMismatchMessage = computed(() => {
    if (!form.password || !form.confirmPassword) {
      return ''
    }
    if (form.password !== form.confirmPassword) {
      return '비밀번호가 일치하지 않아요. 동일하게 입력해주세요.'
    }
    return ''
  })
  const isSubmitDisabled = computed(() => isSubmitting.value || Boolean(passwordMismatchMessage.value))

  const resetErrors = () => {
    Object.keys(errors).forEach((key) => {
      errors[key] = ''
    })
  }

  const handlePhoneInput = (value) => {
    form.phone = formatPhoneNumber(value)
  }

  const validate = () => {
    resetErrors()
    let isValid = true

    if (!form.email.trim() || !emailPattern.test(form.email)) {
      errors.email = '올바른 이메일을 입력해주세요.'
      isValid = false
    }

    if (!form.password.trim() || form.password.length < 8) {
      errors.password = '비밀번호는 8자 이상 입력해주세요.'
      isValid = false
    }

    if (!form.confirmPassword.trim()) {
      errors.confirmPassword = '비밀번호 확인을 입력해주세요.'
      isValid = false
    } else if (form.password !== form.confirmPassword) {
      errors.confirmPassword = '비밀번호가 일치하지 않아요. 동일하게 입력해주세요.'
      isValid = false
    }

    if (!form.name.trim()) {
      errors.name = '이름을 입력해주세요.'
      isValid = false
    }

    const digits = form.phone.replace(/[^0-9]/g, '')
    if (digits.length < 10) {
      errors.phone = '휴대폰 번호를 정확하게 입력해주세요.'
      isValid = false
    }

    return isValid
  }

  const submitRegister = async () => {
    if (!validate()) {
      serverError.value = ''
      successMessage.value = ''
      return null
    }

    isSubmitting.value = true
    serverError.value = ''
    successMessage.value = ''

    try {
      const { email, password, name, phone } = form
      const createdUser = await registerUser({ email, password, name, phone })
      successMessage.value = '회원가입이 완료되었어요.'
      return createdUser
    } catch (error) {
      console.error(error)
      serverError.value = error.message ?? '회원가입에 실패했어요. 잠시 후 다시 시도해주세요.'
      return null
    } finally {
      isSubmitting.value = false
    }
  }

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
  }
}
