<template>
    <section class="bg-white shadow-lg rounded-2xl p-6 md:p-8 border border-gray-100 space-y-6">
        <div class="flex flex-col gap-1 md:flex-row md:items-center md:justify-between">
            <div>
                <h2 class="text-2xl font-semibold text-gray-900">{{ title }}</h2>
                <p class="text-sm text-gray-500">받는 분, 주소, 메모를 빠르게 입력해 주세요.</p>
            </div>
            <span class="text-xs px-3 py-1 rounded-full"
                  :class="mode === 'edit' ? 'bg-blue-50 text-blue-700' : 'bg-green-50 text-green-700'">
        {{ mode === 'edit' ? '수정 중' : '신규 등록' }}
      </span>
        </div>

        <form class="space-y-4" @submit.prevent="handleSubmit">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <label class="flex flex-col gap-2 text-sm font-semibold text-gray-700">
                    주소지 이름
                    <div class="relative">
                        <input
                            v-model="form.name"
                            type="text"
                            maxlength="100"
                            class="w-full rounded-lg border border-gray-200 px-3 py-2 pr-10 focus:border-green-500 focus:ring-2 focus:ring-green-100 transition"
                            placeholder="집, 회사, 부모님 댁"
                            required
                        />
                        <button
                            v-if="form.name"
                            type="button"
                            class="absolute right-2 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                            @click="handleClear('name')"
                        >
                            X
                        </button>
                    </div>
                </label>
                <label class="flex flex-col gap-2 text-sm font-semibold text-gray-700">
                    받는 분
                    <div class="relative">
                        <input
                            v-model="form.recipient"
                            type="text"
                            maxlength="100"
                            class="w-full rounded-lg border border-gray-200 px-3 py-2 pr-10 focus:border-green-500 focus:ring-2 focus:ring-green-100 transition"
                            placeholder="이름을 입력하세요"
                            required
                        />
                        <button
                            v-if="form.recipient"
                            type="button"
                            class="absolute right-2 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                            @click="handleClear('recipient')"
                        >
                            X
                        </button>
                    </div>
                </label>
                <label class="flex flex-col gap-2 text-sm font-semibold text-gray-700">
                    연락처
                    <div class="relative">
                        <input
                            :value="form.phone"
                            type="tel"
                            inputmode="numeric"
                            maxlength="13"
                            class="w-full rounded-lg border border-gray-200 px-3 py-2 pr-10 focus:border-green-500 focus:ring-2 focus:ring-green-100 transition"
                            placeholder="010-1234-5678"
                            @input="handlePhoneInput"
                            required
                        />
                        <button
                            v-if="form.phone"
                            type="button"
                            class="absolute right-2 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                            @click="handleClear('phone')"
                        >
                            X
                        </button>
                    </div>
                    <p class="text-xs text-gray-500">숫자 11자리만 입력하면 자동으로 하이픈을 넣어드려요.</p>
                </label>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <label class="flex flex-col gap-2 text-sm font-semibold text-gray-700">
                    우편번호
                    <div class="relative">
                        <input
                            v-model="form.postalCode"
                            type="text"
                            maxlength="15"
                            class="w-full rounded-lg border border-gray-200 px-3 py-2 pr-10 focus:border-green-500 focus:ring-2 focus:ring-green-100 transition"
                            placeholder="예: 06236"
                        />
                        <button
                            v-if="form.postalCode"
                            type="button"
                            class="absolute right-2 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                            @click="handleClear('postalCode')"
                        >
                            X
                        </button>
                    </div>
                </label>
                <label class="flex flex-col gap-2 text-sm font-semibold text-gray-700">
                    기본 주소
                    <div class="relative">
                        <input
                            v-model="form.addressLine"
                            type="text"
                            class="w-full rounded-lg border border-gray-200 px-3 py-2 pr-10 focus:border-green-500 focus:ring-2 focus:ring-green-100 transition"
                            placeholder="도로명 주소를 입력하세요"
                            required
                        />
                        <button
                            v-if="form.addressLine"
                            type="button"
                            class="absolute right-2 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                            @click="handleClear('addressLine')"
                        >
                            X
                        </button>
                    </div>
                </label>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <label class="flex flex-col gap-2 text-sm font-semibold text-gray-700">
                    상세 주소
                    <div class="relative">
                        <input
                            v-model="form.addressLineDetail"
                            type="text"
                            class="w-full rounded-lg border border-gray-200 px-3 py-2 pr-10 focus:border-green-500 focus:ring-2 focus:ring-green-100 transition"
                            placeholder="동/호수, 층 등을 입력하세요"
                            required
                        />
                        <button
                            v-if="form.addressLineDetail"
                            type="button"
                            class="absolute right-2 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                            @click="handleClear('addressLineDetail')"
                        >
                            X
                        </button>
                    </div>
                </label>
                <label class="flex flex-col gap-2 text-sm font-semibold text-gray-700">
                    배송 메모
                    <div class="relative">
                        <input
                            v-model="form.memo"
                            type="text"
                            maxlength="200"
                            class="w-full rounded-lg border border-gray-200 px-3 py-2 pr-10 focus:border-green-500 focus:ring-2 focus:ring-green-100 transition"
                            placeholder="부재 시 문 앞에 놓아주세요."
                        />
                        <button
                            v-if="form.memo"
                            type="button"
                            class="absolute right-2 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                            @click="handleClear('memo')"
                        >
                            X
                        </button>
                    </div>
                </label>
            </div>

            <div class="flex flex-col gap-1">
                <div class="flex items-center gap-3">
                    <input
                        id="address-default"
                        v-model="form.main"
                        type="checkbox"
                        class="w-5 h-5 rounded border-gray-300 text-green-600 focus:ring-green-500 disabled:cursor-not-allowed disabled:opacity-70"
                        :disabled="isMainLocked"
                    />
                    <label for="address-default" class="text-sm text-gray-700">이 주소를 기본 배송지로 사용할게요.</label>
                </div>
                <p class="text-xs text-gray-500">등록이나 수정할 때 바로 기본 배송지로 지정할 수 있어요.</p>
            </div>

            <p v-if="displayError" class="text-sm text-red-600 bg-red-50 border border-red-100 rounded-lg px-3 py-2">
                {{ displayError }}
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
                    :disabled="isSubmitting || isSubmitDisabled"
                >
                    {{ isSubmitting ? '저장 중...' : actionLabel }}
                </button>
            </div>
        </form>
    </section>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {formatPhoneNumber, sanitizePhoneDigits} from '@/utils/phone';

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
    isMainLocked: {
        type: Boolean,
        default: false,
    },
    isSubmitDisabled: {
        type: Boolean,
        default: false,
    },
});

