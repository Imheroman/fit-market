package com.ssafy.fitmarket_be.order.dto;

import com.ssafy.fitmarket_be.order.domain.OrderReturnExchangeReason;
import com.ssafy.fitmarket_be.order.domain.OrderReturnExchangeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 반품/교환 요청 DTO.
 *
 * @param type   반품 또는 교환 타입
 * @param reason 반품/교환 사유
 * @param detail 추가 사유 설명
 */
public record OrderReturnExchangeRequest(
    @NotNull(message = "반품/교환 유형을 선택해 주세요.")
    OrderReturnExchangeType type,
    @NotNull(message = "반품/교환 사유를 선택해 주세요.")
    OrderReturnExchangeReason reason,
    @NotBlank(message = "반품/교환 사유를 조금 더 알려 주세요.")
    String detail
) {
}
