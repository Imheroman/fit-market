<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <AppHeader />

    <main class="flex-1">
      <div class="container mx-auto px-4 py-10">
        <div class="flex flex-col gap-10">
          <header class="space-y-3 text-center md:text-left">
            <p class="text-sm font-semibold text-green-600">마이페이지</p>
            <h1 class="text-3xl md:text-4xl font-bold text-gray-900">나의 회원 정보</h1>
            <p class="text-gray-500">회원정보를 확인하고 수정하거나 탈퇴할 수 있어요.</p>
          </header>

          <section class="bg-white shadow-lg rounded-2xl p-6 md:p-8 border border-green-100">
            <div class="flex flex-col gap-6">
              <div class="flex flex-col gap-2">
                <h2 class="text-xl font-semibold text-gray-900">기본 정보</h2>
                <p v-if="!isAuthenticated" class="text-sm text-gray-500">
                  로그인 후 마이페이지를 이용해주세요.
                </p>
              </div>

              <div v-if="isAuthenticated" class="grid gap-4 md:grid-cols-2">
                <div
                  v-for="item in profileFields"
                  :key="item.label"
                  class="p-4 rounded-xl bg-green-50 border border-green-100"
                >
                  <p class="text-sm text-gray-500">{{ item.label }}</p>
                  <p class="text-lg font-semibold text-gray-900">{{ item.value }}</p>
                </div>
              </div>

              <div v-if="isAuthenticated" class="flex flex-col gap-3 md:flex-row">
                <button
                  type="button"
                  @click="handleEditProfile"
                  class="flex-1 bg-green-600 text-white px-6 py-3 rounded-xl font-semibold hover:bg-green-700 transition-colors"
                >
                  회원 정보 수정
                </button>
                <button
                  type="button"
                  @click="handleDeleteAccount"
                  class="flex-1 border border-red-200 text-red-600 px-6 py-3 rounded-xl font-semibold hover:bg-red-50 transition-colors"
                >
                  회원 탈퇴
                </button>
              </div>
            </div>
          </section>
        </div>
      </div>
    </main>

    <AppFooter />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useAuth } from '@/composables/useAuth'
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
import { useRouter } from 'vue-router'

const router = useRouter();
const { user, isAuthenticated, deleteAccount } = useAuth()

const formattedCreatedDate = computed(() => {
  if (!user.value?.joinedAt) return '-'
  return new Date(user.value.joinedAt).toLocaleDateString('ko-KR')
})

const profileFields = computed(() => (
  user.value
    ? [
        { label: '이름', value: user.value.name },
        { label: '이메일', value: user.value.email },
        { label: '연락처', value: user.value.phone ?? '-' },
        { label: '주소', value: user.value.address ?? '-' },
        { label: '가입일', value: formattedCreatedDate.value },
        { label: '권한', value: user.value.roles?.join(', ') ?? '-' },
      ]
    : []
))

const handleEditProfile = () => {
  router.push('/mypage/edit')
}

const handleDeleteAccount = async () => { // Changed
  if (!confirm('탈퇴 시 모든 정보가 삭제됩니다. 계속하시겠습니까?')) return

  try { // Changed
    await deleteAccount();
    alert('탈퇴 처리되었습니다. 다시 만나요!')
    router.push('/');
  } catch (error) {
    console.error(error);
    alert('탈퇴 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.') // Changed
  }
}
</script>
