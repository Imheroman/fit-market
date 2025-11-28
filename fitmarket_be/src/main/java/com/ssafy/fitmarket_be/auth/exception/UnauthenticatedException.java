package com.ssafy.fitmarket_be.auth.exception;

public class UnauthenticatedException extends RuntimeException {
  private static final String ERROR_MESSAGE = "인증되지 않은 사용자입니다.";

  public UnauthenticatedException() {
    super(ERROR_MESSAGE);
  }

  public String getErrorMessage() {
    return ERROR_MESSAGE;
  }
}
