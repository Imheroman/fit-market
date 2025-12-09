<template>
  <div class="min-h-screen bg-gradient-to-b from-green-50 to-white">
    <AppHeader />

    <div class="container mx-auto px-4 py-10">
      <div class="max-w-5xl mx-auto space-y-8">
        <section class="bg-white border border-green-100 rounded-2xl p-8 shadow-sm">
          <div class="flex flex-col gap-6 md:flex-row md:items-center md:justify-between">
            <div>
              <p
                class="text-sm font-semibold mb-2 flex items-center gap-2"
                :class="isCancelled ? 'text-red-600' : 'text-green-600'"
              >
                <component :is="isCancelled ? AlertTriangle : CheckCircle" class="w-4 h-4" />
                {{ isCancelled ? '주문이 취소되었어요' : '결제가 끝났어요' }}
              </p>
              <h1 class="text-3xl font-bold mb-2">
                {{ isCancelled ? '주문이 취소되었습니다' : '주문이 완료되었어요' }}
              </h1>
              <p class="text-gray-600">
                주문번호 {{ orderNumber }} ·
                <span v-if="isCancelled">취소 완료</span>
                <span v-else>총 {{ cartItems.length }}개 상품</span>
              </p>
            </div>
            <div class="text-right">
              <p class="text-sm text-gray-500">{{ isCancelled ? '환불 예정 금액' : '최종 결제 금액' }}</p>
              <p :class="['text-3xl font-bold', isCancelled ? 'text-red-500' : 'text-green-600']">
                {{ totalPayment.toLocaleString() }}원
              </p>
              <p class="text-sm text-gray-500 mt-1">
                {{ isCancelled ? '환불은 영업일 기준 3일 이내 완료돼요.' : '배송비 3,000원 포함' }}
              </p>
            </div>
          </div>
        </section>

        <section class="space-y-6">
          <div class="bg-white border border-green-100 rounded-2xl p-6">
            <div class="flex flex-col gap-2 md:flex-row md:items-start md:justify-between mb-6">
              <div>
                <h2 class="text-xl font-bold">배송지 정보</h2>
                <p class="text-sm text-gray-500">
                  {{ isCancelled ? '주문이 취소되어 배송지를 수정할 수 없어요.' : '결제 완료 상태일 때만 배송지 변경을 요청할 수 있어요.' }}
                </p>
              </div>
              <span
                class="inline-flex items-center gap-2 text-sm px-3 py-1 rounded-full"
                :class="canEditAddress ? 'text-green-600 bg-green-50' : 'text-gray-500 bg-gray-100'"
              >
                <MapPin class="w-4 h-4" />
                {{ formattedSelectedAddress?.name || formattedSelectedAddress?.recipient || '배송지' }}
                {{ canEditAddress ? '수정 가능' : isCancelled ? '배송 중단' : '수정 잠금' }}
              </span>
            </div>

            <p
              v-if="isAddressLoading"
              class="text-sm text-gray-600 bg-green-50 border border-green-100 rounded-lg px-3 py-2 mb-4"
            >
              배송지 정보를 불러오는 중이에요.
            </p>
            <p
              v-else-if="addressErrorMessage"
              class="text-sm text-red-600 bg-red-50 border border-red-100 rounded-lg px-3 py-2 mb-4"
            >
              {{ addressErrorMessage }}
            </p>

            <div class="border border-green-100 rounded-2xl p-5 bg-gradient-to-br from-green-50/60 to-white">
              <div class="flex flex-col gap-2">
                <p class="text-sm font-semibold text-gray-500 uppercase tracking-wide">받는 분</p>
                <p class="text-lg font-bold">{{ formattedSelectedAddress?.recipient }} · {{ formattedSelectedAddress?.phone }}</p>
                <p class="text-gray-700">
                  {{ formattedSelectedAddress?.addressLine }} {{ formattedSelectedAddress?.addressLineDetail }}
                  <span v-if="formattedSelectedAddress?.postalCode" class="text-gray-400">({{ formattedSelectedAddress?.postalCode }})</span>
                </p>
                <p class="text-sm text-gray-500">배송 메모: {{ formattedSelectedAddress?.memo || '입력된 메모가 없어요.' }}</p>
              </div>
            </div>

            <div class="mt-5 space-y-3">
              <div class="flex items-start gap-3 text-sm">
                <AlertTriangle class="w-4 h-4 mt-0.5" :class="canEditAddress ? 'text-green-600' : 'text-gray-400'" />
                <p class="text-gray-600">
                  {{ canEditAddress
                    ? '상품 준비가 시작되기 전까지 배송지 변경이 가능해요. 필요하면 아래 버튼을 눌러 요청해 주세요.'
                    : '배송이 준비 중이거나 주문이 취소되어 배송지를 바꿀 수 없어요.' }}
                </p>
              </div>
              <button
                class="w-full md:w-auto px-5 py-3 rounded-lg font-semibold flex items-center justify-center gap-2 transition-colors"
                :class="canEditAddress ? 'bg-green-600 hover:bg-green-700 text-white' : 'bg-gray-100 text-gray-400 cursor-not-allowed'"
                :disabled="!canEditAddress"
                @click="handleEditAddress"
              >
                <Edit3 class="w-4 h-4" />
                배송지 변경 요청하기
              </button>
            </div>
          </div>

          <div class="bg-white border border-green-100 rounded-2xl p-6">
            <div class="flex items-center justify-between mb-6">
              <div>
                <h2 class="text-xl font-bold">결제 상태</h2>
                <p class="text-sm text-gray-500">
                  {{ isCancelled ? '결제와 주문이 모두 취소되었어요.' : '결제 완료 상태에서만 무료 취소가 가능해요.' }}
                </p>
              </div>
              <span
                class="inline-flex items-center gap-2 text-sm px-3 py-1 rounded-full"
                :class="isCancelled ? 'text-red-600 bg-red-50' : 'text-blue-600 bg-blue-50'"
              >
                <ShieldCheck class="w-4 h-4" />
                {{ isCancelled ? '취소 완료' : '안전결제 보장' }}
              </span>
            </div>

            <div class="space-y-4">
              <div
                class="border rounded-xl p-4"
                :class="isCancelled ? 'border-red-200 bg-red-50/80' : 'border-green-500 bg-green-50/70'"
              >
                <div class="flex items-center gap-2 mb-2">
                  <BadgeCheck
                    class="w-5 h-5"
                    :class="isCancelled ? 'text-red-500' : 'text-green-600'"
                  />
                  <p class="font-semibold">{{ isCancelled ? '결제 취소 완료' : '결제 완료' }}</p>
                  <span class="text-xs" :class="isCancelled ? 'text-red-600' : 'text-green-600'">
                    {{ isCancelled ? '환불 대기' : '무료취소 가능' }}
                  </span>
                </div>
                <p class="text-sm text-gray-600">
                  {{ isCancelled ? '취소 접수가 끝났어요. 영업일 기준 3일 내로 환불해 드릴게요.' : '필요하면 지금 바로 무료로 취소할 수 있어요.' }}
                </p>
                <button
                  v-if="!isCancelled"
                  class="mt-3 text-sm text-green-700 font-semibold hover:underline"
                  @click="handleCancelRequest"
                >
                  결제 취소 요청하기
                </button>
                <p v-else class="mt-3 text-xs text-gray-500">환불 상태는 알림으로 다시 알려드릴게요.</p>
              </div>

              <div v-if="!isCancelled" class="border rounded-xl p-4 border-yellow-200 bg-yellow-50/70">
                <div class="flex items-center gap-2 mb-2">
                  <PackageCheck class="w-5 h-5 text-yellow-600" />
                  <p class="font-semibold">주문 접수 · 상품 준비</p>
                </div>
                <p class="text-sm text-gray-600">상품 준비가 시작되면 취소 시 수수료가 발생해요.</p>
                <p class="text-xs text-gray-500 mt-2">주문 완료 이후 취소 수수료: {{ cancellationFee.toLocaleString() }}원</p>
              </div>

              <div v-else class="border rounded-xl p-4 border-gray-200 bg-gray-50">
                <p class="font-semibold text-gray-700 mb-1">주문이 안전하게 취소되었어요.</p>
                <p class="text-sm text-gray-600">배송 준비는 중단되었으며, 환불이 완료되면 알림으로 알려드릴게요.</p>
              </div>
            </div>
          </div>
        </section>

        <section class="bg-white border border-green-100 rounded-2xl p-6">
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-xl font-bold">주문한 상품</h2>
            <p class="text-sm text-gray-500">총 {{ cartItems.length }}건</p>
          </div>

          <div class="divide-y divide-green-100">
            <div
              v-for="item in cartItems"
              :key="item.id"
              class="py-4 flex items-center gap-4 cursor-pointer transition-colors hover:bg-green-50/70 px-2 rounded-xl"
              @click="navigateToProduct(item.id)"
            >
              <img :src="item.image" :alt="item.name" class="w-20 h-20 rounded-xl object-cover bg-green-50" />
              <div class="flex-1">
                <p class="font-semibold">{{ item.name }}</p>
                <p class="text-sm text-gray-500">{{ item.category }} · {{ item.calories * item.quantity }}kcal</p>
              </div>
              <div class="text-right">
                <p class="text-sm text-gray-500">수량 {{ item.quantity }}개</p>
                <p class="text-lg font-bold">{{ (item.price * item.quantity).toLocaleString() }}원</p>
              </div>
            </div>
          </div>

          <div class="border-t border-green-100 mt-6 pt-6 space-y-2 text-sm">
            <div class="flex justify-between text-gray-600">
              <span>상품 금액</span>
              <span>{{ totalPrice.toLocaleString() }}원</span>
            </div>
            <div class="flex justify-between text-gray-600">
              <span>배송비</span>
              <span>{{ shippingFee.toLocaleString() }}원</span>
            </div>
            <div class="flex justify-between text-gray-900 font-semibold text-lg">
              <span>{{ isCancelled ? '환불 예정 금액' : '총 결제 금액' }}</span>
              <span>{{ totalPayment.toLocaleString() }}원</span>
            </div>
          </div>
        </section>
      </div>
    </div>

    <AppFooter />
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { CheckCircle, MapPin, ShieldCheck, BadgeCheck, PackageCheck, AlertTriangle, Edit3 } from 'lucide-vue-next'
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
import { useCart } from '@/composables/useCart'
import { useAddresses } from '@/composables/useAddresses'
import { useOrderStatus } from '@/composables/useOrderStatus'
import { formatPhoneNumber } from '@/utils/phone'

