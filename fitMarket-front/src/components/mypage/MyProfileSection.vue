<template>
  <section class="bg-white shadow-lg rounded-2xl p-6 md:p-8 border border-gray-100">
    <div class="flex flex-col gap-6">
      <div class="space-y-1">
        <h2 class="text-2xl font-semibold text-gray-900">기본 정보</h2>
        <p class="text-sm text-gray-500">회원정보를 확인하고 수정하거나 탈퇴할 수 있어요.</p>
      </div>

      <div v-if="isProfileLoading" class="text-sm text-gray-500">회원 정보를 불러오고 있어요.</div>

      <div v-else-if="profileError" class="text-sm text-red-600 bg-red-50 border border-red-100 rounded-xl p-4">
        {{ profileError }}
      </div>

      <div v-else-if="!isAuthenticated" class="text-sm text-gray-500">로그인하면 내 정보가 채워져요.</div>

      <div v-else class="grid gap-4 md:grid-cols-2">
        <div v-for="item in profileFields" :key="item.label" class="p-4 rounded-xl bg-gray-50 border border-gray-100">
          <p class="text-sm text-gray-500">{{ item.label }}</p>
          <p class="text-lg font-semibold text-gray-900">{{ item.value }}</p>
        </div>
      </div>

      <div v-if="isAuthenticated" class="flex flex-col gap-3 md:flex-row">
        <button
          type="button"
          class="flex-1 bg-green-600 text-white px-6 py-3 rounded-xl font-semibold hover:bg-green-700 transition-colors"
          @click="emit('edit-profile')"
        >
          회원 정보 수정
        </button>
        <button
          type="button"
          class="flex-1 border border-red-200 text-red-600 px-6 py-3 rounded-xl font-semibold hover:bg-red-50 transition-colors"
          @click="emit('delete-account')"
        >
          회원 탈퇴
        </button>
      </div>
    </div>
  </section>
</template>

<script setup>
defineProps({
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
  profileFields: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits(['edit-profile', 'delete-account']);
</script>
