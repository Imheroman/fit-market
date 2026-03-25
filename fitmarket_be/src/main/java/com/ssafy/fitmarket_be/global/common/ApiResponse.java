package com.ssafy.fitmarket_be.global.common;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 공통 API 응답 래퍼.
 *
 * <p>성공: {@code { "success": true, "data": {...}, "error": null }}
 * <p>실패: {@code { "success": false, "data": null, "error": { "code": "...", "message": "..." } }}
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public record ApiResponse<T>(
    boolean success,
    T data,
    ErrorDetail error
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, null, new ErrorDetail(code, message));
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, new ErrorDetail(null, message));
    }
}
