package com.ssafy.fitmarket_be.global.exception;

import org.springframework.http.HttpStatus;

/**
 * 엔티티가 이미 존재할 때 발생하는 예외.
 * HTTP 409 Conflict로 매핑.
 */
public class DuplicateEntityException extends BusinessException {

    public DuplicateEntityException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
