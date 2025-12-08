<template>
  <section class="bg-white shadow-lg rounded-2xl p-6 md:p-8 border border-gray-100 space-y-6">
    <div class="flex flex-col gap-1 md:flex-row md:items-center md:justify-between">
      <div>
        <h2 class="text-2xl font-semibold text-gray-900">배송지 관리</h2>
        <p class="text-sm text-gray-500">기본 배송지와 추가 주소를 자유롭게 관리하세요.</p>
      </div>
      <button
        class="px-4 py-2 rounded-lg text-sm font-semibold bg-green-600 text-white hover:bg-green-700 transition-colors"
        @click="emit('add-address')"
      >
        새 배송지 등록
      </button>
    </div>

    <p v-if="errorMessage" class="text-sm text-red-600 bg-red-50 border border-red-100 rounded-lg px-3 py-2">
      {{ errorMessage }}
    </p>

    <div class="space-y-4">
      <div
        v-if="isLoading"
        class="border border-dashed border-green-200 rounded-2xl p-5 text-sm text-gray-500 bg-green-50/40"
      >
        배송지를 불러오는 중이에요. 잠시만 기다려 주세요.
      </div>

      <div
        v-else-if="!addresses.length"
        class="border border-dashed border-gray-200 rounded-2xl p-5 text-sm text-gray-500 bg-gray-50"
      >
        등록된 배송지가 없어요. 새 배송지를 추가해 주세요.
      </div>

      <template v-else>
        <article
          v-for="address in addresses"
          :key="address.id"
          class="border rounded-2xl p-5 transition-all"
          :class="address.isDefault ? 'border-green-300 bg-green-50/50' : 'border-gray-100 bg-white hover:border-green-200'"
        >
          <div class="flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
            <div>
              <div class="flex items-center gap-2">
                <p class="text-lg font-semibold">{{ address.label }}</p>
                <span v-if="address.isDefault" class="text-xs px-2 py-0.5 rounded-full bg-green-100 text-green-700">기본</span>
              </div>
              <p class="text-sm text-gray-600">{{ address.recipient }} · {{ formatPhone(address.phone) }}</p>
              <p class="text-sm text-gray-500">{{ address.addressLine }} {{ address.detailAddress }}</p>
              <p class="text-xs text-gray-400 mt-2">{{ address.instructions }}</p>
            </div>
            <div class="flex flex-wrap gap-2 mt-4 md:mt-0">
              <button
                class="px-3 py-2 rounded-lg text-sm font-semibold border"
                :class="address.isDefault ? 'border-gray-200 text-gray-400 cursor-not-allowed' : 'border-green-200 text-green-700 hover:bg-green-50'"
                :disabled="address.isDefault || isLoading"
                @click="emit('set-default', address.id)"
              >
                기본으로 설정
              </button>
              <button
                class="px-3 py-2 rounded-lg text-sm font-semibold border border-gray-200 text-gray-600 hover:bg-gray-50"
                :disabled="isLoading"
                @click="emit('edit-address', address)"
              >
                수정
              </button>
              <button
                class="px-3 py-2 rounded-lg text-sm font-semibold border border-red-200 text-red-600 hover:bg-red-50"
                :disabled="addresses.length <= 1 || isLoading"
                @click="emit('remove-address', address.id)"
              >
                삭제
              </button>
            </div>
          </div>
        </article>
      </template>
    </div>

    <p class="text-xs text-gray-500">배송지는 최소 1개 이상 유지되어야 해요.</p>
  </section>
</template>

<script setup>
import { formatPhoneNumber } from '@/utils/phone';

defineProps({
  addresses: {
    type: Array,
    default: () => [],
  },
  isLoading: {
    type: Boolean,
    default: false,
  },
  errorMessage: {
    type: String,
    default: '',
  },
});

const formatPhone = (value) => formatPhoneNumber(value);

const emit = defineEmits(['add-address', 'edit-address', 'set-default', 'remove-address']);
</script>
