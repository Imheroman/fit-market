package com.ssafy.fitmarket_be.order.dto;

import com.ssafy.fitmarket_be.order.domain.OrderReturnExchangeType;

/**
 * 반품/교환 가능 여부 응답 DTO.
 *
 * @param eligible 요청 가능 여부
 * @param message  안내 메시지
 * @param type     요청 유형
 */
public record OrderReturnExchangeResponse(
    boolean eligible,
    String message,
    OrderReturnExchangeType type
) {
}
