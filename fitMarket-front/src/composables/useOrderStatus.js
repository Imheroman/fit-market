import { ref, computed } from 'vue'

const SHIPPING_FEE = 3000
const CANCELLATION_FEE = 3000

const generateOrderNumber = () => {
  const date = new Date()
  const yyyy = date.getFullYear()
  const mm = String(date.getMonth() + 1).padStart(2, '0')
  const dd = String(date.getDate()).padStart(2, '0')
  const suffix = date.getTime().toString().slice(-4)
  return `FM-${yyyy}-${mm}-${dd}${suffix}`
}

const orderNumber = ref(generateOrderNumber())
const paymentStatus = ref('paid') // paid | refunded | pending
const orderStatus = ref('processing') // processing | preparing | cancelled

const isCancelled = computed(() => orderStatus.value === 'cancelled')
const canFreeCancel = computed(() => paymentStatus.value === 'paid' && !isCancelled.value)

const completePayment = (nextOrderNumber) => {
  paymentStatus.value = 'paid'
  orderStatus.value = 'processing'
  orderNumber.value = nextOrderNumber || orderNumber.value || generateOrderNumber()
}

const cancelOrder = () => {
  paymentStatus.value = 'refunded'
  orderStatus.value = 'cancelled'
}

const setOrderNumber = (value) => {
  orderNumber.value = value || generateOrderNumber()
}

const resetOrderStatus = () => {
  paymentStatus.value = 'pending'
  orderStatus.value = 'processing'
  setOrderNumber()
}

export function useOrderStatus() {
  return {
    orderNumber,
    paymentStatus,
    orderStatus,
    isCancelled,
    canFreeCancel,
    shippingFee: SHIPPING_FEE,
    cancellationFee: CANCELLATION_FEE,
    completePayment,
    cancelOrder,
    resetOrderStatus,
    setOrderNumber,
  }
}
