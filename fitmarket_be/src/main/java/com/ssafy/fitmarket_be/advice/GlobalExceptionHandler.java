package com.ssafy.fitmarket_be.advice;

import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
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
   * 클라이언트 에러 처리
   *
   * @param e IllegalArgumentException
   * @return
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Object> handleAllException(final IllegalArgumentException e) {
    log.error("전역 에러 발생: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
  }

  /**
   * 비즈니스 규칙 위반 처리
   *
   * @param e IllegalStateException
   * @return
   */
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<Object> handleIllegalStateException(IllegalStateException e) {
    log.warn("비즈니스 규칙 위반: {}", e.getMessage());
    return ResponseEntity.badRequest().body(e.getMessage());
  }

  /**
   * 일단 가장 크게만 에러를 잡음
   *
   * @param e Throwable
   * @return
   */
  @ExceptionHandler({Throwable.class})
  public ResponseEntity<Object> handleAllException(final Throwable e) {
    log.error("전역 에러 발생: {}", e.getMessage());

    return ResponseEntity.internalServerError().body("서버에서 에러 발생");
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    String message = ex.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .collect(Collectors.joining(", "));
    log.error("유효성 검증 실패: {}", message);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(message));
  }

  public record ErrorResponse(String message) {

  }
}
