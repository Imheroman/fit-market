<template>
  <section class="bg-white shadow-lg rounded-2xl p-6 md:p-8 border border-gray-100 space-y-6">
    <div class="flex flex-col gap-1 md:flex-row md:items-center md:justify-between">
      <div>
        <h2 class="text-2xl font-semibold text-gray-900">배송지 관리</h2>
        <p class="text-sm text-gray-500">기본 배송지와 추가 주소를 자유롭게 관리하세요.</p>
      </div>
    </div>

    <p v-if="errorMessage" class="text-sm text-red-600 bg-red-50 border border-red-100 rounded-lg px-3 py-2">
      {{ errorMessage }}
    </p>

    <p
      v-if="isLimitReached"
      class="text-sm text-amber-700 bg-amber-50 border border-amber-100 rounded-lg px-3 py-2"
    >
      배송지는 최대 {{ maxCount }}개까지 등록할 수 있어요. 기존 배송지를 수정하거나 삭제해 주세요.
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
          :class="address.main ? 'border-green-300 bg-green-50/50' : 'border-gray-100 bg-white hover:border-green-200'"
        >
          <div class="flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
            <div>
              <div class="flex items-center gap-2">
                <p class="text-lg font-semibold">{{ address.name || address.recipient || '배송지' }}</p>
                <span v-if="address.main" class="text-xs px-2 py-0.5 rounded-full bg-green-100 text-green-700">기본</span>
              </div>
              <p class="text-sm text-gray-600">{{ address.recipient || '수령인 미입력' }} · {{ formatPhone(address.phone) }}</p>
              <p class="text-sm text-gray-500">
                {{ address.addressLine }} {{ address.addressLineDetail }}
                <span v-if="address.postalCode" class="text-gray-400">({{ address.postalCode }})</span>
              </p>
              <p v-if="address.memo" class="text-xs text-gray-400 mt-2">{{ address.memo }}</p>
            </div>
            <div class="flex flex-wrap gap-2 mt-4 md:mt-0">
              <button
                class="px-3 py-2 rounded-lg text-sm font-semibold border"
                :class="address.main ? 'border-gray-200 text-gray-400 cursor-not-allowed' : 'border-green-200 text-green-700 hover:bg-green-50'"
                :disabled="address.main || isLoading"
                @click="emit('set-default', address.id)"
              >
                기본으로 설정
              </button>
              <RouterLink
                v-if="address.id"
                :to="{ name: 'my-page-addresses-edit', params: { id: address.id } }"
                class="px-3 py-2 rounded-lg text-sm font-semibold border border-gray-200 text-gray-600 hover:bg-gray-50 inline-flex items-center justify-center"
              >
                수정
              </RouterLink>
              <button
                class="px-3 py-2 rounded-lg text-sm font-semibold border border-red-200 text-red-600 hover:bg-red-50"
                :disabled="isLoading"
                @click="emit('remove-address', address.id)"
              >
                삭제
              </button>
            </div>
          </div>
        </article>
      </template>
    </div>

    <p class="text-xs text-gray-500">배송지는 최대 {{ maxCount }}개까지 등록할 수 있어요.</p>

    <div class="flex justify-end pt-2">
      <RouterLink
        v-if="!isLimitReached"
        :to="{ name: 'my-page-addresses-new' }"
        class="px-4 py-2 rounded-lg text-sm font-semibold bg-green-600 text-white hover:bg-green-700 transition-colors"
      >
        새 배송지 등록
      </RouterLink>
      <button
        v-else
        type="button"
        class="px-4 py-2 rounded-lg text-sm font-semibold bg-gray-200 text-gray-500 cursor-not-allowed"
        disabled
      >
        새 배송지 등록
      </button>
    </div>
  </section>
</template>

<script setup>
import { formatPhoneNumber } from '@/utils/phone';
import { RouterLink } from 'vue-router';

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
  isLimitReached: {
    type: Boolean,
    default: false,
  },
  maxCount: {
    type: Number,
    default: 5,
  },
});

const formatPhone = (value) => formatPhoneNumber(value);

const emit = defineEmits(['set-default', 'remove-address']);
</script>
