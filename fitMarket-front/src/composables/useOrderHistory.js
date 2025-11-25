import { computed, ref } from 'vue'

const filterOptions = [
  { label: '전체', value: 'all', months: null },
  { label: '1개월', value: '1m', months: 1 },
  { label: '3개월', value: '3m', months: 3 },
  { label: '6개월', value: '6m', months: 6 },
  { label: '1년', value: '1y', months: 12 },
]

const orderHistory = ref([
  {
    id: 'order-20250115',
    orderNumber: 'FM-2025-01-150321',
    orderedAt: '2025-01-15T09:25:00+09:00',
    totalAmount: 25800,
    itemCount: 3,
    addressLabel: '집',
    status: 'delivered',
    summary: '그린 샐러드 도시락 외 2건',
  },
  {
    id: 'order-20241203',
    orderNumber: 'FM-2024-12-031142',
    orderedAt: '2024-12-03T18:40:00+09:00',
    totalAmount: 18400,
    itemCount: 2,
    addressLabel: '회사',
    status: 'shipping',
    summary: '단백 퀴노아 샐러드 외 1건',
  },
  {
    id: 'order-20241022',
    orderNumber: 'FM-2024-10-220914',
    orderedAt: '2024-10-22T11:10:00+09:00',
    totalAmount: 32600,
    itemCount: 4,
    addressLabel: '부모님 댁',
    status: 'processing',
    summary: '키토 밀프렙 세트 외 3건',
  },
  {
    id: 'order-20240508',
    orderNumber: 'FM-2024-05-081755',
    orderedAt: '2024-05-08T14:05:00+09:00',
    totalAmount: 12900,
    itemCount: 1,
    addressLabel: '집',
    status: 'cancelled',
    summary: '그릭요거트 베리볼',
  },
])

const selectedRange = ref(filterOptions[2].value) // default 3개월

const filteredOrders = computed(() => {
  if (selectedRange.value === 'all') return orderHistory.value

  const option = filterOptions.find((opt) => opt.value === selectedRange.value)
  if (!option?.months) return orderHistory.value

  const now = new Date()
  const threshold = new Date(now)
  threshold.setMonth(threshold.getMonth() - option.months)

  return orderHistory.value.filter((order) => new Date(order.orderedAt) >= threshold)
})

const filterDescription = computed(() => {
  const option = filterOptions.find((opt) => opt.value === selectedRange.value)
  if (!option || option.value === 'all') return '전체 주문 내역을 보여드려요.'
  return `${option.label} 동안의 주문을 모아봤어요.`
})

const setFilter = (value) => {
  const exists = filterOptions.some((opt) => opt.value === value)
  if (exists) {
    selectedRange.value = value
  }
}

export function useOrderHistory() {
  return {
    orders: orderHistory,
    filterOptions,
    selectedRange,
    filteredOrders,
    filterDescription,
    setFilter,
  }
}
