package com.ssafy.fitmarket_be.payment.dto;

/**
 * 결제 실패 시 프런트로 반환되는 응답 DTO.
 *
 * @param orderId    주문 번호(존재하는 경우)
 * @param errorCode  토스페이먼츠 오류 코드
 * @param message    토스페이먼츠가 전달한 상세 메시지
 * @param guide      사용자에게 안내할 문구
 */
public record TossPaymentFailureResponse(
    String orderId,
    String errorCode,
    String message,
    String guide
) {}
