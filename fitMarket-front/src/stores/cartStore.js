import {defineStore} from 'pinia';
import {addCartItem, deleteCartItem, fetchCartItems, updateCartItemQuantity} from '@/api/cartApi';

const MIN_QUANTITY = 1;
const MAX_QUANTITY = 100;

const toNumber = (value) => {
  const parsed = Number(value);
  if (!Number.isFinite(parsed)) return 0;
  return parsed;
};

const clampQuantity = (value) => {
  const parsed = Math.floor(toNumber(value) || 0);
  if (Number.isNaN(parsed)) return MIN_QUANTITY;
  if (parsed < MIN_QUANTITY) return MIN_QUANTITY;
  if (parsed > MAX_QUANTITY) return MAX_QUANTITY;
  return parsed;
};

const isQuantityInRange = (value) => {
  const parsed = Math.floor(toNumber(value) || 0);
  return parsed >= MIN_QUANTITY && parsed <= MAX_QUANTITY;
};

const mapCartItem = (item) => ({
  cartItemId: item?.cartItemId,
  productId: item?.productId ?? item?.id,
  name: item?.productName ?? item?.name ?? '',
  categoryId: item?.categoryId,
  category: item?.categoryName ?? item?.category ?? '',
  price: toNumber(item?.price),
  quantity: clampQuantity(item?.quantity ?? MIN_QUANTITY),
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
    cartCount: (state) => state.cartItems.length,
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
      if (!isQuantityInRange(quantity)) {
        this.errorMessage = '수량은 1~100개까지만 담을 수 있어요.';
        return;
      }

      this.errorMessage = '';

      try {
        const normalizedQuantity = clampQuantity(quantity);
        await addCartItem(product.productId ?? product.id, normalizedQuantity);
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
      if (!isQuantityInRange(newQuantity)) {
        this.errorMessage = '수량은 1~100개까지만 바꿀 수 있어요.';
        return;
      }

      this.errorMessage = '';

      try {
        const normalizedQuantity = clampQuantity(newQuantity);
        await updateCartItemQuantity(cartItemId, normalizedQuantity);
        const item = this.cartItems.find((cartItem) => cartItem.cartItemId === cartItemId);
        if (item) {
          item.quantity = normalizedQuantity;
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
