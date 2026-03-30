package com.ssafy.fitmarket_be.payment.event;

import com.ssafy.fitmarket_be.order.domain.OrderMode;

/**
 * 결제 승인이 완료되었을 때 발행되는 이벤트.
 * Order 도메인에서 수신하여 주문 상태를 갱신한다.
 */
public record PaymentCompletedEvent(
    Long orderId,
    Long userId,
    String orderNumber,
    String paymentKey,
    Long totalAmount,
    OrderMode orderMode,
    String itemsSnapshot
) {
}
