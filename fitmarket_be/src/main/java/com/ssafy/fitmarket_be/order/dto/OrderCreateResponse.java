package com.ssafy.fitmarket_be.order.dto;

import com.ssafy.fitmarket_be.order.domain.OrderMode;

/**
 * 주문 생성 결과 DTO.
 *
 * @param orderNumber       생성된 주문 번호(토스 orderId로 사용)
 * @param orderName         주문 요약 이름(대표 상품명)
 * @param totalAmount       결제 총액(상품 합계 - 할인 + 배송비)
 * @param merchandiseAmount 상품 합계
 * @param shippingFee       배송비
 * @param discountAmount    할인 금액
 * @param mode              주문 모드(cart/direct)
 */
public record OrderCreateResponse(
    String orderNumber,
    String orderName,
    Long totalAmount,
    Long merchandiseAmount,
    Long shippingFee,
    Long discountAmount,
    OrderMode mode
) {
}
