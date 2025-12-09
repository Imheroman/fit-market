import { computed, ref } from 'vue';
import { fetchAddresses, createAddress, updateAddress, deleteAddress, setMainAddress } from '@/api/addressApi';
import { sanitizePhoneDigits } from '@/utils/phone';

const savedAddresses = ref([]);
const selectedAddressId = ref(null);
const isLoading = ref(false);
const isMutating = ref(false);
const errorMessage = ref('');
const hasLoaded = ref(false);

const buildAddressPayload = (payload) => {
  const phoneDigits = sanitizePhoneDigits(payload?.phone ?? '').slice(0, 11);
  const body = {
    name: (payload?.name ?? '').trim(),
    recipient: (payload?.recipient ?? '').trim(),
    phone: phoneDigits,
    memo: (payload?.memo ?? '').trim(),
    postalCode: (payload?.postalCode ?? '').trim(),
    addressLine: (payload?.addressLine ?? '').trim(),
    addressLineDetail: (payload?.addressLineDetail ?? '').trim(),
    main: Boolean(payload?.main),
  };

  return body;
};

const normalizeAddress = (address) => ({
  id: address?.id,
  name: address?.name ?? '',
  recipient: address?.recipient ?? '',
  phone: sanitizePhoneDigits(address?.phone ?? ''),
  memo: address?.memo ?? '',
  postalCode: address?.postalCode ?? '',
  addressLine: address?.addressLine ?? '',
  addressLineDetail: address?.addressLineDetail ?? '',
  main: Boolean(address?.main ?? false),
  createdDate: address?.createdDate ?? null,
  modifiedDate: address?.modifiedDate ?? null,
  deletedDate: address?.deletedDate ?? null,
});

const findMainId = (list) => list.find((addr) => addr.main)?.id ?? null;

const markMain = (list, mainId) => {
  if (!mainId) return list;
  return list.map((addr) => ({
    ...addr,
    main: addr.id === mainId,
  }));
};

const syncSelection = () => {
  const mainId = findMainId(savedAddresses.value);
  if (mainId) {
    selectedAddressId.value = mainId;
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
    console.log("normalized addresses:", normalized);
    const mainId = findMainId(normalized) ?? normalized[0]?.id ?? null;
    savedAddresses.value = markMain(normalized, mainId);
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
    const requestBody = buildAddressPayload(payload);
    const created = await createAddress(requestBody);
    const normalized = normalizeAddress(created);
    const nextMainId = normalized.main
      ? normalized.id
      : findMainId(savedAddresses.value) ?? normalized.id;

    const nextList = markMain([...savedAddresses.value, normalized], nextMainId);
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
    const requestBody = buildAddressPayload(payload);
    const updated = await updateAddress(addressId, requestBody);
    const normalized = normalizeAddress(updated);
    const nextList = savedAddresses.value.map((addr) => (addr.id === addressId ? normalized : addr));
    const nextMainId = normalized.main
      ? normalized.id
      : findMainId(nextList) ?? nextList[0]?.id ?? null;

    savedAddresses.value = markMain(nextList, nextMainId);
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

  const targetExists = savedAddresses.value.some((addr) => addr.id === addressId);
  if (!targetExists) {
    throw new Error('선택한 배송지를 찾지 못했어요.');
  }

  isMutating.value = true;
  errorMessage.value = '';

  try {
    await setMainAddress(addressId);
    savedAddresses.value = markMain(savedAddresses.value, addressId);
    syncSelection();
    return savedAddresses.value.find((addr) => addr.id === addressId) ?? null;
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

  isMutating.value = true;
  errorMessage.value = '';

  try {
    await deleteAddress(addressId);
    const nextList = savedAddresses.value.filter((addr) => addr.id !== addressId);
    const nextMainId = findMainId(nextList) ?? nextList[0]?.id ?? null;

    savedAddresses.value = markMain(nextList, nextMainId);
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

  const defaultAddress = computed(() => savedAddresses.value.find((addr) => addr.main) ?? null);

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
