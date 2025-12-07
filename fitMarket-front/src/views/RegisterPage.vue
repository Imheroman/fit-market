<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <AppHeader />

    <main class="flex-1">
      <div class="container mx-auto px-4 py-16 max-w-3xl">
        <section class="bg-white border border-green-100 rounded-3xl shadow-xl p-8">
          <div class="space-y-3 text-center">
            <p class="text-sm font-semibold text-green-600">회원가입</p>
            <h1 class="text-3xl font-bold text-gray-900">필요한 정보만 입력하면 금방 끝나요</h1>
            <p class="text-gray-500">이메일, 이름, 비밀번호, 휴대폰 번호를 입력하면 건강한 장보기가 시작돼요.</p>
          </div>

          <form class="mt-10 space-y-6" @submit.prevent="handleSubmit">
            <div v-if="serverError" class="rounded-2xl border border-red-200 bg-red-50 p-4 text-sm text-red-600">
              {{ serverError }}
            </div>
            <div v-if="successMessage" class="rounded-2xl border border-green-200 bg-green-50 p-4 text-sm text-green-700">
              {{ successMessage }}
            </div>

            <div class="space-y-5">
              <div>
                <label for="register-email" class="block text-sm font-semibold text-gray-700">이메일</label>
                <input
                  id="register-email"
                  v-model="form.email"
                  type="email"
                  autocomplete="email"
                  placeholder="example@fitmarket.com"
                  class="mt-2 w-full rounded-2xl border border-gray-200 px-4 py-3 focus:border-green-500 focus:ring-2 focus:ring-green-100"
                />
                <p v-if="errors.email" class="mt-1 text-sm text-red-500">{{ errors.email }}</p>
              </div>

              <div>
                <label for="register-password" class="flex items-center justify-between text-sm font-semibold text-gray-700">
                  비밀번호
                  <span class="text-xs font-normal text-gray-400">영문+숫자 8~16자</span>
                </label>
                <input
                  id="register-password"
                  v-model="form.password"
                  type="password"
                  autocomplete="new-password"
                  maxlength="16"
                  placeholder="새 비밀번호"
                  class="mt-2 w-full rounded-2xl border border-gray-200 px-4 py-3 focus:border-green-500 focus:ring-2 focus:ring-green-100"
                />
                <p v-if="errors.password" class="mt-1 text-sm text-red-500">{{ errors.password }}</p>
                <p v-else class="mt-1 text-xs text-gray-500">숫자와 영문을 모두 넣어 8~16자로 만들어 주세요.</p>
              </div>

              <div>
                <label for="register-password-confirm" class="block text-sm font-semibold text-gray-700">비밀번호 확인</label>
                <input
                  id="register-password-confirm"
                  v-model="form.confirmPassword"
                  type="password"
                  autocomplete="new-password"
                  maxlength="16"
                  placeholder="비밀번호를 다시 입력해주세요"
                  class="mt-2 w-full rounded-2xl border border-gray-200 px-4 py-3 focus:border-green-500 focus:ring-2 focus:ring-green-100"
                />
                <p v-if="passwordMismatchMessage" class="mt-1 text-sm text-red-500">{{ passwordMismatchMessage }}</p>
                <p v-else-if="errors.confirmPassword" class="mt-1 text-sm text-red-500">{{ errors.confirmPassword }}</p>
              </div>

              <div>
                <label for="register-name" class="block text-sm font-semibold text-gray-700">이름</label>
                <input
                  id="register-name"
                  v-model="form.name"
                  type="text"
                  autocomplete="name"
                  maxlength="30"
                  placeholder="홍길동"
                  class="mt-2 w-full rounded-2xl border border-gray-200 px-4 py-3 focus:border-green-500 focus:ring-2 focus:ring-green-100"
                />
                <p v-if="errors.name" class="mt-1 text-sm text-red-500">{{ errors.name }}</p>
                <p v-else class="mt-1 text-xs text-gray-500">한글 또는 영문으로 2~30자까지 입력할 수 있어요.</p>
              </div>

              <div>
                <label for="register-phone" class="block text-sm font-semibold text-gray-700">휴대폰 번호</label>
                <input
                  id="register-phone"
                  v-model="form.phone"
                  type="tel"
                  inputmode="numeric"
                  autocomplete="tel"
                  placeholder="010-0000-0000"
                  maxlength="13"
                  @input="onPhoneInput"
                  class="mt-2 w-full rounded-2xl border border-gray-200 px-4 py-3 focus:border-green-500 focus:ring-2 focus:ring-green-100"
                />
                <p v-if="errors.phone" class="mt-1 text-sm text-red-500">{{ errors.phone }}</p>
                <p v-else class="mt-1 text-xs text-gray-500">숫자만 입력하면 하이픈(-)은 자동으로 넣어드릴게요.</p>
              </div>
            </div>

            <button
              type="submit"
              class="w-full rounded-2xl bg-green-600 px-6 py-3 font-semibold text-white hover:bg-green-700 disabled:opacity-40 disabled:cursor-not-allowed"
              :disabled="isSubmitDisabled"
            >
              <span v-if="isSubmitting">가입 진행 중...</span>
              <span v-else>회원가입 완료</span>
            </button>
          </form>

          <div class="mt-8 text-sm text-center text-gray-600">
            <p>이미 계정이 있으신가요?</p>
            <button type="button" class="mt-2 font-semibold text-green-600 hover:text-green-700" @click="goToLogin">
              로그인하기
            </button>
          </div>
        </section>
      </div>
    </main>

    <AppFooter />
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
import { useRegisterForm } from '@/composables/useRegisterForm'

const router = useRouter()
const {
  form,
  errors,
  isSubmitting,
  serverError,
  successMessage,
  passwordMismatchMessage,
  isSubmitDisabled,
  handlePhoneInput,
  submitRegister,
} = useRegisterForm()

const goToLogin = () => {
  router.push({ name: 'login' })
}

const onPhoneInput = (event) => {
  handlePhoneInput(event.target.value)
}

const handleSubmit = async () => {
  const createdUser = await submitRegister()
  if (createdUser) {
    router.push({ name: 'login', query: { email: createdUser.email, registered: '1' } })
  }
}
</script>
