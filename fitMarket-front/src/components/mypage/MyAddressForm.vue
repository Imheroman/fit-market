<template>
  <section class="bg-white shadow-lg rounded-2xl p-6 md:p-8 border border-gray-100 space-y-6">
    <div class="flex flex-col gap-1 md:flex-row md:items-center md:justify-between">
      <div>
        <h2 class="text-2xl font-semibold text-gray-900">{{ title }}</h2>
        <p class="text-sm text-gray-500">받는 분, 주소, 요청 사항을 빠르게 입력해 주세요.</p>
      </div>
      <span class="text-xs px-3 py-1 rounded-full" :class="mode === 'edit' ? 'bg-blue-50 text-blue-700' : 'bg-green-50 text-green-700'">
        {{ mode === 'edit' ? '수정 중' : '신규 등록' }}
      </span>
    </div>

    <form class="space-y-4" @submit.prevent="handleSubmit">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <label class="flex flex-col gap-2 text-sm font-semibold text-gray-700">
          배송지 이름
          <input
            v-model="form.label"
            type="text"
            class="w-full rounded-lg border border-gray-200 px-3 py-2 focus:border-green-500 focus:ring-2 focus:ring-green-100 transition"
            placeholder="집, 회사, 부모님 댁"
            required
          />
        </label>
        <label class="flex flex-col gap-2 text-sm font-semibold text-gray-700">
          받는 분
          <input
            v-model="form.recipient"
            type="text"
            class="w-full rounded-lg border border-gray-200 px-3 py-2 focus:border-green-500 focus:ring-2 focus:ring-green-100 transition"
            placeholder="이름을 입력하세요"
            required
          />
        </label>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <label class="flex flex-col gap-2 text-sm font-semibold text-gray-700">
          연락처
          <input
            :value="form.phone"
            type="tel"
            inputmode="tel"
            class="w-full rounded-lg border border-gray-200 px-3 py-2 focus:border-green-500 focus:ring-2 focus:ring-green-100 transition"
            placeholder="010-1234-5678"
            @input="handlePhoneInput"
            required
          />
        </label>
        <label class="flex flex-col gap-2 text-sm font-semibold text-gray-700">
          배송지 메모
          <input
            v-model="form.instructions"
            type="text"
            class="w-full rounded-lg border border-gray-200 px-3 py-2 focus:border-green-500 focus:ring-2 focus:ring-green-100 transition"
            placeholder="부재 시 문 앞에 놓아주세요."
          />
        </label>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <label class="flex flex-col gap-2 text-sm font-semibold text-gray-700">
          기본 주소
          <input
            v-model="form.addressLine"
            type="text"
            class="w-full rounded-lg border border-gray-200 px-3 py-2 focus:border-green-500 focus:ring-2 focus:ring-green-100 transition"
            placeholder="도로명 주소를 입력하세요"
            required
          />
        </label>
        <label class="flex flex-col gap-2 text-sm font-semibold text-gray-700">
          상세 주소
          <input
            v-model="form.detailAddress"
            type="text"
            class="w-full rounded-lg border border-gray-200 px-3 py-2 focus:border-green-500 focus:ring-2 focus:ring-green-100 transition"
            placeholder="동/호수, 층 등을 입력하세요"
          />
        </label>
      </div>

      <div class="flex items-center gap-3">
        <input
          id="address-default"
          v-model="form.isDefault"
          type="checkbox"
          class="w-5 h-5 rounded border-gray-300 text-green-600 focus:ring-green-500"
        />
        <label for="address-default" class="text-sm text-gray-700">이 주소를 기본 배송지로 사용할게요.</label>
      </div>

      <p v-if="submitError" class="text-sm text-red-600 bg-red-50 border border-red-100 rounded-lg px-3 py-2">
        {{ submitError }}
      </p>

      <div class="flex flex-wrap gap-3 justify-end">
        <button
          v-if="showCancel"
          type="button"
          class="px-4 py-2 rounded-lg text-sm font-semibold border border-gray-200 text-gray-600 hover:bg-gray-50 transition-colors"
          @click="emit('cancel')"
        >
          취소
        </button>
        <button
          type="submit"
          class="px-5 py-3 rounded-lg text-sm font-semibold text-white bg-green-600 hover:bg-green-700 transition-colors disabled:opacity-60"
          :disabled="isSubmitting"
        >
          {{ isSubmitting ? '저장 중...' : actionLabel }}
        </button>
      </div>
    </form>
  </section>
</template>

<script setup>
import { computed, reactive, watch } from 'vue';
import { formatPhoneNumber, sanitizePhoneDigits } from '@/utils/phone';

const props = defineProps({
  mode: {
    type: String,
    default: 'create',
  },
  initialData: {
    type: Object,
    default: () => ({}),
  },
  isSubmitting: {
    type: Boolean,
    default: false,
  },
  submitError: {
    type: String,
    default: '',
  },
});

const emit = defineEmits(['submit', 'cancel']);

const form = reactive({
  label: '',
  recipient: '',
  phone: '',
  addressLine: '',
  detailAddress: '',
  instructions: '',
  isDefault: false,
});

const title = computed(() => (props.mode === 'edit' ? '배송지 수정' : '새 배송지 추가'));
const actionLabel = computed(() => (props.mode === 'edit' ? '배송지 수정하기' : '배송지 추가하기'));
const showCancel = computed(() => props.mode === 'edit');

const syncForm = (data) => {
  form.label = data?.label ?? '';
  form.recipient = data?.recipient ?? '';
  form.phone = formatPhoneNumber(data?.phone ?? '');
  form.addressLine = data?.addressLine ?? '';
  form.detailAddress = data?.detailAddress ?? '';
  form.instructions = data?.instructions ?? '';
  form.isDefault = Boolean(data?.isDefault);
};

watch(
  () => props.initialData,
  (value) => {
    syncForm(value ?? {});
  },
  { immediate: true, deep: true }
);

const handlePhoneInput = (event) => {
  const digits = sanitizePhoneDigits(event.target.value);
  form.phone = formatPhoneNumber(digits);
};

const handleSubmit = () => {
  emit('submit', {
    ...form,
    phone: sanitizePhoneDigits(form.phone),
  });
};
</script>
