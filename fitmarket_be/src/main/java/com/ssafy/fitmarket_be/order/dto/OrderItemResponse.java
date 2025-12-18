package com.ssafy.fitmarket_be.order.dto;

/**
 * 주문 상세에서 노출할 상품 스냅샷 응답.
 *
 * @param productId   상품 식별자
 * @param productName 상품 이름
 * @param quantity    주문 수량
 * @param unitPrice   단가
 * @param totalPrice  수량 합계 금액
 */
public record OrderItemResponse(
    Long productId,
    String productName,
    int quantity,
    Long unitPrice,
    Long totalPrice
) {
}
