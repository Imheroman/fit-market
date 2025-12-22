package com.ssafy.fitmarket_be.order.dto;

import com.ssafy.fitmarket_be.order.domain.OrderReturnExchangeReason;

/**
 * 주문 환불 요청 DTO.
 *
 * @param reason 환불 사유 코드
 * @param detail 환불 사유 상세
 */
public record OrderRefundRequest(
    OrderReturnExchangeReason reason,
    String detail
) {
}
