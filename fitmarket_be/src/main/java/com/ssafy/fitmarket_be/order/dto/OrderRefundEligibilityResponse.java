package com.ssafy.fitmarket_be.order.dto;

/**
 * 환불 가능 여부 응답 DTO.
 *
 * @param eligible 환불 가능 여부
 * @param message  안내 메시지
 */
public record OrderRefundEligibilityResponse(
    boolean eligible,
    String message
) {
}
