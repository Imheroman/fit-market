package com.ssafy.fitmarket_be.order.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 주문 상태 변경 요청 DTO.
 *
 * @param approvalStatus 주문 승인 상태 문자열
 */
public record OrderStatusUpdateRequest(
    @NotBlank(message = "변경할 주문 상태를 입력해 주세요.") String approvalStatus
) {
}
