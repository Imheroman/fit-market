<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <AppHeader />

    <main class="flex-1">
      <div class="container mx-auto px-4 py-10 max-w-3xl">
        <div class="space-y-8">
          <header class="text-center md:text-left">
            <p class="text-sm font-semibold text-green-600">회원 정보 수정</p>
            <h1 class="mt-2 text-3xl md:text-4xl font-bold text-gray-900">내 정보를 손쉽게 바꿔보세요</h1>
            <p class="mt-3 text-gray-500">필요한 항목만 고치고 저장하면 돼요.</p>
          </header>

          <section class="bg-white shadow-lg rounded-2xl border border-green-100 p-6 md:p-8">
            <div v-if="isLoading" class="space-y-4 animate-pulse">
              <div class="h-5 bg-gray-200 rounded" />
              <div class="h-5 bg-gray-200 rounded" />
              <div class="h-5 bg-gray-200 rounded" />
            </div>

            <form v-else class="space-y-6" @submit.prevent="handleSubmit">
              <div v-if="serverError" class="p-4 rounded-xl bg-red-50 text-red-600 text-sm">
                {{ serverError }}
              </div>
              <div v-if="successMessage" class="p-4 rounded-xl bg-green-50 text-green-700 text-sm">
                {{ successMessage }}
              </div>

              <div class="space-y-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700">이름</label>
                  <input
                    v-model="form.name"
                    type="text"
                    class="mt-2 w-full rounded-xl border border-gray-200 px-4 py-3 focus:border-green-500 focus:ring-2 focus:ring-green-100"
                    placeholder="이름을 입력해주세요"
                  />
                  <p v-if="errors.name" class="mt-1 text-sm text-red-500">{{ errors.name }}</p>
                </div>

                <div>
                  <label class="block text-sm font-medium text-gray-700">이메일</label>
                  <input
                    v-model="form.email"
                    type="email"
                    class="mt-2 w-full rounded-xl border border-gray-200 px-4 py-3 focus:border-green-500 focus:ring-2 focus:ring-green-100"
                    placeholder="example@fitmarket.com"
                  />
                  <p v-if="errors.email" class="mt-1 text-sm text-red-500">{{ errors.email }}</p>
                </div>

                <div>
                  <label class="block text-sm font-medium text-gray-700">연락처</label>
                  <input
                    v-model="form.phone"
                    type="tel"
                    class="mt-2 w-full rounded-xl border border-gray-200 px-4 py-3 focus:border-green-500 focus:ring-2 focus:ring-green-100"
                    placeholder="010-0000-0000"
                  />
                  <p v-if="errors.phone" class="mt-1 text-sm text-red-500">{{ errors.phone }}</p>
                </div>

                <div>
                  <label class="block text-sm font-medium text-gray-700">주소</label>
                  <input
                    v-model="form.address"
                    type="text"
                    class="mt-2 w-full rounded-xl border border-gray-200 px-4 py-3 focus:border-green-500 focus:ring-2 focus:ring-green-100"
                    placeholder="배송 받을 주소를 입력해주세요"
                  />
                  <p v-if="errors.address" class="mt-1 text-sm text-red-500">{{ errors.address }}</p>
                </div>
              </div>

<!--              <div class="rounded-2xl border border-gray-100 bg-gray-50 p-4 space-y-3">-->
<!--                <p class="text-sm font-semibold text-gray-700">알림 설정</p>-->
<!--                <label class="flex items-center gap-3 text-sm text-gray-700">-->
<!--                  <input type="checkbox" v-model="form.notificationEmail" class="rounded border-gray-300 text-green-600" />-->
<!--                  이메일로 알림 받아볼래요-->
<!--                </label>-->
<!--                <label class="flex items-center gap-3 text-sm text-gray-700">-->
<!--                  <input type="checkbox" v-model="form.notificationSms" class="rounded border-gray-300 text-green-600" />-->
<!--                  문자로도 알려주세요-->
<!--                </label>-->
<!--                <label class="flex items-center gap-3 text-sm text-gray-700">-->
<!--                  <input type="checkbox" v-model="form.marketingConsent" class="rounded border-gray-300 text-green-600" />-->
<!--                  이벤트와 혜택을 빠르게 받을래요-->
<!--                </label>-->
<!--              </div>-->

              <div class="flex flex-col gap-3 md:flex-row">
                <button
                  type="button"
                  @click="goBack"
                  class="flex-1 rounded-xl border border-gray-200 px-6 py-3 font-semibold text-gray-600 hover:bg-gray-50"
                >
                  돌아가기
                </button>
                <button
                  type="button"
                  @click="resetForm"
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
                  <span v-else>변경사항 저장</span>
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
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
import { useUserProfile } from '@/composables/useUserProfile'

const router = useRouter()
const { form, errors, isDirty, isLoading, isSubmitting, serverError, successMessage, loadProfile, submitProfile, resetForm } =
  useUserProfile()

onMounted(() => {
  loadProfile()
})

const goBack = () => {
  router.push('/mypage')
}

const handleSubmit = async () => {
  await submitProfile()
}
</script>
