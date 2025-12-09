<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <AppHeader />

    <main class="flex-1">
      <div class="container mx-auto px-4 py-10 max-w-3xl">
        <div class="space-y-8">
          <header class="text-center md:text-left space-y-2">
            <p class="text-sm font-semibold text-green-600">비밀번호 변경</p>
            <h1 class="text-3xl md:text-4xl font-bold text-gray-900">더 단단하게 지켜드릴게요</h1>
            <p class="text-gray-500">현재 비밀번호를 확인하고 새 비밀번호를 설정하세요.</p>
          </header>

          <section class="bg-white shadow-lg rounded-2xl border border-green-100 p-6 md:p-8">
            <div v-if="isProfileLoading" class="space-y-4 animate-pulse">
              <div class="h-5 bg-gray-200 rounded" />
              <div class="h-5 bg-gray-200 rounded" />
              <div class="h-5 bg-gray-200 rounded" />
            </div>

            <div v-else-if="profileError" class="p-4 rounded-xl bg-red-50 text-red-600 text-sm">
              {{ profileError }}
            </div>

            <div v-else-if="!isAuthenticated" class="space-y-4">
              <p class="text-sm text-gray-600">로그인 후 비밀번호를 바꿀 수 있어요.</p>
              <RouterLink
                to="/login"
                class="inline-flex items-center justify-center rounded-xl bg-green-600 px-4 py-2 text-white font-semibold hover:bg-green-700"
              >
                로그인하기
              </RouterLink>
            </div>

            <form v-else class="space-y-6" @submit.prevent="handleSubmit">
              <div v-if="serverError" class="p-4 rounded-xl bg-red-50 text-red-600 text-sm">
                {{ serverError }}
              </div>
              <div v-if="successMessage" class="p-4 rounded-xl bg-green-50 text-green-700 text-sm">
                {{ successMessage }}
              </div>

              <div class="space-y-2">
                <label class="block text-sm font-medium text-gray-700">현재 비밀번호</label>
                <div class="relative">
                  <input
                    v-model="currentPassword"
                    type="password"
                    class="w-full rounded-xl border border-gray-200 px-4 py-3 pr-12 focus:border-green-500 focus:ring-2 focus:ring-green-100"
                    placeholder="현재 비밀번호를 입력해주세요"
                  />
                  <button
                    v-if="currentPassword"
                    type="button"
                    class="absolute inset-y-0 right-3 my-auto h-9 w-9 rounded-full text-lg text-gray-400 hover:text-gray-700"
                    aria-label="현재 비밀번호 지우기"
                    @click="handleClear('currentPassword')"
                  >
                    X
                  </button>
                </div>
                <p v-if="errors.currentPassword" class="text-sm text-red-500">{{ errors.currentPassword }}</p>
              </div>

              <div class="space-y-2">
                <label class="block text-sm font-medium text-gray-700">새 비밀번호</label>
                <div class="relative">
                  <input
                    v-model="newPassword"
                    type="password"
                    maxlength="16"
                    class="w-full rounded-xl border border-gray-200 px-4 py-3 pr-12 focus:border-green-500 focus:ring-2 focus:ring-green-100"
                    placeholder="영문+숫자 조합 8~16자"
                  />
                  <button
                    v-if="newPassword"
                    type="button"
                    class="absolute inset-y-0 right-3 my-auto h-9 w-9 rounded-full text-lg text-gray-400 hover:text-gray-700"
                    aria-label="새 비밀번호 지우기"
                    @click="handleClear('newPassword')"
                  >
                    X
                  </button>
                </div>
                <p v-if="errors.newPassword" class="text-sm text-red-500">{{ errors.newPassword }}</p>
                <div class="text-xs text-gray-500 space-y-1">
                  <p>영문과 숫자를 모두 포함해 주세요.</p>
                  <p>8~16자로 설정해 주세요.</p>
                </div>
              </div>

              <div class="space-y-2">
                <label class="block text-sm font-medium text-gray-700">새 비밀번호 확인</label>
                <div class="relative">
                  <input
                    v-model="confirmPassword"
                    type="password"
                    maxlength="16"
                    class="w-full rounded-xl border border-gray-200 px-4 py-3 pr-12 focus:border-green-500 focus:ring-2 focus:ring-green-100"
                    placeholder="한 번 더 입력해주세요"
                  />
                  <button
                    v-if="confirmPassword"
                    type="button"
                    class="absolute inset-y-0 right-3 my-auto h-9 w-9 rounded-full text-lg text-gray-400 hover:text-gray-700"
                    aria-label="새 비밀번호 확인 지우기"
                    @click="handleClear('confirmPassword')"
                  >
                    X
                  </button>
                </div>
                <p v-if="errors.confirmPassword" class="text-sm text-red-500">{{ errors.confirmPassword }}</p>
              </div>

              <div class="flex flex-col gap-3 md:flex-row">
                <RouterLink
                  to="/mypage/edit"
                  class="flex-1 rounded-xl border border-gray-200 px-6 py-3 font-semibold text-gray-600 hover:bg-gray-50 text-center"
                >
                  목록으로 돌아가기
                </RouterLink>
                <button
                  type="button"
                  @click="handleReset"
                  :disabled="isSubmitting"
                  class="flex-1 rounded-xl border border-gray-200 px-6 py-3 font-semibold text-gray-600 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-40"
                >
                  초기화
                </button>
                <button
                  type="submit"
                  :disabled="isSubmitting"
                  class="flex-1 rounded-xl bg-green-600 px-6 py-3 font-semibold text-white hover:bg-green-700 disabled:cursor-not-allowed disabled:opacity-40"
                >
                  <span v-if="isSubmitting">저장 중...</span>
                  <span v-else>변경하기</span>
                </button>
              </div>
            </form>
          </section>
        </div>
      </div>
    </main>

    <AppFooter />
  </div>
</template>

<script setup>
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';
import AppHeader from '@/components/AppHeader.vue';
import AppFooter from '@/components/AppFooter.vue';
import { useAuth } from '@/composables/useAuth';
import { useChangePassword } from '@/composables/useChangePassword';

const router = useRouter();
const { isAuthenticated, isProfileLoading, profileError, loadUserProfile } = useAuth();
const {
  currentPassword,
  newPassword,
  confirmPassword,
  errors,
  serverError,
  successMessage,
  isSubmitting,
  submit,
  resetForm,
  clearField,
} = useChangePassword();

onMounted(async () => {
  try {
    await loadUserProfile();
  } catch (error) {
    console.error(error);
  }
});

const handleSubmit = async () => {
  const result = await submit();
  if (result) {
    await loadUserProfile();
    const message = successMessage.value || '비밀번호를 바꿨어요.';
    window.alert(message);
    router.push('/mypage');
  }
};

const handleReset = () => {
  resetForm();
};

const handleClear = (field) => {
  clearField(field);
};
</script>
