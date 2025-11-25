import { ref, computed } from 'vue'

const savedAddresses = ref([
  {
    id: 1,
    label: '집',
    recipient: '김영웅',
    phone: '010-1234-5678',
    addressLine: '서울 강남구 테헤란로 123',
    detailAddress: '101동 1203호',
    instructions: '문 앞에 두고 초인종은 누르지 말아주세요.',
    isDefault: true,
  },
  {
    id: 2,
    label: '회사',
    recipient: '김영웅',
    phone: '010-1234-5678',
    addressLine: '서울 송파구 중대로 55',
    detailAddress: '15층 피트마켓 팀',
    instructions: '리셉션에 맡겨주세요.',
    isDefault: false,
  },
  {
    id: 3,
    label: '부모님 댁',
    recipient: '김영웅 부모님',
    phone: '031-123-4567',
    addressLine: '경기 성남시 분당구 정자일로 88',
    detailAddress: '101동 902호',
    instructions: '관리실에 보관 후 연락 부탁드려요.',
    isDefault: false,
  },
  {
    id: 4,
    label: '헬스장',
    recipient: '김영웅',
    phone: '010-9876-5432',
    addressLine: '서울 마포구 양화로 188',
    detailAddress: '지하 2층 PT데스크',
    instructions: '데스크 직원에게 전달 후 문자 부탁드려요.',
    isDefault: false,
  },
])

const selectedAddressId = ref(savedAddresses.value.find((addr) => addr.isDefault)?.id ?? null)

export function useAddresses() {
  const addresses = computed(() => savedAddresses.value)

  const defaultAddress = computed(() => {
    return savedAddresses.value.find((addr) => addr.isDefault) ?? savedAddresses.value[0] ?? null
  })

  const selectedAddress = computed(() => {
    const target = savedAddresses.value.find((addr) => addr.id === selectedAddressId.value)
    return target ?? defaultAddress.value
  })

  const selectAddress = (addressId) => {
    const exists = savedAddresses.value.some((addr) => addr.id === addressId)
    if (exists) {
      selectedAddressId.value = addressId
    }
  }

  const setDefaultAddress = (addressId) => {
    const target = savedAddresses.value.find((addr) => addr.id === addressId)
    if (!target) return

    savedAddresses.value = savedAddresses.value.map((addr) => ({
      ...addr,
      isDefault: addr.id === addressId,
    }))

    selectedAddressId.value = addressId
  }

  const removeAddress = (addressId) => {
    if (savedAddresses.value.length <= 1) return
    savedAddresses.value = savedAddresses.value.filter((addr) => addr.id !== addressId)
    if (selectedAddressId.value === addressId) {
      const nextDefault = savedAddresses.value.find((addr) => addr.isDefault) ?? savedAddresses.value[0] ?? null
      selectedAddressId.value = nextDefault?.id ?? null
    }
  }

  return {
    addresses,
    defaultAddress,
    selectedAddress,
    selectedAddressId,
    selectAddress,
    setDefaultAddress,
    removeAddress,
  }
}
