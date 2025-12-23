<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <AppHeader />

    <main class="flex-1">
      <div class="container mx-auto px-4 py-10 max-w-3xl">
        <div class="space-y-8">
          <header class="text-center md:text-left space-y-2">
            <p class="text-sm font-semibold text-green-600">이름 변경</p>
            <h1 class="text-3xl md:text-4xl font-bold text-gray-900">새 이름으로 불러드릴게요</h1>
            <p class="text-gray-500">배송지와 영수증에 보여줄 이름을 정해요.</p>
          </header>

          <section class="bg-white shadow-lg rounded-2xl border border-green-100 p-6 md:p-8">
            <div v-if="isProfileLoading || isInitializing" class="space-y-4 animate-pulse">
              <div class="h-5 bg-gray-200 rounded" />
              <div class="h-5 bg-gray-200 rounded" />
              <div class="h-5 bg-gray-200 rounded" />
            </div>

            <div v-else-if="profileError" class="p-4 rounded-xl bg-red-50 text-red-600 text-sm">
              {{ profileError }}
            </div>

            <form v-else class="space-y-6" @submit.prevent="handleSubmit">
              <div v-if="serverError" class="p-4 rounded-xl bg-red-50 text-red-600 text-sm">
                {{ serverError }}
              </div>
              <div v-if="successMessage" class="p-4 rounded-xl bg-green-50 text-green-700 text-sm">
                {{ successMessage }}
              </div>

              <div class="space-y-2">
                <label class="block text-sm font-medium text-gray-700">이름</label>
                <div class="relative">
                  <input
                    v-model="name"
                    type="text"
                    maxlength="30"
                    class="w-full rounded-xl border border-gray-200 px-4 py-3 pr-12 focus:border-green-500 focus:ring-2 focus:ring-green-100"
                    placeholder="이름을 입력해주세요"
                  />
                  <button
                    v-if="name"
                    type="button"
                    class="absolute inset-y-0 right-3 my-auto h-9 w-9 rounded-full text-lg text-gray-400 hover:text-gray-700"
                    aria-label="입력 지우기"
                    @click="handleClear"
                  >
                    X
                  </button>
                </div>
                <p v-if="error" class="text-sm text-red-500">{{ error }}</p>
                <p class="text-xs text-gray-500">한글 또는 영문 2~30자로 입력해주세요.</p>
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
                  :disabled="!isDirty || isSubmitting"
                  class="flex-1 rounded-xl border border-gray-200 px-6 py-3 font-semibold text-gray-600 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-40"
                >
                  변경 취소
                </button>
                <button
                  type="submit"
                  :disabled="!isDirty || isSubmitting"
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
import { computed, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import AppHeader from '@/components/AppHeader.vue';
import AppFooter from '@/components/AppFooter.vue';
import { useAuth } from '@/composables/useAuth';
import { useChangeName } from '@/composables/useChangeName';

const router = useRouter();
const { user, loadUserProfile, isProfileLoading, profileError } = useAuth();
const { name, error, serverError, successMessage, isSubmitting, isDirty, setInitialValue, submit, reset, clear } =
  useChangeName();
const isInitializing = ref(true);
const profileName = computed(() => user.value?.name ?? '');

const hydrateFromProfile = async () => {
  try {
    await loadUserProfile();
  } catch (errorResponse) {
    console.error(errorResponse);
  } finally {
    isInitializing.value = false;
  }
};

onMounted(() => {
  hydrateFromProfile();
});

watch(
  profileName,
  (value) => {
    setInitialValue(value);
  },
  { immediate: true }
);

const handleSubmit = async () => {
  const result = await submit();
  if (result) {
    await loadUserProfile();
    const message = successMessage.value || '이름을 바꿨어요.';
    window.alert(message);
    router.push('/mypage');
  }
};

const handleReset = () => {
  reset();
};

const handleClear = () => {
  clear();
};
</script>
