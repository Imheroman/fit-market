import { ref, computed } from 'vue'

const cartItems = ref([
  {
    id: 1,
    name: '그린 샐러드 도시락',
    category: '도시락',
    price: 8500,
    image: '/fresh-green-salad-bowl.png',
    quantity: 2,
    calories: 320,
    protein: 18,
    carbs: 35,
    fat: 12,
  },
  {
    id: 2,
    name: '고단백 치킨 밀키트',
    category: '밀키트',
    price: 12900,
    image: '/healthy-chicken-meal-prep.png',
    quantity: 1,
    calories: 450,
    protein: 42,
    carbs: 28,
    fat: 18,
  },
])

export function useCart() {
  const totalPrice = computed(() => {
    return cartItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
  })

  const totalNutrition = computed(() => {
    return cartItems.value.reduce(
      (totals, item) => ({
        calories: totals.calories + item.calories * item.quantity,
        protein: totals.protein + item.protein * item.quantity,
        carbs: totals.carbs + item.carbs * item.quantity,
        fat: totals.fat + item.fat * item.quantity,
      }),
      { calories: 0, protein: 0, carbs: 0, fat: 0 }
    )
  })

  const cartCount = computed(() => {
    return cartItems.value.reduce((sum, item) => sum + item.quantity, 0)
  })

  const addToCart = (product) => {
    const existing = cartItems.value.find(item => item.id === product.id)
    if (existing) {
      existing.quantity++
    } else {
      cartItems.value.push({ ...product, quantity: 1 })
    }
  }

  const updateQuantity = (id, newQuantity) => {
    if (newQuantity < 1) return
    const item = cartItems.value.find(item => item.id === id)
    if (item) {
      item.quantity = newQuantity
    }
  }

  const removeItem = (id) => {
    cartItems.value = cartItems.value.filter(item => item.id !== id)
  }

  return {
    cartItems,
    totalPrice,
    totalNutrition,
    cartCount,
    addToCart,
    updateQuantity,
    removeItem,
  }
}