const router = useRouter()
const { cartItems, totalPrice } = useCart()
const { selectedAddress, loadAddresses, isLoading: isAddressLoading, errorMessage: addressErrorMessage } = useAddresses()
const { orderNumber, shippingFee, isCancelled, cancellationFee, canFreeCancel, cancelOrder } = useOrderStatus()

const totalPayment = computed(() => totalPrice.value + shippingFee)

const canEditAddress = computed(() => canFreeCancel.value && !isCancelled.value)

const formattedSelectedAddress = computed(() => {
  if (!selectedAddress.value) return null;
  return {
    ...selectedAddress.value,
    name: selectedAddress.value.name ?? selectedAddress.value.label ?? '',
    addressLineDetail:
      selectedAddress.value.addressLineDetail ?? selectedAddress.value.detailAddress ?? '',
    memo: selectedAddress.value.memo ?? selectedAddress.value.instructions ?? '',
    postalCode: selectedAddress.value.postalCode ?? '',
    phone: formatPhoneNumber(selectedAddress.value.phone),
  };
});

const handleEditAddress = () => {
  if (!canEditAddress.value) return
  window.alert('배송지 변경 기능을 곧 연결할게요. 지금은 고객센터로 요청해 주세요.')
}

const handleCancelRequest = () => {
  if (isCancelled.value) return
  const confirmed = window.confirm('결제를 취소할까요? 취소하면 배송도 함께 중단돼요.')
  if (!confirmed) return
  cancelOrder()
  window.alert('주문과 결제가 모두 취소되었어요.')
}

const navigateToProduct = (productId) => {
  router.push({ name: 'product-detail', params: { id: productId } })
}

onMounted(() => {
  loadAddresses().catch((error) => {
    console.error(error)
  })
})
</script>
