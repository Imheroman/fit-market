import {storeToRefs} from 'pinia';
import {useCartStore} from '@/stores/cartStore';

export function useCart() {
  const cartStore = useCartStore();
  const {
    cartItems,
    totalPrice,
    totalNutrition,
    cartCount,
    isLoading,
    errorMessage,
    isInitialized
  } = storeToRefs(cartStore);

  const loadCart = (options) => cartStore.loadCart(options);
  const addToCart = (product, quantity = 1) => cartStore.addToCart(product, quantity);
  const updateQuantity = (cartItemId, newQuantity) => cartStore.updateQuantity(cartItemId, newQuantity);
  const removeItem = (cartItemId) => cartStore.removeItem(cartItemId);
  const resetCart = (items) => cartStore.resetCart(items);

  return {
    cartItems,
    totalPrice,
    totalNutrition,
    cartCount,
    isLoading,
    errorMessage,
    isInitialized,
    loadCart,
    addToCart,
    updateQuantity,
    removeItem,
    resetCart,
  };
}
