<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <AppHeader />

    <main class="flex-1">
      <div class="container mx-auto px-4 py-10">
        <div class="space-y-8">
          <header class="text-center md:text-left space-y-3">
            <p class="text-sm font-semibold text-green-600">마이페이지</p>
            <h1 class="text-3xl md:text-4xl font-bold text-gray-900">나의 회원 정보</h1>
            <p class="text-gray-500">주문 기록과 배송지를 한 곳에서 관리할 수 있어요.</p>
          </header>

          <section class="bg-white shadow-lg rounded-2xl p-6 md:p-8 border border-green-100">
            <div class="flex flex-col gap-6">
              <div class="flex flex-col gap-2">
                <h2 class="text-xl font-semibold text-gray-900">요약 정보</h2>
                <p class="text-sm text-gray-500">로그인 상태와 기본 정보를 먼저 확인해요.</p>
              </div>

              <div v-if="isAuthenticated" class="grid gap-4 md:grid-cols-3">
                <div class="p-4 rounded-xl bg-green-50 border border-green-100">
                  <p class="text-sm text-gray-500">이름</p>
                  <p class="text-lg font-semibold text-gray-900">{{ user?.name }}</p>
                </div>
                <div class="p-4 rounded-xl bg-green-50 border border-green-100">
                  <p class="text-sm text-gray-500">이메일</p>
                  <p class="text-lg font-semibold text-gray-900">{{ user?.email }}</p>
                </div>
                <div class="p-4 rounded-xl bg-green-50 border border-green-100">
                  <p class="text-sm text-gray-500">가입일</p>
                  <p class="text-lg font-semibold text-gray-900">{{ formattedCreatedDate }}</p>
                </div>
              </div>

              <div v-else class="text-center text-sm text-gray-500">로그인 후 마이페이지 기능을 이용해주세요.</div>
            </div>
          </section>

          <div class="flex flex-wrap gap-3">
            <button
              v-for="tab in tabs"
              :key="tab.value"
              class="flex items-center gap-2 px-5 py-3 rounded-xl font-semibold transition-colors"
              :class="activeTab === tab.value ? 'bg-green-600 text-white shadow-lg' : 'bg-white text-gray-600 border border-gray-200 hover:border-green-200'"
              @click="setActiveTab(tab.value)"
            >
              <component :is="tab.icon" class="w-4 h-4" />
              {{ tab.label }}
            </button>
          </div>

          <section v-if="activeTab === 'profile'" class="bg-white shadow-lg rounded-2xl p-6 md:p-8 border border-gray-100">
            <div class="flex flex-col gap-6">
              <div class="space-y-1">
                <h2 class="text-2xl font-semibold text-gray-900">기본 정보</h2>
                <p class="text-sm text-gray-500">회원정보를 확인하고 수정하거나 탈퇴할 수 있어요.</p>
              </div>

              <div v-if="isAuthenticated" class="grid gap-4 md:grid-cols-2">
                <div v-for="item in profileFields" :key="item.label" class="p-4 rounded-xl bg-gray-50 border border-gray-100">
                  <p class="text-sm text-gray-500">{{ item.label }}</p>
                  <p class="text-lg font-semibold text-gray-900">{{ item.value }}</p>
                </div>
              </div>

              <div v-if="isAuthenticated" class="flex flex-col gap-3 md:flex-row">
                <button
                  type="button"
                  @click="handleEditProfile"
                  class="flex-1 bg-green-600 text-white px-6 py-3 rounded-xl font-semibold hover:bg-green-700 transition-colors"
                >
                  회원 정보 수정
                </button>
                <button
                  type="button"
                  @click="handleDeleteAccount"
                  class="flex-1 border border-red-200 text-red-600 px-6 py-3 rounded-xl font-semibold hover:bg-red-50 transition-colors"
                >
                  회원 탈퇴
                </button>
              </div>
            </div>
          </section>

          <section v-else-if="activeTab === 'orders'" class="bg-white shadow-lg rounded-2xl p-6 md:p-8 border border-gray-100 space-y-6">
            <div class="flex flex-col gap-1">
              <h2 class="text-2xl font-semibold text-gray-900">전체 주문 목록</h2>
              <p class="text-sm text-gray-500">{{ filterDescription }}</p>
            </div>

            <div class="flex flex-wrap gap-3">
              <button
                v-for="option in filterOptions"
                :key="option.value"
                class="px-4 py-2 rounded-full text-sm font-semibold border transition-colors"
                :class="selectedRange === option.value ? 'bg-green-600 border-green-600 text-white' : 'bg-white border-gray-200 text-gray-600 hover:border-green-200'"
                @click="setFilter(option.value)"
              >
                {{ option.label }}
              </button>
            </div>

            <div v-if="filteredOrders.length" class="space-y-4">
              <article
                v-for="order in filteredOrders"
                :key="order.id"
                class="border border-gray-100 rounded-2xl p-5 hover:border-green-200 transition-colors"
              >
                <div class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
                  <div>
                    <p class="text-sm text-gray-500">주문번호 {{ order.orderNumber }}</p>
                    <p class="text-xl font-semibold">{{ order.summary }}</p>
                  </div>
                  <span
                    class="inline-flex items-center gap-2 text-sm font-semibold px-3 py-1 rounded-full"
                    :class="getStatusMeta(order.status).badgeClass"
                  >
                    {{ getStatusMeta(order.status).label }}
                  </span>
                </div>

                <dl class="mt-4 grid gap-3 md:grid-cols-4 text-sm text-gray-600">
                  <div>
                    <dt class="text-gray-500">주문일</dt>
                    <dd class="font-semibold">{{ formatOrderDate(order.orderedAt) }}</dd>
                  </div>
                  <div>
                    <dt class="text-gray-500">상품 수</dt>
                    <dd class="font-semibold">{{ order.itemCount }}건</dd>
                  </div>
                  <div>
                    <dt class="text-gray-500">결제 금액</dt>
                    <dd class="font-semibold">{{ formatCurrency(order.totalAmount) }}</dd>
                  </div>
                  <div>
                    <dt class="text-gray-500">배송지</dt>
                    <dd class="font-semibold">{{ order.addressLabel }}</dd>
                  </div>
                </dl>

                <div class="mt-4 flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
                  <p class="text-xs text-gray-500">주문 같은 날 다른 상품이 있다면 묶음으로 확인할 수 있어요.</p>
                  <button
                    class="px-4 py-2 rounded-lg text-sm font-semibold border border-green-200 text-green-700 hover:bg-green-50 transition-colors"
                    @click="handleViewOrder(order.orderNumber)"
                  >
                    주문 상세 보기
                  </button>
                </div>
              </article>
            </div>
            <div v-else class="text-center text-gray-500 py-12 border border-dashed border-gray-200 rounded-2xl">
              해당 기간의 주문이 없어요. 기간을 넓혀 다시 확인해 주세요.
            </div>
          </section>

          <section v-else class="bg-white shadow-lg rounded-2xl p-6 md:p-8 border border-gray-100 space-y-6">
            <div class="flex flex-col gap-1 md:flex-row md:items-center md:justify-between">
              <div>
                <h2 class="text-2xl font-semibold text-gray-900">배송지 관리</h2>
                <p class="text-sm text-gray-500">기본 배송지와 추가 주소를 자유롭게 관리하세요.</p>
              </div>
              <button
                class="px-4 py-2 rounded-lg text-sm font-semibold bg-green-600 text-white hover:bg-green-700 transition-colors"
                @click="handleAddAddress"
              >
                새 배송지 등록
              </button>
            </div>

            <div class="space-y-4">
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
                    <p class="text-sm text-gray-600">{{ address.recipient }} · {{ address.phone }}</p>
                    <p class="text-sm text-gray-500">{{ address.addressLine }} {{ address.detailAddress }}</p>
                    <p class="text-xs text-gray-400 mt-2">{{ address.instructions }}</p>
                  </div>
                  <div class="flex flex-wrap gap-2 mt-4 md:mt-0">
                    <button
                      class="px-3 py-2 rounded-lg text-sm font-semibold border"
                      :class="address.isDefault ? 'border-gray-200 text-gray-400 cursor-not-allowed' : 'border-green-200 text-green-700 hover:bg-green-50'"
                      :disabled="address.isDefault"
                      @click="handleSetDefault(address.id)"
                    >
                      기본으로 설정
                    </button>
                    <button
                      class="px-3 py-2 rounded-lg text-sm font-semibold border border-gray-200 text-gray-600 hover:bg-gray-50"
                      @click="handleEditAddressInfo(address)"
                    >
                      수정
                    </button>
                    <button
                      class="px-3 py-2 rounded-lg text-sm font-semibold border border-red-200 text-red-600 hover:bg-red-50"
                      :disabled="addresses.length <= 1"
                      @click="handleRemoveAddress(address.id)"
                    >
                      삭제
                    </button>
                  </div>
                </div>
              </article>
            </div>

            <p class="text-xs text-gray-500">배송지는 최소 1개 이상 유지되어야 해요.</p>
          </section>
        </div>
      </div>
    </main>

    <AppFooter />
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { UserRound, Package, MapPin } from 'lucide-vue-next'
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
import { useAuth } from '@/composables/useAuth'
import { useOrderHistory } from '@/composables/useOrderHistory'
import { useAddresses } from '@/composables/useAddresses'

