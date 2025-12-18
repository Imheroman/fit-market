package com.ssafy.fitmarket_be.order.dto;

import com.ssafy.fitmarket_be.order.domain.OrderAddressSnapshot;
import com.ssafy.fitmarket_be.order.domain.OrderMode;
import com.ssafy.fitmarket_be.payment.domain.PaymentStatus;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 상세 응답 DTO.
 *
 * @param orderNumber       주문 번호
 * @param orderMode         주문 모드
 * @param approvalStatus    주문 승인 상태
 * @param paymentStatus     결제 상태
 * @param orderName         대표 상품명을 포함한 주문 이름
 * @param totalAmount       총 결제 금액
 * @param merchandiseAmount 상품 합계 금액
 * @param shippingFee       배송비
 * @param discountAmount    할인 금액
 * @param orderedAt         주문 일시
 * @param comment           주문 메모
 * @param address           배송지 스냅샷
 * @param items             주문 상품 목록
 */
public record OrderDetailResponse(
    String orderNumber,
    OrderMode orderMode,
    String approvalStatus,
    PaymentStatus paymentStatus,
    String orderName,
    Long totalAmount,
    Long merchandiseAmount,
    Long shippingFee,
    Long discountAmount,
    LocalDateTime orderedAt,
    String comment,
    OrderAddressSnapshot address,
    List<OrderItemResponse> items
) {
}
