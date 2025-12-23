package com.ssafy.fitmarket_be.order.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 주문 배송지 수정 요청 DTO.
 *
 * @param addressId 새 배송지 식별자
 */
public record OrderAddressUpdateRequest(
    @NotNull(message = "변경할 배송지를 선택해 주세요.") Long addressId
) {
}
