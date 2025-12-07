import { defineStore } from 'pinia';

const defaultCartItems = [
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
];

export const useCartStore = defineStore('cart', {
  state: () => ({
    cartItems: [...defaultCartItems],
  }),
  getters: {
    cartCount: (state) => state.cartItems.reduce((sum, item) => sum + item.quantity, 0),
    totalPrice: (state) => state.cartItems.reduce((sum, item) => sum + item.price * item.quantity, 0),
    totalNutrition: (state) =>
      state.cartItems.reduce(
        (totals, item) => ({
          calories: totals.calories + item.calories * item.quantity,
          protein: totals.protein + item.protein * item.quantity,
          carbs: totals.carbs + item.carbs * item.quantity,
          fat: totals.fat + item.fat * item.quantity,
        }),
        { calories: 0, protein: 0, carbs: 0, fat: 0 }
      ),
  },
  actions: {
    addToCart(product, quantity = 1) {
      if (!product || quantity < 1) return;
      const existing = this.cartItems.find((item) => item.id === product.id);

      if (existing) {
        existing.quantity += quantity;
      } else {
        this.cartItems.push({ ...product, quantity });
      }
    },
    updateQuantity(id, newQuantity) {
      if (newQuantity < 1) return;
      const item = this.cartItems.find((cartItem) => cartItem.id === id);
      if (item) {
        item.quantity = newQuantity;
      }
    },
    removeItem(id) {
      this.cartItems = this.cartItems.filter((item) => item.id !== id);
    },
    resetCart(items = []) {
      this.cartItems = items.length > 0 ? [...items] : [];
    },
  },
  persist: {
    paths: ['cartItems'],
  },
});

export { defaultCartItems };
