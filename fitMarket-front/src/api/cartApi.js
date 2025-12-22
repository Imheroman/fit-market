import {fitmarket} from '@/restapi';
import {isAuthError} from '@/utils/httpError';

const extractPayload = (response) => response?.data?.data ?? response?.data ?? [];
const buildError = (error, fallback) => {
  const message = error?.response?.data?.message ?? fallback;
  const wrapped = new Error(message);
  wrapped.cause = error;
  wrapped.isAuthError = isAuthError(error);
  return wrapped;
};

export async function fetchCartItems() {
  try {
    const response = await fitmarket.get('/cart', {withCredentials: true});
    return extractPayload(response);
  } catch (error) {
    throw buildError(error, '장바구니를 불러오지 못했어요. 잠시 후 다시 시도해 주세요.');
  }
}

export async function addCartItem(cartItemId, quantity = 1) {
  try {
    await fitmarket.post(`/cart/${cartItemId}`, {quantity}, {withCredentials: true});
  } catch (error) {
    throw buildError(error, '장바구니에 담지 못했어요. 다시 시도해 주세요.');
  }
}

export async function updateCartItemQuantity(cartItemId, quantity) {
  try {
    await fitmarket.patch(`/cart/${cartItemId}`, {quantity}, {withCredentials: true});
  } catch (error) {
    throw buildError(error, '수량을 바꾸지 못했어요. 다시 시도해 주세요.');
  }
}

export async function deleteCartItem(cartItemId) {
  try {
    await fitmarket.delete(`/cart/${cartItemId}`, {withCredentials: true});
  } catch (error) {
    throw buildError(error, '상품을 삭제하지 못했어요. 이미 삭제되었는지 확인해 주세요.');
  }
}
