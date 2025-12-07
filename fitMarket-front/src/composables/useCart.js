import { storeToRefs } from 'pinia';
import { useCartStore } from '@/stores/cartStore';

export function useCart() {
  const cartStore = useCartStore();
  const { cartItems, totalPrice, totalNutrition, cartCount } = storeToRefs(cartStore);

  const addToCart = (product, quantity = 1) => cartStore.addToCart(product, quantity);
  const updateQuantity = (id, newQuantity) => cartStore.updateQuantity(id, newQuantity);
  const removeItem = (id) => cartStore.removeItem(id);

  return {
    cartItems,
    totalPrice,
    totalNutrition,
    cartCount,
    addToCart,
    updateQuantity,
    removeItem,
  };
}