const router = useRouter()
const { user, isAuthenticated, deleteAccount } = useAuth()
const { filterOptions, selectedRange, filteredOrders, filterDescription, setFilter } = useOrderHistory()
const { addresses, setDefaultAddress, removeAddress } = useAddresses()

const tabs = [
  { label: '기본 정보', value: 'profile', icon: UserRound },
  { label: '주문 내역', value: 'orders', icon: Package },
  { label: '배송지 관리', value: 'addresses', icon: MapPin },
]

const activeTab = ref('profile')

const formattedCreatedDate = computed(() => {
  if (!user.value?.joinedAt) return '-'
  return new Date(user.value.joinedAt).toLocaleDateString('ko-KR')
})

const profileFields = computed(() => (
  user.value
    ? [
        { label: '이름', value: user.value.name },
        { label: '이메일', value: user.value.email },
        { label: '연락처', value: user.value.phone ?? '-' },
        { label: '주소', value: user.value.address ?? '-' },
        { label: '가입일', value: formattedCreatedDate.value },
        { label: '권한', value: user.value.roles?.join(', ') ?? '-' },
      ]
    : []
))

const orderStatusMeta = {
  delivered: { label: '배송 완료', badgeClass: 'bg-green-100 text-green-700' },
  shipping: { label: '배송 중', badgeClass: 'bg-blue-100 text-blue-700' },
  processing: { label: '상품 준비중', badgeClass: 'bg-yellow-100 text-yellow-600' },
  cancelled: { label: '주문 취소', badgeClass: 'bg-red-100 text-red-600' },
}

