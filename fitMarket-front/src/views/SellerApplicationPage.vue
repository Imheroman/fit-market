<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <AppHeader />

    <main class="flex-1">
      <div class="container mx-auto px-4 py-10 max-w-3xl">
        <div class="space-y-8">
          <!-- 헤더 -->
          <header class="text-center space-y-3">
            <p class="text-sm font-semibold text-green-600">판매자 등록</p>
            <h1 class="text-3xl md:text-4xl font-bold text-gray-900">판매자 신청하기</h1>
            <p class="text-gray-500">
              건강식품을 판매하고 싶으신가요? 아래 정보를 입력하고 신청해주세요.
            </p>
          </header>

          <!-- 성공 메시지 -->
          <div
            v-if="successMessage"
            class="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-lg flex items-center gap-2"
          >
            <CheckCircle2 class="w-5 h-5" />
            <span>{{ successMessage }}</span>
          </div>

          <!-- 에러 메시지 -->
          <div
            v-if="errorMessage"
            class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg flex items-center gap-2"
          >
            <AlertCircle class="w-5 h-5" />
            <span>{{ errorMessage }}</span>
          </div>

          <!-- 신청 폼 -->
          <form @submit.prevent="handleSubmit" class="bg-white shadow-lg rounded-2xl p-6 md:p-8 border border-gray-200">
            <div class="space-y-6">
              <h2 class="text-xl font-semibold text-gray-900 pb-3 border-b border-gray-200">
                사업자 정보
              </h2>

              <!-- 상호명 -->
              <div>
                <label for="businessName" class="block text-sm font-medium text-gray-700 mb-2">
                  상호명 <span class="text-red-500">*</span>
                </label>
                <input
                  id="businessName"
                  v-model="form.businessName"
                  type="text"
                  placeholder="예: 건강한 밥상"
                  class="w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
                  :class="errors.businessName ? 'border-red-300' : 'border-gray-300'"
                />
                <p v-if="errors.businessName" class="mt-1 text-sm text-red-600">
                  {{ errors.businessName }}
                </p>
              </div>

              <!-- 사업자등록번호 -->
              <div>
                <label for="businessNumber" class="block text-sm font-medium text-gray-700 mb-2">
                  사업자등록번호 <span class="text-red-500">*</span>
                </label>
                <input
                  id="businessNumber"
                  v-model="form.businessNumber"
                  type="text"
                  placeholder="123-45-67890"
                  class="w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
                  :class="errors.businessNumber ? 'border-red-300' : 'border-gray-300'"
                />
                <p v-if="errors.businessNumber" class="mt-1 text-sm text-red-600">
                  {{ errors.businessNumber }}
                </p>
              </div>

              <!-- 사업자 유형 -->
              <div>
                <label for="businessType" class="block text-sm font-medium text-gray-700 mb-2">
                  사업자 유형 <span class="text-red-500">*</span>
                </label>
                <select
                  id="businessType"
                  v-model="form.businessType"
                  class="w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
                  :class="errors.businessType ? 'border-red-300' : 'border-gray-300'"
                >
                  <option value="">선택해주세요</option>
                  <option value="individual">개인사업자</option>
                  <option value="corporation">법인사업자</option>
                </select>
                <p v-if="errors.businessType" class="mt-1 text-sm text-red-600">
                  {{ errors.businessType }}
                </p>
              </div>

              <!-- 연락처 -->
              <div>
                <label for="contactPhone" class="block text-sm font-medium text-gray-700 mb-2">
                  대표 연락처 <span class="text-red-500">*</span>
                </label>
                <input
                  id="contactPhone"
                  v-model="form.contactPhone"
                  type="text"
                  placeholder="010-1234-5678"
                  class="w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
                  :class="errors.contactPhone ? 'border-red-300' : 'border-gray-300'"
                />
                <p v-if="errors.contactPhone" class="mt-1 text-sm text-red-600">
                  {{ errors.contactPhone }}
                </p>
              </div>

              <!-- 사업장 주소 -->
              <div>
                <label for="businessAddress" class="block text-sm font-medium text-gray-700 mb-2">
                  사업장 주소 <span class="text-red-500">*</span>
                </label>
                <input
                  id="businessAddress"
                  v-model="form.businessAddress"
                  type="text"
                  placeholder="서울시 강남구 테헤란로 123"
                  class="w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
                  :class="errors.businessAddress ? 'border-red-300' : 'border-gray-300'"
                />
                <p v-if="errors.businessAddress" class="mt-1 text-sm text-red-600">
                  {{ errors.businessAddress }}
                </p>
              </div>

              <!-- 사업 소개 -->
              <div>
                <label for="introduction" class="block text-sm font-medium text-gray-700 mb-2">
                  사업 소개 <span class="text-red-500">*</span>
                </label>
                <textarea
                  id="introduction"
                  v-model="form.introduction"
                  rows="5"
                  placeholder="어떤 상품을 판매하시나요? 특징과 강점을 소개해주세요. (최소 20자)"
                  class="w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 resize-none"
                  :class="errors.introduction ? 'border-red-300' : 'border-gray-300'"
                ></textarea>
                <p v-if="errors.introduction" class="mt-1 text-sm text-red-600">
                  {{ errors.introduction }}
                </p>
                <p class="mt-1 text-sm text-gray-500">
                  {{ form.introduction.length }} / 20자 이상
                </p>
              </div>

              <!-- 버튼 -->
              <div class="flex gap-3 pt-4">
                <button
                  type="button"
                  @click="handleReset"
                  class="flex-1 px-6 py-3 bg-gray-100 text-gray-700 rounded-lg font-medium hover:bg-gray-200 transition-colors"
                  :disabled="isSubmitting"
                >
                  초기화
                </button>
                <button
                  type="submit"
                  class="flex-1 px-6 py-3 bg-green-600 text-white rounded-lg font-medium hover:bg-green-700 transition-colors disabled:bg-gray-300 disabled:cursor-not-allowed flex items-center justify-center gap-2"
                  :disabled="isSubmitting"
                >
                  <Loader2 v-if="isSubmitting" class="w-5 h-5 animate-spin" />
                  <span>{{ isSubmitting ? '신청 중...' : '판매자 신청하기' }}</span>
                </button>
              </div>

              <p class="text-sm text-gray-500 text-center">
                신청 후 1-2일 내에 검토 결과를 이메일로 알려드립니다.
              </p>
            </div>
          </form>
        </div>
      </div>
    </main>

    <AppFooter />
  </div>
</template>

<script setup>
import { CheckCircle2, AlertCircle, Loader2 } from 'lucide-vue-next'
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
import { useSellerApplication } from '@/composables/useSellerApplication'
import { useRouter } from 'vue-router'

const router = useRouter()
const { form, errors, isSubmitting, successMessage, errorMessage, submitApplication, resetForm } =
  useSellerApplication()

const handleSubmit = async () => {
  const success = await submitApplication()
  if (success) {
    setTimeout(() => {
      router.push('/mypage')
    }, 2000)
  }
}

const handleReset = () => {
  if (confirm('입력한 내용을 모두 초기화하시겠습니까?')) {
    resetForm()
  }
}
</script>
