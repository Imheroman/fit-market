<template>
  <section class="space-y-6">
    <MyAddressesSection
      :addresses="addresses"
      :is-loading="isAddressLoading"
      :error-message="addressErrorMessage"
      @set-default="handleSetDefault"
      @remove-address="handleRemoveAddress"
    />
  </section>
</template>

<script setup>
import { onMounted } from 'vue';
import MyAddressesSection from '@/components/mypage/MyAddressesSection.vue';
import { useAuth } from '@/composables/useAuth';
import { useAddresses } from '@/composables/useAddresses';

const { loadUserProfile } = useAuth();
const {
  addresses,
  loadAddresses,
  setDefaultAddress,
  removeAddress,
  isLoading: isAddressLoading,
  errorMessage: addressErrorMessage,
} = useAddresses();

onMounted(async () => {
  try {
    await Promise.all([loadUserProfile(), loadAddresses()]);
  } catch (error) {
    console.error(error);
  }
});

const handleSetDefault = (addressId) => {
  setDefaultAddress(addressId)
    .then(() => {
      window.alert('기본 배송지를 업데이트했어요.');
    })
    .catch((error) => {
      console.error(error);
      window.alert(error?.message ?? '기본 배송지 설정에 실패했어요.');
    });
};

const handleRemoveAddress = async (addressId) => {
  if (!confirm('이 배송지를 삭제할까요?')) return;

  try {
    await removeAddress(addressId);
    window.alert('배송지를 삭제했어요.');
  } catch (error) {
    console.error(error);
    window.alert(error?.message ?? '배송지를 삭제하지 못했어요.');
  }
};
</script>
