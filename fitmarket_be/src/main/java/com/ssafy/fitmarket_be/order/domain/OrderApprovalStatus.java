package com.ssafy.fitmarket_be.order.domain;

import java.util.Arrays;

/**
 * 주문 승인 상태를 정의한다.
 */
public enum OrderApprovalStatus {
  PENDING_APPROVAL("pending_approval"),
  APPROVED("approved"),
  REJECTED("rejected"),
  CANCELLED("cancelled"),
  SHIPPING("shipping"),
  DELIVERED("delivered");

  private final String dbValue;

  OrderApprovalStatus(String dbValue) {
    this.dbValue = dbValue;
  }

  /**
   * 데이터베이스에 저장되는 문자열 값을 반환한다.
   *
   * @return DB 저장 문자열
   */
  public String dbValue() {
    return dbValue;
  }

  /**
   * 문자열 값을 Enum으로 변환한다.
   *
   * @param value 상태 문자열
   * @return 매칭되는 상태
   * @throws IllegalArgumentException 지원하지 않는 값일 때
   */
  public static OrderApprovalStatus from(String value) {
    return Arrays.stream(values())
        .filter(status -> status.dbValue.equalsIgnoreCase(value))
        .findFirst()
        .orElseThrow(() ->
            new IllegalArgumentException("지원하지 않는 주문 상태예요. 다시 선택해 주세요."));
  }

  /**
   * 배송이 시작되어 더 이상 변경/환불이 어려운 상태인지 확인한다.
   *
   * @return 배송 진행 혹은 완료 상태 여부
   */
  public boolean isShippingOrLater() {
    return this == SHIPPING || this == DELIVERED;
  }

  /**
   * 주문이 종료 상태인지 확인한다.
   *
   * @return 취소/거절/배송완료 여부
   */
  public boolean isTerminal() {
    return this == CANCELLED || this == REJECTED || this == DELIVERED;
  }
}
