<template>
  <section class="space-y-6">
    <div class="flex items-center justify-between">
      <div class="space-y-1">
        <h2 class="text-2xl font-semibold text-gray-900">새 배송지 등록</h2>
        <p class="text-sm text-gray-500">연락처는 숫자 11자리만 입력할 수 있어요.</p>
      </div>
      <RouterLink
        :to="{ name: 'my-page-addresses' }"
        class="text-sm font-semibold text-green-700 hover:text-green-800"
      >
        목록으로 돌아가기
      </RouterLink>
    </div>

    <p
      v-if="isAddressLimitReached"
      class="text-sm text-amber-700 bg-amber-50 border border-amber-100 rounded-lg px-3 py-2"
    >
      배송지는 최대 {{ maxAddressCount }}개까지 등록할 수 있어요. 기존 배송지를 수정하거나 삭제해 주세요.
    </p>

    <MyAddressForm
      mode="create"
      :initial-data="addressFormData"
      :is-submitting="isAddressSubmitting"
      :is-submit-disabled="isAddressLimitReached"
      :submit-error="addressSubmitError"
      :is-main-locked="isMainLocked"
      @submit="handleSubmitAddress"
      @cancel="handleCancel"
    />
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import MyAddressForm from '@/components/mypage/MyAddressForm.vue';
import { useAuth } from '@/composables/useAuth';
import { useAddresses } from '@/composables/useAddresses';

const router = useRouter();
const { user, loadUserProfile } = useAuth();
const {
  addresses,
  loadAddresses,
  addAddress,
  isMutating: isAddressSubmitting,
  isAddressLimitReached,
  maxAddressCount,
} = useAddresses();

const addressFormData = ref(createEmptyAddress());
const addressSubmitError = ref('');
const hasLoadedAddresses = ref(false);

const isMainLocked = computed(() => hasLoadedAddresses.value && addresses.value.length === 0);

onMounted(async () => {
  try {
    await loadUserProfile();
  } catch (error) {
    console.error(error);
    return;
  }

  try {
    await loadAddresses();
    hasLoadedAddresses.value = true;
  } catch (error) {
    console.error(error);
    hasLoadedAddresses.value = true;
  }
});

function createEmptyAddress() {
  return {
    name: '',
    recipient: '',
    phone: '',
    postalCode: '',
    addressLine: '',
    addressLineDetail: '',
    memo: '',
    main: false,
  };
}


const handleSubmitAddress = async (payload) => {
  addressSubmitError.value = '';

  try {
    if (isAddressLimitReached.value) {
      addressSubmitError.value = `배송지는 최대 ${maxAddressCount}개까지 등록할 수 있어요.`;
      return;
    }
    await addAddress(payload);
    window.alert('새 배송지를 등록했어요.');
    router.push({ name: 'my-page-addresses' });
  } catch (error) {
    console.error(error);
    addressSubmitError.value = error?.message ?? '배송지 저장에 실패했어요.';
  }
};

const handleCancel = () => {
  router.push({ name: 'my-page-addresses' });
};
</script>
