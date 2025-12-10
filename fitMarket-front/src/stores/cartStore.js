import {defineStore} from 'pinia';
import {addCartItem, deleteCartItem, fetchCartItems, updateCartItemQuantity} from '@/api/cartApi';

const toNumber = (value) => {
  const parsed = Number(value);
  if (!Number.isFinite(parsed)) return 0;
  return parsed;
};

const mapCartItem = (item) => ({
  cartItemId: item?.cartItemId,
  productId: item?.productId ?? item?.id,
  name: item?.productName ?? item?.name ?? '',
  categoryId: item?.categoryId,
  category: item?.categoryName ?? item?.category ?? '',
  price: toNumber(item?.price),
  quantity: toNumber(item?.quantity ?? 1),
  image: item?.imageUrl ?? item?.image ?? '',
  calories: toNumber(item?.nutrition?.calories ?? item?.calories),
  protein: toNumber(item?.nutrition?.protein ?? item?.protein),
  carbs: toNumber(item?.nutrition?.carbs ?? item?.carbs),
  fat: toNumber(item?.nutrition?.fat ?? item?.fat),
});

export const useCartStore = defineStore('cart', {
  state: () => ({
    cartItems: [],
    isLoading: false,
    isInitialized: false,
    errorMessage: '',
  }),
  getters: {
    cartCount: (state) => state.cartItems.reduce((sum, item) => sum + (item.quantity ?? 0), 0),
    totalPrice: (state) => state.cartItems.reduce((sum, item) => sum + (item.price ?? 0) * (item.quantity ?? 0), 0),
    totalNutrition: (state) =>
        state.cartItems.reduce(
            (totals, item) => ({
              calories: totals.calories + (item.calories ?? 0) * (item.quantity ?? 0),
              protein: totals.protein + (item.protein ?? 0) * (item.quantity ?? 0),
              carbs: totals.carbs + (item.carbs ?? 0) * (item.quantity ?? 0),
              fat: totals.fat + (item.fat ?? 0) * (item.quantity ?? 0),
            }),
            {calories: 0, protein: 0, carbs: 0, fat: 0}
        ),
  },
  actions: {
    setCartItems(items = []) {
      this.cartItems = Array.isArray(items) ? items.map(mapCartItem) : [];
    },
    async loadCart({force = false} = {}) {
      if (this.isLoading) return;
      if (this.isInitialized && !force) return;

      this.isLoading = true;
      this.errorMessage = '';

      try {
        const items = await fetchCartItems();
        this.setCartItems(items);
        this.isInitialized = true;
      } catch (error) {
        this.errorMessage = error?.message ?? '장바구니를 불러오지 못했어요. 다시 시도해 주세요.';
        throw error;
      } finally {
        this.isLoading = false;
      }
    },
    async addToCart(product, quantity = 1) {
      if (!product?.id && !product?.productId) return;
      if (quantity < 1) {
        this.errorMessage = '수량은 최소 1개 이상이에요.';
        return;
      }

      this.errorMessage = '';

      try {
        await addCartItem(product.productId ?? product.id, quantity);
        await this.loadCart({force: true});
      } catch (error) {
        this.errorMessage = error?.message ?? '장바구니에 담지 못했어요. 다시 시도해 주세요.';
        throw error;
      }
    },
    async updateQuantity(cartItemId, newQuantity) {
      if (!cartItemId) {
        this.errorMessage = '상품 정보를 찾지 못했어요. 새로고침 후 다시 시도해 주세요.';
        return;
      }
      if (newQuantity < 1) {
        this.errorMessage = '수량은 최소 1개 이상이에요.';
        return;
      }

      this.errorMessage = '';

      try {
        await updateCartItemQuantity(cartItemId, newQuantity);
        const item = this.cartItems.find((cartItem) => cartItem.cartItemId === cartItemId);
        if (item) {
          item.quantity = newQuantity;
        } else {
          await this.loadCart({force: true});
        }
      } catch (error) {
        this.errorMessage = error?.message ?? '수량을 바꾸지 못했어요. 다시 시도해 주세요.';
        throw error;
      }
    },
    async removeItem(cartItemId) {
      if (!cartItemId) {
        this.errorMessage = '상품 정보를 찾지 못했어요. 새로고침 후 다시 시도해 주세요.';
        return;
      }
      this.errorMessage = '';
      try {
        await deleteCartItem(cartItemId);
        this.cartItems = this.cartItems.filter((item) => item.cartItemId !== cartItemId);
      } catch (error) {
        this.errorMessage = error?.message ?? '상품을 삭제하지 못했어요. 다시 시도해 주세요.';
        throw error;
      }
    },
    resetCart(items = []) {
      this.setCartItems(items);
      this.isInitialized = Boolean(items?.length);
      this.errorMessage = '';
    },
  },
});
