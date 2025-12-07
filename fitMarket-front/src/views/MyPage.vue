<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <AppHeader />

    <main class="flex-1">
      <div class="container mx-auto px-4 py-10">
        <div class="space-y-8">
          <header class="text-center md:text-left space-y-3">
            <p class="text-sm font-semibold text-green-600">마이페이지</p>
            <h1 class="text-3xl md:text-4xl font-bold text-gray-900">나의 회원 정보</h1>
            <p class="text-gray-500">주문 기록과 배송지를 한 곳에서 관리할 수 있어요.</p>
          </header>

<!--          <MyPageSummary
            :user="user"
            :is-authenticated="isAuthenticated"
            :is-profile-loading="isProfileLoading"
            :profile-error="profileError"
            :formatted-created-date="formattedCreatedDate"
          />-->

          <MyPageTabs :tabs="tabs" :active-tab="activeTab" @change="setActiveTab" />

          <MyProfileSection
            v-if="activeTab === 'profile'"
            :is-authenticated="isAuthenticated"
            :is-profile-loading="isProfileLoading"
            :profile-error="profileError"
            :profile-fields="profileFields"
            @edit-profile="handleEditProfile"
            @delete-account="handleDeleteAccount"
          />

          <MyOrdersSection
            v-else-if="activeTab === 'orders'"
            :filter-options="filterOptions"
            :selected-range="selectedRange"
            :filter-description="filterDescription"
            :orders="filteredOrders"
            @change-filter="setFilter"
            @view-order="handleViewOrder"
          />

          <MyAddressesSection
            v-else
            :addresses="addresses"
            @add-address="handleAddAddress"
            @edit-address="handleEditAddressInfo"
            @set-default="handleSetDefault"
            @remove-address="handleRemoveAddress"
          />
        </div>
      </div>
    </main>

    <AppFooter />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { UserRound, Package, MapPin } from 'lucide-vue-next';
import AppHeader from '@/components/AppHeader.vue';
import AppFooter from '@/components/AppFooter.vue';
import MyPageSummary from '@/components/mypage/MyPageSummary.vue';
import MyPageTabs from '@/components/mypage/MyPageTabs.vue';
import MyProfileSection from '@/components/mypage/MyProfileSection.vue';
import MyOrdersSection from '@/components/mypage/MyOrdersSection.vue';
import MyAddressesSection from '@/components/mypage/MyAddressesSection.vue';
import { useAuth, ensureRoleArray } from '@/composables/useAuth';
import { useOrderHistory } from '@/composables/useOrderHistory';
import { useAddresses } from '@/composables/useAddresses';
import { formatPhoneNumber } from '@/utils/phone';

const router = useRouter();
const { user, isAuthenticated, deleteAccount, loadUserProfile, isProfileLoading, profileError } = useAuth();
const { filterOptions, selectedRange, filteredOrders, filterDescription, setFilter } = useOrderHistory();
const { addresses, setDefaultAddress, removeAddress } = useAddresses();

const tabs = [
  { label: '기본 정보', value: 'profile', icon: UserRound },
  { label: '주문 내역', value: 'orders', icon: Package },
  { label: '배송지 관리', value: 'addresses', icon: MapPin },
];

const activeTab = ref('profile');

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
        { label: '주소', value: user.value.address ?? '-' },
        { label: '가입일', value: formattedCreatedDate.value || '?' },
        { label: '권한', value: user.value.role ?? '?'},
      ]
    : []
));

const setActiveTab = (value) => {
  activeTab.value = value;
};

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

const handleViewOrder = (orderNumber) => {
  window.alert(`주문번호 ${orderNumber} 상세 페이지는 준비 중이에요.`);
};

const handleSetDefault = (addressId) => {
  setDefaultAddress(addressId);
  window.alert('기본 배송지를 업데이트했어요.');
};

const handleAddAddress = () => {
  window.alert('새 배송지 등록 기능은 곧 연결할게요. 당분간은 고객센터로 요청해 주세요.');
};

const handleEditAddressInfo = (address) => {
  window.alert(`${address.label} 배송지 수정 기능은 준비 중이에요.`);
};

const handleRemoveAddress = (addressId) => {
  if (addresses.value.length <= 1) {
    window.alert('배송지는 최소 1개 이상 필요해요.');
    return;
  }

  if (!confirm('이 배송지를 삭제할까요?')) return;
  removeAddress(addressId);
};
</script>
