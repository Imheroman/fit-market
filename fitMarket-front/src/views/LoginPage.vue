<template>
    <div class="min-h-screen bg-gray-50 flex flex-col">
        <AppHeader/>

        <main class="flex-1">
            <div class="container mx-auto px-4 py-16 max-w-xl">
                <section class="bg-white border border-green-100 rounded-3xl shadow-xl p-8">
                    <div class="space-y-3 text-center">
                        <p class="text-sm font-semibold text-green-600">로그인</p>
                        <h1 class="text-3xl font-bold text-gray-900">초록초록에 다시 오신 걸 환영해요</h1>
                        <p class="text-gray-500">이메일과 비밀번호만 입력하면 내 장바구니와 주문 내역을 바로 불러올게요.</p>
                    </div>

                    <form class="mt-10 space-y-6" @submit.prevent="handleSubmit">
                        <div v-if="serverError"
                             class="rounded-2xl border border-red-200 bg-red-50 p-4 text-sm text-red-600">
                            {{ serverError }}
                        </div>
                        <div v-if="successMessage"
                             class="rounded-2xl border border-green-200 bg-green-50 p-4 text-sm text-green-700">
                            {{ successMessage }}
                        </div>

                        <div class="space-y-5">
                            <div>
                                <label for="login-email" class="block text-sm font-semibold text-gray-700">이메일</label>
                                <input
                                    id="login-email"
                                    v-model="form.email"
                                    type="email"
                                    autocomplete="email"
                                    placeholder="example@fitmarket.com"
                                    class="mt-2 w-full rounded-2xl border border-gray-200 px-4 py-3 focus:border-green-500 focus:ring-2 focus:ring-green-100"
                                />
                                <p v-if="errors.email" class="mt-1 text-sm text-red-500">{{ errors.email }}</p>
                            </div>

                            <div>
                                <label for="login-password"
                                       class="flex items-center justify-between text-sm font-semibold text-gray-700">
                                    비밀번호
                                    <span class="text-xs font-normal text-gray-400">8자 이상 입력해주세요</span>
                                </label>
                                <input
                                    id="login-password"
                                    v-model="form.password"
                                    type="password"
                                    autocomplete="current-password"
                                    placeholder="••••••••"
                                    class="mt-2 w-full rounded-2xl border border-gray-200 px-4 py-3 focus:border-green-500 focus:ring-2 focus:ring-green-100"
                                />
                                <p v-if="errors.password" class="mt-1 text-sm text-red-500">{{ errors.password }}</p>
                            </div>
                        </div>

                        <button
                            type="submit"
                            class="w-full rounded-2xl bg-green-600 px-6 py-3 font-semibold text-white hover:bg-green-700 disabled:opacity-40 disabled:cursor-not-allowed"
                            :disabled="isSubmitting"
                        >
                            <span v-if="isSubmitting">로그인 중...</span>
                            <span v-else>로그인</span>
                        </button>
                    </form>

                    <div class="mt-8 text-sm text-center text-gray-600">
                        <p>아직 회원이 아니신가요?</p>
                        <button type="button" class="mt-2 font-semibold text-green-600 hover:text-green-700"
                                @click="goToSignup">
                            1분만에 회원가입하기
                        </button>
                    </div>
                </section>
            </div>
        </main>

        <AppFooter/>
    </div>
</template>

<script setup>
import {onMounted} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
import {useLoginForm} from '@/composables/useLoginForm'

const router = useRouter()
const route = useRoute()
const {form, errors, isSubmitting, serverError, successMessage, submitLogin} = useLoginForm()

onMounted(() => {
    let shouldCleanupQuery = false

    if (route.query.email) {
        form.email = String(route.query.email)
        shouldCleanupQuery = true
    }

    if (route.query.registered === '1') {
        successMessage.value = '회원가입이 완료되었어요. 로그인 후 서비스를 즐겨보세요.'
        shouldCleanupQuery = true
    }

    if (shouldCleanupQuery) {
        router.replace({name: 'login'})
    }
})

const goToSignup = () => {
    router.push({name: 'signup'})
}

const handleSubmit = async () => {
    const session = await submitLogin()
    if (session) {
        router.push({name: 'home'});
    }
}
</script>
