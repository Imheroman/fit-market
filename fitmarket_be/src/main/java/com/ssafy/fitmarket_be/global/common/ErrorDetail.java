package com.ssafy.fitmarket_be.global.common;

/**
 * API 에러 상세 정보.
 *
 * @param code    에러 코드 (예: "PRODUCT_001"), 범용 에러 시 null
 * @param message 사용자 친화적 에러 메시지
 */
public record ErrorDetail(
    String code,
    String message
) {}
