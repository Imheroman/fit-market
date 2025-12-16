import { ref, computed, reactive } from 'vue'
import { formatPhoneNumber, sanitizePhoneDigits } from '@/utils/phone'
import {
  applySeller,
  fetchMySellerApplication,
  fetchSellerApplicationsByStatus,
  reviewSellerApplication,
} from '@/api/sellerApi'

const applications = ref([])
const myApplication = ref(null)

const createDefaultForm = () => ({
  businessName: '',
  businessNumber: '',
  businessType: 'individual',
  contactPhone: '',
  businessAddress: '',
  introduction: '',
})

export function useSellerApplication() {
  const form = reactive(createDefaultForm())
  const errors = reactive({
    businessName: '',
    businessNumber: '',
    businessType: '',
    contactPhone: '',
    businessAddress: '',
    introduction: '',
  })
  const isSubmitting = ref(false)
  const successMessage = ref('')
  const errorMessage = ref('')
  const handleContactPhoneInput = (value) => {
    form.contactPhone = formatPhoneNumber(value)
  }

  const resetErrors = () => {
    Object.keys(errors).forEach((key) => {
      errors[key] = ''
    })
  }

  const validate = () => {
    resetErrors()
    let isValid = true

    if (!form.businessName.trim()) {
      errors.businessName = '상호명을 입력해주세요.'
      isValid = false
    }

    const businessNumberPattern = /^\d{3}-\d{2}-\d{5}$/
    if (!form.businessNumber.trim() || !businessNumberPattern.test(form.businessNumber)) {
      errors.businessNumber = '올바른 사업자등록번호를 입력해주세요. (예: 123-45-67890)'
      isValid = false
    }

    if (!form.businessType) {
      errors.businessType = '사업자 유형을 선택해주세요.'
      isValid = false
    }

    const phoneDigits = sanitizePhoneDigits(form.contactPhone)
    if (!phoneDigits) {
      errors.contactPhone = '연락처를 입력해주세요.'
      isValid = false
    } else if (phoneDigits.length < 10) {
      errors.contactPhone = '연락처는 숫자 10~11자리로 입력해주세요. (예: 010-1234-5678)'
      isValid = false
    }

    if (!form.businessAddress.trim()) {
      errors.businessAddress = '사업장 주소를 입력해주세요.'
      isValid = false
    }

    if (!form.introduction.trim()) {
      errors.introduction = '사업 소개를 입력해주세요.'
      isValid = false
    } else if (form.introduction.trim().length < 20) {
      errors.introduction = '사업 소개는 최소 20자 이상 입력해주세요.'
      isValid = false
    }

    return isValid
  }

  const submitApplication = async () => {
    if (!validate()) {
      errorMessage.value = '입력 항목을 확인해주세요.'
      return false
    }

    isSubmitting.value = true
    errorMessage.value = ''
    successMessage.value = ''

    try {
      const payload = {
        businessName: form.businessName,
        businessNumber: form.businessNumber,
        businessType: form.businessType,
        contactPhone: sanitizePhoneDigits(form.contactPhone),
        businessAddress: form.businessAddress,
        introduction: form.introduction,
      }
      const response = await applySeller(payload)
      applications.value.unshift({
        ...response,
        appliedAt: response.createdDate,
      })
      successMessage.value = '판매자 신청이 완료되었습니다. 검토까지 1-2일 소요됩니다.'
      Object.assign(form, createDefaultForm())
      return true
    } catch (error) {
      console.error(error)
      errorMessage.value = error.message || '신청 중 오류가 발생했습니다. 다시 시도해주세요.'
      return false
    } finally {
      isSubmitting.value = false
    }
  }

  const loadMyApplication = async () => {
    try {
      const response = await fetchMySellerApplication()
      myApplication.value = {
        ...response,
        appliedAt: response.createdDate,
      }
      // 거절된 경우 재신청 폼에 기존 데이터를 채움
      if (myApplication.value?.status === 'rejected') {
        form.businessName = myApplication.value.businessName || ''
        form.businessNumber = myApplication.value.businessNumber || ''
        form.businessType = myApplication.value.businessType || 'individual'
        form.contactPhone = formatPhoneNumber(myApplication.value.contactPhone || '')
        form.businessAddress = myApplication.value.businessAddress || ''
        form.introduction = myApplication.value.introduction || ''
      }
    } catch (error) {
      // 신청 내역이 없으면 404가 될 수 있으므로 무시
      console.debug('No seller application found for current user')
      myApplication.value = null
    }
  }

  const resetForm = () => {
    Object.assign(form, createDefaultForm())
    resetErrors()
    successMessage.value = ''
    errorMessage.value = ''
  }

  return {
    form,
    errors,
    isSubmitting,
    successMessage,
    errorMessage,
    handleContactPhoneInput,
    submitApplication,
    loadMyApplication,
    myApplication,
    fetchMyApplication: fetchMySellerApplication,
    resetForm,
  }
}

export function useSellerApplicationsAdmin() {
  const pendingApplications = computed(() =>
    applications.value.filter((app) => app.status === 'pending')
  )
  const approvedApplications = computed(() =>
    applications.value.filter((app) => app.status === 'approved')
  )
  const rejectedApplications = computed(() =>
    applications.value.filter((app) => app.status === 'rejected')
  )

  const loadApplications = async () => {
    const pending = await fetchSellerApplicationsByStatus('pending')
    const approved = await fetchSellerApplicationsByStatus('approved')
    const rejected = await fetchSellerApplicationsByStatus('rejected')
    const normalize = (list) =>
      list.map((item) => ({
        ...item,
        appliedAt: item.createdDate,
      }))
    applications.value = [...normalize(pending), ...normalize(approved), ...normalize(rejected)]
  }

  const approveApplication = async (applicationId, note = '') => {
    await reviewSellerApplication(applicationId, 'approved', note)
    await loadApplications()
    return true
  }

  const rejectApplication = async (applicationId, note = '') => {
    await reviewSellerApplication(applicationId, 'rejected', note)
    await loadApplications()
    return true
  }

  return {
    applications,
    pendingApplications,
    approvedApplications,
    rejectedApplications,
    loadApplications,
    approveApplication,
    rejectApplication,
  }
}
