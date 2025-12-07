<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <AppHeader />

    <main class="flex-1">
      <div class="container mx-auto px-4 py-10 max-w-3xl">
        <div class="space-y-8">
          <header class="text-center md:text-left space-y-2">
            <p class="text-sm font-semibold text-green-600">회원 정보 수정</p>
            <h1 class="text-3xl md:text-4xl font-bold text-gray-900">내 계정을 깔끔하게 관리해요</h1>
            <p class="text-gray-500">이메일은 로그인 전용이에요. 이름, 연락처, 비밀번호는 필요할 때 바로 바꿀 수 있어요.</p>
          </header>

          <section class="bg-white shadow-lg rounded-2xl border border-green-100 p-6 md:p-8 space-y-6">
            <div v-if="isProfileLoading" class="space-y-4 animate-pulse">
              <div class="h-5 bg-gray-200 rounded" />
              <div class="h-5 bg-gray-200 rounded" />
              <div class="h-5 bg-gray-200 rounded" />
            </div>

            <div v-else-if="profileError" class="p-4 rounded-xl bg-red-50 text-red-600 text-sm">
              {{ profileError }}
            </div>

            <div v-else-if="!isAuthenticated" class="space-y-4">
              <p class="text-sm text-gray-600">로그인하면 내 정보를 확인하고 수정할 수 있어요.</p>
              <RouterLink
                to="/login"
                class="inline-flex items-center justify-center rounded-xl bg-green-600 px-4 py-2 text-white font-semibold hover:bg-green-700"
              >
                로그인하기
              </RouterLink>
            </div>

            <div v-else class="space-y-6">
              <div class="space-y-2">
                <label class="block text-sm font-medium text-gray-700">이메일</label>
                <div class="flex items-center gap-3 rounded-xl border border-gray-200 bg-gray-50 px-4 py-3">
                  <span class="text-lg font-semibold text-gray-900">{{ userEmail }}</span>
                  <span class="ml-auto text-xs text-gray-500">로그인용이라 수정할 수 없어요.</span>
                </div>
              </div>

              <div class="grid gap-4">
                <div class="flex items-center justify-between rounded-xl border border-gray-100 bg-gray-50 p-4">
                  <div class="space-y-1">
                    <p class="text-sm text-gray-500">이름</p>
                    <p class="text-lg font-semibold text-gray-900">{{ userName }}</p>
                    <p class="text-xs text-gray-500">배송지와 영수증에 표시돼요.</p>
                  </div>
                  <RouterLink
                    :to="{ name: 'my-page-edit-name' }"
                    class="inline-flex items-center gap-2 rounded-lg bg-green-600 px-4 py-2 text-white font-semibold hover:bg-green-700"
                  >
                    변경하기
                  </RouterLink>
                </div>

                <div class="flex items-center justify-between rounded-xl border border-gray-100 bg-gray-50 p-4">
                  <div class="space-y-1">
                    <p class="text-sm text-gray-500">연락처</p>
                    <p class="text-lg font-semibold text-gray-900">{{ userPhone }}</p>
                    <p class="text-xs text-gray-500">배송 안내와 알림에 사용돼요.</p>
                  </div>
                  <RouterLink
                    :to="{ name: 'my-page-edit-phone' }"
                    class="inline-flex items-center gap-2 rounded-lg bg-green-600 px-4 py-2 text-white font-semibold hover:bg-green-700"
                  >
                    변경하기
                  </RouterLink>
                </div>

                <div class="flex items-center justify-between rounded-xl border border-gray-100 bg-gray-50 p-4">
                  <div class="space-y-1">
                    <p class="text-sm text-gray-500">비밀번호</p>
                    <p class="text-lg font-semibold text-gray-900">••••••••</p>
                    <p class="text-xs text-gray-500">계정을 더 단단하게 지켜요.</p>
                  </div>
                  <RouterLink
                    :to="{ name: 'my-page-edit-password' }"
                    class="inline-flex items-center gap-2 rounded-lg bg-green-600 px-4 py-2 text-white font-semibold hover:bg-green-700"
                  >
                    변경하기
                  </RouterLink>
                </div>
              </div>

              <div class="flex justify-end">
                <RouterLink to="/mypage" class="text-sm text-gray-600 hover:text-gray-900 underline">
                  마이페이지로 돌아가기
                </RouterLink>
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
import { computed, onMounted } from 'vue';
import AppHeader from '@/components/AppHeader.vue';
import AppFooter from '@/components/AppFooter.vue';
import { useAuth } from '@/composables/useAuth';
import { formatPhoneNumber } from '@/utils/phone';

const { user, isAuthenticated, loadUserProfile, isProfileLoading, profileError } = useAuth();

onMounted(async () => {
  try {
    await loadUserProfile();
  } catch (error) {
    console.error(error);
  }
});

const userEmail = computed(() => user.value?.email ?? '이메일 정보를 찾을 수 없어요.');
const userName = computed(() => user.value?.name ?? '이름을 등록해주세요.');
const userPhone = computed(() => {
  const phone = user.value?.phone;
  return phone ? formatPhoneNumber(phone) : '연락처를 등록해주세요.';
});
</script>
