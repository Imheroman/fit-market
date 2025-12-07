<template>
  <section class="bg-white shadow-lg rounded-2xl p-6 md:p-8 border border-green-100">
    <div class="flex flex-col gap-6">
      <div class="flex flex-col gap-2">
        <h2 class="text-xl font-semibold text-gray-900">요약 정보</h2>
        <p class="text-sm text-gray-500">로그인 상태와 기본 정보를 먼저 확인해요.</p>
      </div>

      <div v-if="isProfileLoading" class="text-center text-sm text-gray-500">회원 정보를 불러오는 중이에요.</div>

      <div v-else-if="profileError" class="text-center text-sm text-red-600 bg-red-50 border border-red-100 rounded-xl p-4">
        {{ profileError }}
      </div>

      <div v-else-if="isAuthenticated" class="grid gap-4 md:grid-cols-3">
        <div v-for="item in summaryItems" :key="item.label" class="p-4 rounded-xl bg-green-50 border border-green-100">
          <p class="text-sm text-gray-500">{{ item.label }}</p>
          <p class="text-lg font-semibold text-gray-900">{{ item.value }}</p>
        </div>
      </div>

      <div v-else class="text-center text-sm text-gray-500">로그인 후 마이페이지 기능을 이용해주세요.</div>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  user: {
    type: Object,
    default: null,
  },
  isAuthenticated: {
    type: Boolean,
    default: false,
  },
  isProfileLoading: {
    type: Boolean,
    default: false,
  },
  profileError: {
    type: String,
    default: '',
  },
  formattedCreatedDate: {
    type: String,
    default: '',
  },
});

const summaryItems = computed(() => [
  { label: '이름', value: props.user?.name ?? '-' },
  { label: '이메일', value: props.user?.email ?? '-' },
  { label: '가입일', value: props.formattedCreatedDate || '?' },
]);
</script>
