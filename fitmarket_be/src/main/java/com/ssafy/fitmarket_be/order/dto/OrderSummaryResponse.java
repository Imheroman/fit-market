package com.ssafy.fitmarket_be.order.dto;

import com.ssafy.fitmarket_be.order.domain.OrderMode;
import com.ssafy.fitmarket_be.payment.domain.PaymentStatus;
import java.time.LocalDateTime;

/**
 * 주문 목록 조회 응답 DTO.
 *
 * @param orderNumber       주문 번호
 * @param orderName         대표 상품명을 포함한 주문 이름
 * @param orderMode         주문 모드
 * @param approvalStatus    주문 승인 상태
 * @param paymentStatus     결제 상태
 * @param totalAmount       총 결제 금액
 * @param merchandiseAmount 상품 합계 금액
 * @param shippingFee       배송비
 * @param discountAmount    할인 금액
 * @param itemCount         주문 상품 개수
 * @param orderedAt         주문 일시
 */
public record OrderSummaryResponse(
    String orderNumber,
    String orderName,
    OrderMode orderMode,
    String approvalStatus,
    PaymentStatus paymentStatus,
    Long totalAmount,
    Long merchandiseAmount,
    Long shippingFee,
    Long discountAmount,
    int itemCount,
    LocalDateTime orderedAt
) {
}
