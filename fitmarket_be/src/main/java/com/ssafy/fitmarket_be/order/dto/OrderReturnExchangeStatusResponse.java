package com.ssafy.fitmarket_be.order.dto;

import com.ssafy.fitmarket_be.order.domain.OrderReturnExchangeStatus;
import com.ssafy.fitmarket_be.order.domain.OrderReturnExchangeType;
import java.time.LocalDateTime;

/**
 * 주문 반품/교환/환불 진행 상태 응답 DTO.
 *
 * @param type        요청 유형
 * @param status      처리 상태
 * @param requestedAt 요청 접수 시각
 * @param processedAt 처리 완료 시각
 */
public record OrderReturnExchangeStatusResponse(
    OrderReturnExchangeType type,
    OrderReturnExchangeStatus status,
    LocalDateTime requestedAt,
    LocalDateTime processedAt
) {
}
