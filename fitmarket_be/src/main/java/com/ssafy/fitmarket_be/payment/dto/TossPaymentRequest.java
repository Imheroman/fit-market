package com.ssafy.fitmarket_be.payment.dto;

import com.ssafy.fitmarket_be.order.dto.OrderCreateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 토스페이먼츠 결제 승인 요청 DTO.
 *
 * @param paymentKey 결제 건을 식별하는 토스 결제 키
 * @param orderId    상점에서 관리하는 주문 번호
 * @param amount     결제 금액(원)
 * @param orderRequest 결제 완료 후 생성할 주문 요청 본문(선결제 플로우용)
 */
public record TossPaymentRequest(
    @NotBlank String paymentKey,
    @NotBlank String orderId,
    @NotNull Long amount,
    @Valid OrderCreateRequest orderRequest
) {}
