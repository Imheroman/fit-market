package com.ssafy.fitmarket_be.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 토스페이먼츠 결제 승인 요청 DTO.
 *
 * @param paymentKey 결제 건을 식별하는 토스 결제 키
 * @param orderId    상점에서 관리하는 주문 번호
 * @param amount     결제 금액(원)
 */
public record TossPaymentRequest(
    @NotBlank String paymentKey,
    @NotBlank String orderId,
    @NotNull Long amount
) {}
