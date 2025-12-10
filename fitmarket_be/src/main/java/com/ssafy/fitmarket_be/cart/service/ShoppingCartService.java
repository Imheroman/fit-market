package com.ssafy.fitmarket_be.cart.service;

import com.ssafy.fitmarket_be.cart.mapper.ShoppingCartMapper;
import com.ssafy.fitmarket_be.entity.ShoppingCartProduct;
import com.ssafy.fitmarket_be.cart.dto.CartItemResponse;
import com.ssafy.fitmarket_be.cart.repository.ShoppingCartRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 장바구니 유스케이스를 담당하는 서비스.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShoppingCartService {

  private final ShoppingCartRepository shoppingCartRepository;
  private final ShoppingCartMapper shoppingCartMapper;

  /**
   * 사용자 장바구니 수를 조회한다.
   *
   * @param userId 사용자 식별자
   * @return 장바구니 응답 목록
   */
  @Transactional(readOnly = true)
  public List<CartItemResponse> getCartItems(Long userId) {
    List<ShoppingCartProduct> shoppingCartProducts = this.shoppingCartRepository.findActiveByUserId(userId);
    return this.shoppingCartMapper.toResponseList(shoppingCartProducts);
  }

  /**
   * 사용자 장바구니 전체를 조회한다.
   *
   * @param userId 사용자 식별자
   * @return 장바구니 수량 조회
   */
  @Transactional(readOnly = true)
  public int countCartItems(Long userId) {
    return this.shoppingCartRepository.countCartItems(userId);
  }

  /**
   * 장바구니에 상품을 담거나 이미 담겨 있다면 수량을 추가한다.
   *
   * @param userId    사용자 식별자
   * @param productId 상품 식별자
   * @param quantity  추가할 수량
   * @return 새로운 상품이 담겼다면 true, 기존 상품 수량이 늘어났다면 false
   */
  @Transactional
  public boolean addItem(Long userId, Long productId, int quantity) {
    if (quantity < 1) {
      throw new IllegalArgumentException("수량은 1개 이상부터 담을 수 있어요.");
    }

    int updated = this.shoppingCartRepository.incrementQuantity(userId, productId, quantity);
    if (updated > 0) {
      log.debug(
          "update cart item for product {} increased by {} for user {}",
          productId,
          quantity,
          userId
      );
      return false;
    }

    int inserted = this.shoppingCartRepository.insert(userId, productId, quantity);
    if (inserted <= 0) {
      throw new IllegalArgumentException("장바구니에 상품을 담지 못했어요. 잠시 후 다시 시도해 주세요.");
    }

    log.debug("insert product {} added to cart for user {} with quantity {}", productId, userId,
        quantity);
    return true;
  }

  /**
   * 장바구니 상품 수량을 수정한다.
   *
   * @param userId     사용자 식별자
   * @param cartItemId 장바구니 아이템 식별자
   * @param quantity   수정할 수량
   */
  @Transactional
  public void updateQuantity(Long userId, Long cartItemId, int quantity) {
    if (quantity < 1) {
      throw new IllegalArgumentException("수량은 1개 이상부터 담을 수 있어요.");
    }

    int updated = this.shoppingCartRepository.updateQuantity(cartItemId, userId, quantity);
    if (updated <= 0) {
      throw new IllegalArgumentException("수정할 장바구니 상품을 찾을 수 없어요. 다시 시도해 주세요.");
    }

    log.debug("cart item {} quantity updated to {} for user {}", cartItemId, quantity, userId);
  }

  /**
   * 장바구니 상품을 소프트 삭제한다.
   *
   * @param userId     사용자 식별자
   * @param cartItemId 장바구니 아이템 식별자
   */
  @Transactional
  public void delete(Long userId, Long cartItemId) {
    int deleted = this.shoppingCartRepository.softDelete(cartItemId, userId);
    if (deleted <= 0) {
      throw new IllegalArgumentException("삭제할 장바구니 상품을 찾을 수 없어요. 이미 삭제되었는지 확인해 주세요.");
    }

    log.debug("cart item {} soft deleted for user {}", cartItemId, userId);
  }
}
