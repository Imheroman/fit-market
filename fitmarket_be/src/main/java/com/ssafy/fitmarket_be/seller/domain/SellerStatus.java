package com.ssafy.fitmarket_be.seller.domain;

import java.util.Arrays;

public enum SellerStatus {
  PENDING("pending"),
  APPROVED("approved"),
  REJECTED("rejected");

  private final String code;

  SellerStatus(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }

  public static SellerStatus from(String code) {
    return Arrays.stream(values())
        .filter(status -> status.code.equalsIgnoreCase(code))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("허용되지 않은 판매자 상태 값입니다: " + code));
  }
}
