<template>
  <section class="space-y-6">
    <MyProfileSection
      :is-authenticated="isAuthenticated"
      :is-profile-loading="isProfileLoading"
      :profile-error="profileError"
      :profile-fields="profileFields"
      @edit-profile="handleEditProfile"
      @delete-account="handleDeleteAccount"
    />
  </section>
</template>

<script setup>
import { computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import MyProfileSection from '@/components/mypage/MyProfileSection.vue';
import { useAuth } from '@/composables/useAuth';
import { formatPhoneNumber } from '@/utils/phone';

const router = useRouter();
const { user, isAuthenticated, deleteAccount, loadUserProfile, isProfileLoading, profileError } = useAuth();

onMounted(async () => {
  try {
    await loadUserProfile();
  } catch (error) {
    console.error(error);
  }
});

const formattedCreatedDate = computed(() => {
  if (!user.value?.createdDate) return '';
  return new Date(user.value.createdDate).toLocaleDateString('ko-KR');
});

const profileFields = computed(() => (
  user.value
    ? [
        { label: '이름', value: user.value.name },
        { label: '이메일', value: user.value.email },
        { label: '연락처', value: user.value.phone ? formatPhoneNumber(user.value.phone) : '-' },
        // { label: '주소', value: user.value.address ?? '-' },
        { label: '가입일', value: formattedCreatedDate.value || '?' },
        { label: '권한', value: user.value.role ?? '?' },
      ]
    : []
));

const handleEditProfile = () => {
  router.push('/mypage/edit');
};

const handleDeleteAccount = async () => {
  if (!confirm('탈퇴 시 모든 정보가 삭제됩니다. 계속하시겠습니까?')) return;

  try {
    await deleteAccount();
    alert('탈퇴 처리되었습니다. 다시 만나요!');
    router.push('/');
  } catch (error) {
    console.error(error);
    alert('탈퇴 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
  }
};
</script>
