<template>
  <section class="space-y-6">
    <div class="flex items-center justify-between">
      <div class="space-y-1">
        <h2 class="text-2xl font-semibold text-gray-900">배송지 수정</h2>
        <p class="text-sm text-gray-500">주소지 이름과 상세 정보를 다시 확인해 주세요.</p>
      </div>
      <RouterLink
        :to="{ name: 'my-page-addresses' }"
        class="text-sm font-semibold text-green-700 hover:text-green-800"
      >
        목록으로 돌아가기
      </RouterLink>
    </div>

    <MyAddressForm
      v-if="addressFormData"
      mode="edit"
      :initial-data="addressFormData"
      :is-submitting="isAddressSubmitting"
      :submit-error="addressSubmitError"
      :is-main-locked="isMainLocked"
      @submit="handleSubmitAddress"
      @cancel="handleCancel"
    />

    <div
      v-else
      class="border border-dashed border-gray-200 rounded-2xl p-5 text-sm text-gray-500 bg-gray-50"
    >
      수정할 배송지를 찾는 중이에요.
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref, watch, computed } from 'vue';
import { RouterLink, useRoute, useRouter } from 'vue-router';
import MyAddressForm from '@/components/mypage/MyAddressForm.vue';
import { useAuth } from '@/composables/useAuth';
import { useAddresses } from '@/composables/useAddresses';

const route = useRoute();
const router = useRouter();
const targetId = computed(() => Number(route.params.id));

const { loadUserProfile } = useAuth();
const { addresses, loadAddresses, editAddress, isMutating: isAddressSubmitting } = useAddresses();

const addressFormData = ref(null);
const addressSubmitError = ref('');
const isMainLocked = computed(() => addresses.value.length === 1);

onMounted(async () => {
  try {
    await loadUserProfile();
  } catch (error) {
    console.error(error);
    return;
  }

  try {
    await loadAddresses();
    syncTargetAddress();
  } catch (error) {
    console.error(error);
  }
});

watch([addresses, () => route.params.id], () => {
  syncTargetAddress();
});

const syncTargetAddress = () => {
  if (Number.isNaN(targetId.value)) {
    window.alert('잘못된 배송지 정보예요.');
    router.push({ name: 'my-page-addresses' });
    return;
  }

  const found = addresses.value.find((item) => item.id === targetId.value);
  if (found) {
    addressFormData.value = { ...found };
    return;
  }

  window.alert('수정할 배송지를 찾지 못했어요.');
  router.push({ name: 'my-page-addresses' });
};

const handleSubmitAddress = async (payload) => {
  addressSubmitError.value = '';

  try {
    await editAddress(targetId.value, payload);
    window.alert('배송지를 수정했어요.');
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
