package com.ssafy.fitmarket_be.order.dto;

/**
 * 주문 환불 요청 DTO.
 *
 * @param reason 환불 사유(선택)
 */
public record OrderRefundRequest(
    String reason
) {
}
