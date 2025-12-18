package com.ssafy.fitmarket_be.payment.domain;

/**
 * 결제 상태를 표현한다.
 */
public enum PaymentStatus {
  PENDING,
  PAID,
  REFUNDED,
  FAILED;

  /**
   * 결제가 완료 상태인지 여부를 반환한다.
   *
   * @return 결제 완료 여부
   */
  public boolean isPaid() {
    return this == PAID;
  }
}