const getStatusMeta = (status) => orderStatusMeta[status] ?? { label: '확인 필요', badgeClass: 'bg-gray-100 text-gray-600' }

const formatOrderDate = (date) => {
  return new Date(date).toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' })
}

const formatCurrency = (value) => `${value.toLocaleString()}원`

const setActiveTab = (value) => {
  activeTab.value = value
}

const handleEditProfile = () => {
  router.push('/mypage/edit')
}

const handleDeleteAccount = async () => {
  if (!confirm('탈퇴 시 모든 정보가 삭제됩니다. 계속하시겠습니까?')) return

  try {
    await deleteAccount()
    alert('탈퇴 처리되었습니다. 다시 만나요!')
    router.push('/')
  } catch (error) {
    console.error(error)
    alert('탈퇴 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.')
  }
}

const handleViewOrder = (orderNumber) => {
  window.alert(`주문번호 ${orderNumber} 상세 페이지는 준비 중이에요.`)
}

const handleSetDefault = (addressId) => {
  setDefaultAddress(addressId)
  window.alert('기본 배송지를 업데이트했어요.')
}

const handleAddAddress = () => {
  window.alert('새 배송지 등록 기능은 곧 연결할게요. 당분간은 고객센터로 요청해 주세요.')
}

const handleEditAddressInfo = (address) => {
  window.alert(`${address.label} 배송지 수정 기능은 준비 중이에요.`)
}

const handleRemoveAddress = (addressId) => {
  if (addresses.value.length <= 1) {
    window.alert('배송지는 최소 1개 이상 필요해요.')
    return
  }

  if (!confirm('이 배송지를 삭제할까요?')) return
  removeAddress(addressId)
}
</script>
