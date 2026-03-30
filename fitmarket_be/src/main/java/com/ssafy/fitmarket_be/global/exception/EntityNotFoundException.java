package com.ssafy.fitmarket_be.global.exception;

import org.springframework.http.HttpStatus;

/**
 * 엔티티를 찾을 수 없을 때 발생하는 예외.
 * HTTP 404 Not Found로 매핑.
 */
public class EntityNotFoundException extends BusinessException {

    public EntityNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

    public EntityNotFoundException(String entityName, Long id) {
        super(entityName + "을(를) 찾을 수 없습니다. ID: " + id, HttpStatus.NOT_FOUND);
    }
}
