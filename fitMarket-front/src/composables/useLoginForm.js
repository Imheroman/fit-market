import { reactive, ref } from 'vue'
import { loginUser } from '@/api/authApi'
import { useAuth } from '@/composables/useAuth'

const createDefaultForm = () => ({
  email: '',
  password: '',
})

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

export function useLoginForm() {
  const { login } = useAuth()

  const form = reactive(createDefaultForm())
  const errors = reactive({
    email: '',
    password: '',
  })
  const isSubmitting = ref(false)
  const serverError = ref('')
  const successMessage = ref('')

  const resetErrors = () => {
    Object.keys(errors).forEach((key) => {
      errors[key] = ''
    })
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

    return isValid
  }

  const submitLogin = async () => {
    if (!validate()) {
      serverError.value = ''
      successMessage.value = ''
      return null
    }

    isSubmitting.value = true
    serverError.value = ''
    successMessage.value = ''

    try {
      const session = await loginUser({ ...form })
      login(session)
      successMessage.value = '안전하게 로그인되었어요.'
      return session
    } catch (error) {
      console.error(error)
      serverError.value = error.message ?? '로그인에 실패했어요. 잠시 후 다시 시도해주세요.'
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
    submitLogin,
  }
}
