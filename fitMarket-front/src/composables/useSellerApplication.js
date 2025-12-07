import { ref, computed, reactive } from 'vue'
import { formatPhoneNumber, sanitizePhoneDigits } from '@/utils/phone'

// Mock 판매자 신청 데이터
const applications = ref([
  {
    id: 1,
    userId: 2,
    userName: '이상인',
    email: 'lee.sangin@example.com',
    businessName: '건강한 밥상',
    businessNumber: '123-45-67890',
    businessType: 'individual',
    contactPhone: '010-2345-6789',
    businessAddress: '서울시 마포구 성미산로 123',
    introduction: '신선한 재료로 건강한 도시락을 만듭니다.',
    status: 'pending',
    appliedAt: '2025-01-20T10:30:00+09:00',
    reviewedAt: null,
    reviewNote: '',
  },
  {
    id: 2,
    userId: 3,
    userName: '박건강',
    email: 'park.health@example.com',
    businessName: '파워밀 키친',
    businessNumber: '234-56-78901',
    businessType: 'corporation',
    contactPhone: '010-3456-7890',
    businessAddress: '경기도 성남시 분당구 판교역로 234',
    introduction: '고단백 저칼로리 식단 전문 업체입니다.',
    status: 'approved',
    appliedAt: '2025-01-15T14:20:00+09:00',
    reviewedAt: '2025-01-16T09:15:00+09:00',
    reviewNote: '검토 완료, 승인되었습니다.',
  },
])

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
      await new Promise((resolve) => setTimeout(resolve, 500))

      const newApplication = {
        id: applications.value.length + 1,
        userId: 1,
        userName: '김영웅',
        email: 'kim.youngwoong@example.com',
        ...form,
        status: 'pending',
        appliedAt: new Date().toISOString(),
        reviewedAt: null,
        reviewNote: '',
      }

      applications.value.unshift(newApplication)
      successMessage.value = '판매자 신청이 완료되었습니다. 검토까지 1-2일 소요됩니다.'
      Object.assign(form, createDefaultForm())
      return true
    } catch (error) {
      console.error(error)
      errorMessage.value = '신청 중 오류가 발생했습니다. 다시 시도해주세요.'
      return false
    } finally {
      isSubmitting.value = false
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
    resetForm,
  }
}

export function useSellerApplicationsAdmin() {
  const pendingApplications = computed(() => {
    return applications.value.filter((app) => app.status === 'pending')
  })

  const approvedApplications = computed(() => {
    return applications.value.filter((app) => app.status === 'approved')
  })

  const rejectedApplications = computed(() => {
    return applications.value.filter((app) => app.status === 'rejected')
  })

  const approveApplication = async (applicationId, note = '') => {
    try {
      await new Promise((resolve) => setTimeout(resolve, 300))
      const app = applications.value.find((a) => a.id === applicationId)
      if (app) {
        app.status = 'approved'
        app.reviewedAt = new Date().toISOString()
        app.reviewNote = note || '검토 완료, 승인되었습니다.'
      }
      return true
    } catch (error) {
      console.error(error)
      return false
    }
  }

  const rejectApplication = async (applicationId, note = '') => {
    try {
      await new Promise((resolve) => setTimeout(resolve, 300))
      const app = applications.value.find((a) => a.id === applicationId)
      if (app) {
        app.status = 'rejected'
        app.reviewedAt = new Date().toISOString()
        app.reviewNote = note || '승인 거부되었습니다.'
      }
      return true
    } catch (error) {
      console.error(error)
      return false
    }
  }

  return {
    applications,
    pendingApplications,
    approvedApplications,
    rejectedApplications,
    approveApplication,
    rejectApplication,
  }
}
