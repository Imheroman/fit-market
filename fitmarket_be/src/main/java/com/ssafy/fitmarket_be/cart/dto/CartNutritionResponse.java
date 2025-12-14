package com.ssafy.fitmarket_be.cart.dto;

/**
 * 장바구니 상품에 노출할 영양 정보 응답 DTO.
 */
public record CartNutritionResponse(
    int calories,
    int protein,
    int carbs,
    int fat
) {

}
