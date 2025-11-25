<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <AppHeader />

    <main class="flex-1">
      <div class="container mx-auto px-4 py-10">
        <div class="space-y-8">
          <!-- 헤더 -->
          <header class="text-center md:text-left space-y-3">
            <p class="text-sm font-semibold text-purple-600">관리자 페이지</p>
            <h1 class="text-3xl md:text-4xl font-bold text-gray-900">판매자 신청 관리</h1>
            <p class="text-gray-500">판매자 신청을 검토하고 승인 또는 거부할 수 있습니다.</p>
          </header>

          <!-- 통계 카드 -->
          <div class="grid md:grid-cols-3 gap-6">
            <div class="bg-white p-6 rounded-2xl shadow-lg border border-gray-200">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-sm text-gray-500">대기중인 신청</p>
                  <p class="text-3xl font-bold text-yellow-600 mt-2">{{ pendingApplications.length }}</p>
                </div>
                <Clock class="w-12 h-12 text-yellow-600 opacity-20" />
              </div>
            </div>
            <div class="bg-white p-6 rounded-2xl shadow-lg border border-gray-200">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-sm text-gray-500">승인됨</p>
                  <p class="text-3xl font-bold text-green-600 mt-2">{{ approvedApplications.length }}</p>
                </div>
                <CheckCircle2 class="w-12 h-12 text-green-600 opacity-20" />
              </div>
            </div>
            <div class="bg-white p-6 rounded-2xl shadow-lg border border-gray-200">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-sm text-gray-500">거부됨</p>
                  <p class="text-3xl font-bold text-red-600 mt-2">{{ rejectedApplications.length }}</p>
                </div>
                <XCircle class="w-12 h-12 text-red-600 opacity-20" />
              </div>
            </div>
          </div>

          <!-- 탭 -->
          <div class="flex gap-3 border-b border-gray-200">
            <button
              @click="activeTab = 'pending'"
              class="px-6 py-3 font-semibold transition-colors border-b-2"
              :class="
                activeTab === 'pending'
                  ? 'text-purple-600 border-purple-600'
                  : 'text-gray-500 border-transparent hover:text-gray-700'
              "
            >
              대기중 ({{ pendingApplications.length }})
            </button>
            <button
              @click="activeTab = 'approved'"
              class="px-6 py-3 font-semibold transition-colors border-b-2"
              :class="
                activeTab === 'approved'
                  ? 'text-purple-600 border-purple-600'
                  : 'text-gray-500 border-transparent hover:text-gray-700'
              "
            >
              승인됨 ({{ approvedApplications.length }})
            </button>
            <button
              @click="activeTab = 'rejected'"
              class="px-6 py-3 font-semibold transition-colors border-b-2"
              :class="
                activeTab === 'rejected'
                  ? 'text-purple-600 border-purple-600'
                  : 'text-gray-500 border-transparent hover:text-gray-700'
              "
            >
              거부됨 ({{ rejectedApplications.length }})
            </button>
          </div>

          <!-- 신청 목록 -->
          <div class="space-y-4">
            <div
              v-for="app in currentApplications"
              :key="app.id"
              class="bg-white shadow-lg rounded-2xl p-6 border border-gray-200"
            >
              <div class="flex items-start justify-between mb-4">
                <div>
                  <div class="flex items-center gap-3">
                    <h3 class="text-xl font-bold text-gray-900">{{ app.businessName }}</h3>
                    <span
                      class="px-3 py-1 rounded-full text-xs font-medium"
                      :class="{
                        'bg-yellow-100 text-yellow-700': app.status === 'pending',
                        'bg-green-100 text-green-700': app.status === 'approved',
                        'bg-red-100 text-red-700': app.status === 'rejected',
                      }"
                    >
                      {{
                        app.status === 'pending'
                          ? '대기중'
                          : app.status === 'approved'
                            ? '승인됨'
                            : '거부됨'
                      }}
                    </span>
                  </div>
                  <p class="text-sm text-gray-500 mt-1">신청자: {{ app.userName }} ({{ app.email }})</p>
                </div>
                <div class="text-right text-sm text-gray-500">
                  <p>신청일: {{ formatDate(app.appliedAt) }}</p>
                  <p v-if="app.reviewedAt">검토일: {{ formatDate(app.reviewedAt) }}</p>
                </div>
              </div>

              <div class="grid md:grid-cols-2 gap-4 mb-4">
                <div class="space-y-2">
                  <div class="flex items-center gap-2 text-sm">
                    <Building2 class="w-4 h-4 text-gray-400" />
                    <span class="text-gray-600">사업자등록번호:</span>
                    <span class="font-medium text-gray-900">{{ app.businessNumber }}</span>
                  </div>
                  <div class="flex items-center gap-2 text-sm">
                    <Tag class="w-4 h-4 text-gray-400" />
                    <span class="text-gray-600">사업자 유형:</span>
                    <span class="font-medium text-gray-900">
                      {{ app.businessType === 'individual' ? '개인사업자' : '법인사업자' }}
                    </span>
                  </div>
                  <div class="flex items-center gap-2 text-sm">
                    <Phone class="w-4 h-4 text-gray-400" />
                    <span class="text-gray-600">연락처:</span>
                    <span class="font-medium text-gray-900">{{ app.contactPhone }}</span>
                  </div>
                </div>
                <div class="space-y-2">
                  <div class="flex items-start gap-2 text-sm">
                    <MapPin class="w-4 h-4 text-gray-400 mt-0.5" />
                    <div>
                      <span class="text-gray-600">사업장 주소:</span>
                      <p class="font-medium text-gray-900">{{ app.businessAddress }}</p>
                    </div>
                  </div>
                </div>
              </div>

              <div class="mb-4">
                <p class="text-sm text-gray-600 mb-2">사업 소개:</p>
                <p class="text-sm text-gray-900 bg-gray-50 p-3 rounded-lg">{{ app.introduction }}</p>
              </div>

              <div v-if="app.reviewNote" class="mb-4 p-3 bg-blue-50 border border-blue-200 rounded-lg">
                <p class="text-sm text-blue-900">
                  <span class="font-semibold">검토 메모:</span> {{ app.reviewNote }}
                </p>
              </div>

              <!-- 대기중인 신청에만 버튼 표시 -->
              <div v-if="app.status === 'pending'" class="flex gap-3 pt-4 border-t border-gray-200">
                <button
                  @click="handleReject(app.id)"
                  class="flex-1 px-6 py-3 bg-red-50 text-red-600 rounded-lg font-medium hover:bg-red-100 transition-colors flex items-center justify-center gap-2"
                >
                  <XCircle class="w-5 h-5" />
                  거부
                </button>
                <button
                  @click="handleApprove(app.id)"
                  class="flex-1 px-6 py-3 bg-green-600 text-white rounded-lg font-medium hover:bg-green-700 transition-colors flex items-center justify-center gap-2"
                >
                  <CheckCircle2 class="w-5 h-5" />
                  승인
                </button>
              </div>
            </div>

            <!-- 목록이 비어있을 때 -->
            <div v-if="currentApplications.length === 0" class="bg-white p-12 rounded-2xl text-center text-gray-500">
              <FileText class="w-16 h-16 mx-auto mb-4 text-gray-300" />
              <p>
                {{
                  activeTab === 'pending'
                    ? '대기중인 신청이 없습니다.'
                    : activeTab === 'approved'
                      ? '승인된 신청이 없습니다.'
                      : '거부된 신청이 없습니다.'
                }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </main>

    <AppFooter />

    <!-- 승인/거부 모달 -->
    <div
      v-if="showModal"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
      @click.self="closeModal"
    >
      <div class="bg-white rounded-2xl p-6 max-w-md w-full">
        <h3 class="text-xl font-bold text-gray-900 mb-4">
          {{ modalType === 'approve' ? '판매자 승인' : '판매자 거부' }}
        </h3>
        <p class="text-gray-600 mb-4">
          {{
            modalType === 'approve'
              ? '이 신청을 승인하시겠습니까?'
              : '이 신청을 거부하시겠습니까?'
          }}
        </p>
        <div class="mb-4">
          <label for="note" class="block text-sm font-medium text-gray-700 mb-2">
            검토 메모 (선택사항)
          </label>
          <textarea
            id="note"
            v-model="reviewNote"
            rows="3"
            placeholder="검토 내용을 입력해주세요."
            class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500 resize-none"
          ></textarea>
        </div>
        <div class="flex gap-3">
          <button
            @click="closeModal"
            class="flex-1 px-6 py-3 bg-gray-100 text-gray-700 rounded-lg font-medium hover:bg-gray-200 transition-colors"
          >
            취소
          </button>
          <button
            @click="confirmAction"
            class="flex-1 px-6 py-3 rounded-lg font-medium transition-colors"
            :class="
              modalType === 'approve'
                ? 'bg-green-600 text-white hover:bg-green-700'
                : 'bg-red-600 text-white hover:bg-red-700'
            "
          >
            {{ modalType === 'approve' ? '승인' : '거부' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import {
  Clock,
  CheckCircle2,
  XCircle,
  Building2,
  Tag,
  Phone,
  MapPin,
  FileText,
} from 'lucide-vue-next'
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
import { useSellerApplicationsAdmin } from '@/composables/useSellerApplication'

const activeTab = ref('pending')
const showModal = ref(false)
const modalType = ref('approve') // 'approve' | 'reject'
const selectedApplicationId = ref(null)
const reviewNote = ref('')

const {
  applications,
  pendingApplications,
  approvedApplications,
  rejectedApplications,
  approveApplication,
  rejectApplication,
} = useSellerApplicationsAdmin()

const currentApplications = computed(() => {
  if (activeTab.value === 'pending') return pendingApplications.value
  if (activeTab.value === 'approved') return approvedApplications.value
  return rejectedApplications.value
})

const handleApprove = (applicationId) => {
  selectedApplicationId.value = applicationId
  modalType.value = 'approve'
  reviewNote.value = ''
  showModal.value = true
}

const handleReject = (applicationId) => {
  selectedApplicationId.value = applicationId
  modalType.value = 'reject'
  reviewNote.value = ''
  showModal.value = true
}

const confirmAction = async () => {
  if (modalType.value === 'approve') {
    await approveApplication(selectedApplicationId.value, reviewNote.value)
  } else {
    await rejectApplication(selectedApplicationId.value, reviewNote.value)
  }
  closeModal()
}

const closeModal = () => {
  showModal.value = false
  selectedApplicationId.value = null
  reviewNote.value = ''
}

const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}
</script>
