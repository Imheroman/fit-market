import { computed, ref } from 'vue';
import { fetchAddresses, createAddress, updateAddress, deleteAddress } from '@/api/addressApi';
import { sanitizePhoneDigits } from '@/utils/phone';

const savedAddresses = ref([]);
const selectedAddressId = ref(null);
const isLoading = ref(false);
const isMutating = ref(false);
const errorMessage = ref('');
const hasLoaded = ref(false);

const normalizeAddress = (address) => ({
  id: address?.id,
  label: address?.label ?? '배송지',
  recipient: address?.recipient ?? '',
  phone: sanitizePhoneDigits(address?.phone ?? ''),
  addressLine: address?.addressLine ?? '',
  detailAddress: address?.detailAddress ?? '',
  instructions: address?.instructions ?? '',
  isDefault: Boolean(address?.isDefault),
});

const findDefaultId = (list) => list.find((addr) => addr.isDefault)?.id ?? null;

const markDefault = (list, defaultId) => {
  if (!defaultId) return list;
  return list.map((addr) => ({
    ...addr,
    isDefault: addr.id === defaultId,
  }));
};

const syncSelection = () => {
  const defaultId = findDefaultId(savedAddresses.value);
  if (defaultId) {
    selectedAddressId.value = defaultId;
    return;
  }

  const fallbackId = savedAddresses.value[0]?.id ?? null;
  selectedAddressId.value = fallbackId;
};

const loadAddresses = async () => {
  if (isLoading.value) return;

  isLoading.value = true;
  errorMessage.value = '';

  try {
    const response = await fetchAddresses();
    const normalized = response.map((item) => normalizeAddress(item));
    const defaultId = findDefaultId(normalized) ?? normalized[0]?.id ?? null;
    savedAddresses.value = markDefault(normalized, defaultId);
    syncSelection();
    hasLoaded.value = true;
  } catch (error) {
    console.error(error);
    errorMessage.value = error?.message ?? '배송지를 불러오지 못했어요.';
    savedAddresses.value = [];
    selectedAddressId.value = null;
    throw error;
  } finally {
    isLoading.value = false;
  }
};

const ensureLoaded = () => {
  if (!hasLoaded.value && !isLoading.value) {
    loadAddresses();
  }
};

const addAddress = async (payload) => {
  isMutating.value = true;
  errorMessage.value = '';

  try {
    const created = await createAddress(payload);
    const normalized = normalizeAddress(created);
    const nextDefaultId = normalized.isDefault
      ? normalized.id
      : findDefaultId(savedAddresses.value) ?? normalized.id;

    const nextList = markDefault([...savedAddresses.value, normalized], nextDefaultId);
    savedAddresses.value = nextList;
    syncSelection();
    return normalized;
  } catch (error) {
    console.error(error);
    errorMessage.value = error?.message ?? '배송지 추가에 실패했어요.';
    throw error;
  } finally {
    isMutating.value = false;
  }
};

const editAddress = async (addressId, payload) => {
  if (!addressId) return null;

  isMutating.value = true;
  errorMessage.value = '';

  try {
    const updated = await updateAddress(addressId, payload);
    const normalized = normalizeAddress(updated);
    const nextList = savedAddresses.value.map((addr) => (addr.id === addressId ? normalized : addr));
    const nextDefaultId = normalized.isDefault
      ? normalized.id
      : findDefaultId(nextList) ?? nextList[0]?.id ?? null;

    savedAddresses.value = markDefault(nextList, nextDefaultId);
    syncSelection();
    return normalized;
  } catch (error) {
    console.error(error);
    errorMessage.value = error?.message ?? '배송지 수정에 실패했어요.';
    throw error;
  } finally {
    isMutating.value = false;
  }
};

const setDefaultAddress = async (addressId) => {
  if (!addressId) return;

  isMutating.value = true;
  errorMessage.value = '';

  try {
    const updated = await updateAddress(addressId, { isDefault: true });
    const normalized = normalizeAddress(updated);
    const nextList = savedAddresses.value.map((addr) =>
      addr.id === addressId ? normalized : { ...addr, isDefault: false }
    );

    savedAddresses.value = markDefault(nextList, addressId);
    syncSelection();
    return normalized;
  } catch (error) {
    console.error(error);
    errorMessage.value = error?.message ?? '기본 배송지 설정에 실패했어요.';
    throw error;
  } finally {
    isMutating.value = false;
  }
};

const removeAddress = async (addressId) => {
  if (!addressId) return;
  if (savedAddresses.value.length <= 1) {
    errorMessage.value = '배송지는 최소 1개 이상 필요해요.';
    return;
  }

  isMutating.value = true;
  errorMessage.value = '';

  try {
    await deleteAddress(addressId);
    const nextList = savedAddresses.value.filter((addr) => addr.id !== addressId);
    const nextDefaultId = findDefaultId(nextList) ?? nextList[0]?.id ?? null;

    savedAddresses.value = markDefault(nextList, nextDefaultId);
    syncSelection();
  } catch (error) {
    console.error(error);
    errorMessage.value = error?.message ?? '배송지 삭제에 실패했어요.';
    throw error;
  } finally {
    isMutating.value = false;
  }
};

export function useAddresses() {
  ensureLoaded();

  const addresses = computed(() => savedAddresses.value);

  const defaultAddress = computed(() => savedAddresses.value.find((addr) => addr.isDefault) ?? null);

  const selectedAddress = computed(() => {
    const target = savedAddresses.value.find((addr) => addr.id === selectedAddressId.value);
    return target ?? defaultAddress.value ?? savedAddresses.value[0] ?? null;
  });

  const selectAddress = (addressId) => {
    const exists = savedAddresses.value.some((addr) => addr.id === addressId);
    if (exists) {
      selectedAddressId.value = addressId;
    }
  };

  return {
    addresses,
    defaultAddress,
    selectedAddress,
    selectedAddressId,
    selectAddress,
    loadAddresses,
    addAddress,
    editAddress,
    setDefaultAddress,
    removeAddress,
    isLoading,
    isMutating,
    errorMessage,
  };
}
