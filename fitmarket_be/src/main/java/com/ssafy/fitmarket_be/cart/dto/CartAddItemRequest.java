package com.ssafy.fitmarket_be.cart.dto;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;

/**
 * 장바구니 상품 추가 요청 DTO.
 */
public record CartAddItemRequest(
    @NotNull(message = "수량을 입력해 주세요.")
    @Range(min = 1, max = 100, message = "수량은 1개 이상 100개 이하로 담을 수 있어요.")
    Integer quantity
) {

}