const emit = defineEmits(['submit', 'cancel']);

const form = reactive({
    name: '',
    recipient: '',
    phone: '',
    postalCode: '',
    addressLine: '',
    addressLineDetail: '',
    memo: '',
    main: false,
});

const title = computed(() => (props.mode === 'edit' ? '배송지 수정' : '새 배송지 추가'));
const actionLabel = computed(() => (props.mode === 'edit' ? '배송지 수정하기' : '배송지 추가하기'));
const showCancel = computed(() => props.mode === 'edit');
const localError = ref('');
const displayError = computed(() => props.submitError || localError.value);

const syncForm = (data) => {
    form.name = data?.name ?? data?.label ?? '';
    form.recipient = data?.recipient ?? '';
    const digits = sanitizePhoneDigits(data?.phone ?? '').slice(0, 11);
    form.phone = digits ? formatPhoneNumber(digits) : '';
    form.postalCode = data?.postalCode ?? '';
    form.addressLine = data?.addressLine ?? '';
    form.addressLineDetail = data?.addressLineDetail ?? data?.detailAddress ?? '';
    form.memo = data?.memo ?? data?.instructions ?? '';
    form.main = props.isMainLocked ? true : Boolean(data?.main ?? data?.isDefault);
    localError.value = '';
};

watch(
    () => props.initialData,
    (value) => {
        syncForm(value ?? {});
    },
    {immediate: true, deep: true}
);

watch(
    () => props.isMainLocked,
    (locked) => {
        form.main = locked ? true : Boolean(props.initialData?.main ?? props.initialData?.isDefault);
    },
    {immediate: true}
);

const handlePhoneInput = (event) => {
    const digits = sanitizePhoneDigits(event.target.value).slice(0, 11);
    form.phone = formatPhoneNumber(digits);
    if (localError.value) {
        localError.value = '';
    }
};

const handleSubmit = () => {
    localError.value = '';
    if (!form.addressLine.trim()) {
        localError.value = '도로명 주소를 입력해 주세요.';
        return;
    }
    if (!form.addressLineDetail.trim()) {
        localError.value = '상세 주소를 입력해 주세요.';
        return;
    }

    const phone = sanitizePhoneDigits(form.phone).slice(0, 11);
    if (phone.length !== 11) {
        localError.value = '연락처는 숫자 11자리로 입력해 주세요.';
        return;
    }

    console.log("submit main:", form.main);

    emit('submit', {
        ...form,
        phone,
        main: form.main,
    });
};

const handleClear = (field) => {
    if (!Object.prototype.hasOwnProperty.call(form, field)) return;
    form[field] = '';
    if (field === 'phone') {
        localError.value = '';
    }
};
</script>
