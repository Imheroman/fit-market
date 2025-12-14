package com.ssafy.fitmarket_be.cart.controller;

import com.ssafy.fitmarket_be.cart.dto.CartItemResponse;
import com.ssafy.fitmarket_be.cart.dto.CartAddItemRequest;
import com.ssafy.fitmarket_be.cart.dto.CartUpdateQuantityRequest;
import com.ssafy.fitmarket_be.cart.service.ShoppingCartService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 장바구니 엔드포인트를 제공하는 컨트롤러.
 */
@RestController
@RequestMapping("/cart")
@Slf4j
@RequiredArgsConstructor
public class ShoppingCartController {

  private final ShoppingCartService shoppingCartService;

  /**
   * 사용자의 장바구니 전체를 조회한다.
   *
   * @param userId 인증된 사용자 식별자
   * @return 장바구니 상품 목록
   */
  @GetMapping
  public ResponseEntity<List<CartItemResponse>> findCartItems(
      @AuthenticationPrincipal(expression = "id") Long userId) {
    List<CartItemResponse> cartItems = this.shoppingCartService.getCartItems(userId);
    return ResponseEntity.status(HttpStatus.OK).body(cartItems);
  }

  /**
   * 장바구니에 상품을 추가하거나 이미 담긴 상품 수량을 증가시킨다.
   *
   * @param userId  인증된 사용자 식별자
   * @param request 장바구니 추가 요청
   * @return 빈 본문
   */
  @PostMapping("/{cartItemId}")
  public ResponseEntity<Void> addItem(
      @AuthenticationPrincipal(expression = "id") Long userId,
      @PathVariable Long cartItemId,
      @Valid @RequestBody CartAddItemRequest request) {
    boolean isCreated = this.shoppingCartService.addItem(userId, cartItemId, request.quantity());
    log.debug("user id: {} -> product id: {} _ created ? {}", userId, cartItemId, isCreated);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  /**
   * 장바구니 상품 수량을 수정한다.
   *
   * @param userId     인증된 사용자 식별자
   * @param cartItemId 장바구니 아이템 식별자
   * @param request    수량 수정 요청
   * @return 빈 본문
   */
  @PatchMapping("/{cartItemId}")
  public ResponseEntity<Void> updateQuantity(
      @AuthenticationPrincipal(expression = "id") Long userId,
      @PathVariable Long cartItemId,
      @Valid @RequestBody CartUpdateQuantityRequest request) {
    this.shoppingCartService.updateQuantity(userId, cartItemId, request.quantity());
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  /**
   * 장바구니 상품을 삭제한다.
   *
   * @param userId     인증된 사용자 식별자
   * @param cartItemId 장바구니 아이템 식별자
   * @return 빈 본문
   */
  @DeleteMapping("/{cartItemId}")
  public ResponseEntity<Void> delete(
      @AuthenticationPrincipal(expression = "id") Long userId,
      @PathVariable Long cartItemId) {
    this.shoppingCartService.delete(userId, cartItemId);
    return ResponseEntity.status(HttpStatus.OK).build();
  }
}
