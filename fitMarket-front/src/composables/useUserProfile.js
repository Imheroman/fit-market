import { reactive, ref, computed } from 'vue'
import { fetchUserProfile, updateUserProfile } from '@/api/userApi'
import { useAuth } from '@/composables/useAuth'

const createDefaultForm = () => ({
  name: '',
  email: '',
  phone: '',
  address: '',
  marketingConsent: false,
  notificationEmail: true,
  notificationSms: false,
})

export function useUserProfile() {
  const { hydrateProfile } = useAuth()

  const form = reactive(createDefaultForm())
  const errors = reactive({
    name: '',
    email: '',
    phone: '',
    address: '',
  })
  const isLoading = ref(false)
  const isSubmitting = ref(false)
  const successMessage = ref('')
  const serverError = ref('')
  const snapshot = ref(null)

  const resetErrors = () => {
    Object.keys(errors).forEach((key) => {
      errors[key] = ''
    })
  }

  const setFormValues = (profile) => {
    const nextValues = {
      ...createDefaultForm(),
      ...profile,
    }

    Object.keys(form).forEach((key) => {
      form[key] = nextValues[key]
    })
  }

  const loadProfile = async () => {
    isLoading.value = true;
    serverError.value = '';

    try {
      const profile = await fetchUserProfile();
      hydrateProfile(profile);
      setFormValues(profile);
      snapshot.value = JSON.parse(JSON.stringify(form));
    } catch (error) {
      console.error(error);
      serverError.value = error?.message ?? '회원 정보를 불러오지 못했어요.';
    } finally {
      isLoading.value = false;
    }
  };

  const isDirty = computed(() => {
    if (!snapshot.value) return false
    return JSON.stringify(form) !== JSON.stringify(snapshot.value)
  })

  const validate = () => {
    resetErrors()
    let isValid = true

    if (!form.name.trim()) {
      errors.name = '이름을 입력해주세요.'
      isValid = false
    }

    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!form.email.trim() || !emailPattern.test(form.email)) {
      errors.email = '올바른 이메일을 입력해주세요.'
      isValid = false
    }

    if (!form.phone.trim()) {
      errors.phone = '연락처를 입력해주세요.'
      isValid = false
    }

    if (!form.address.trim()) {
      errors.address = '주소를 입력해주세요.'
      isValid = false
    }

    return isValid
  }

  const submitProfile = async () => {
    if (!validate()) {
      successMessage.value = ''
      return null
    }

    isSubmitting.value = true
    serverError.value = ''
    successMessage.value = ''

    try {
      const updatedProfile = await updateUserProfile({ ...form })
      hydrateProfile(updatedProfile)
      snapshot.value = JSON.parse(JSON.stringify(form))
      successMessage.value = '변경사항을 저장했어요.'
      return updatedProfile
    } catch (error) {
      console.error(error)
      serverError.value = '저장에 실패했어요. 잠시 후 다시 시도해주세요.'
      return null
    } finally {
      isSubmitting.value = false
    }
  }

  const resetForm = () => {
    if (!snapshot.value) return
    setFormValues(snapshot.value)
    successMessage.value = ''
  }

  return {
    form,
    errors,
    isDirty,
    isLoading,
    isSubmitting,
    serverError,
    successMessage,
    loadProfile,
    submitProfile,
    resetForm,
  }
}
