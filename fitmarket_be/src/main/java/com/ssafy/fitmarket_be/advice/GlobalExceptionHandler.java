package com.ssafy.fitmarket_be.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * 에러를 전역적으로 처리하기 위한 advice
 *
 * @author kim-young-woong
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  /**
   * 일단 가장 크게만 에러를 잡음
   * @param e Throwable
   * @return
   */
  @ExceptionHandler({Throwable.class})
  public ResponseEntity<Object> handleAllException(final Throwable e) {
    log.error("전역 에러 발생: {}", e.getMessage());

    return ResponseEntity.internalServerError().body("서버에서 에러 발생");
  }
}
