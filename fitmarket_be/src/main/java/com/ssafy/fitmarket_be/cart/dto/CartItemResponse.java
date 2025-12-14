package com.ssafy.fitmarket_be.cart.dto;

import com.ssafy.fitmarket_be.entity.ShoppingCartProduct;

/**
 * 장바구니 상품 조회 응답 DTO.
 */
public record CartItemResponse(
    Long cartItemId,
    Long productId,
    String productName,
    Long categoryId,
    String categoryName,
    Long price,
    int quantity,
    String imageUrl,
    CartNutritionResponse nutrition
) {

  /**
   * 도메인 모델을 API 응답 DTO로 변환한다.
   *
   * @param shoppingCartProduct 장바구니 도메인 객체
   * @return 장바구니 응답 DTO
   */
  public static CartItemResponse from(ShoppingCartProduct shoppingCartProduct) {
    CartNutritionResponse nutritionResponse = new CartNutritionResponse(
        shoppingCartProduct.getCalories(),
        shoppingCartProduct.getProtein(),
        shoppingCartProduct.getCarbs(),
        shoppingCartProduct.getFat()
    );

    return new CartItemResponse(
        shoppingCartProduct.getId(),
        shoppingCartProduct.getProductId(),
        shoppingCartProduct.getProductName(),
        shoppingCartProduct.getCategoryId(),
        shoppingCartProduct.getCategoryName(),
        shoppingCartProduct.getPrice(),
        shoppingCartProduct.getQuantity(),
        shoppingCartProduct.getImageUrl(),
        nutritionResponse
    );
  }
}
