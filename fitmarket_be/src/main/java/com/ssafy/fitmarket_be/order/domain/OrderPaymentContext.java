package com.ssafy.fitmarket_be.order.domain;

import com.ssafy.fitmarket_be.payment.domain.PaymentStatus;

/**
 * 결제 처리에 필요한 최소 주문 정보.
 *
 * @param orderId       주문 식별자
 * @param userId        주문자 식별자
 * @param orderNumber   주문 번호(토스 orderId와 동일)
 * @param totalAmount   결제 금액
 * @param paymentStatus 결제 상태
 * @param orderMode     주문 생성 모드
 * @param itemsSnapshot 주문 상품 스냅샷 JSON
 */
public record OrderPaymentContext(
    Long orderId,
    Long userId,
    String orderNumber,
    Long totalAmount,
    PaymentStatus paymentStatus,
    OrderMode orderMode,
    String itemsSnapshot
) {
}
