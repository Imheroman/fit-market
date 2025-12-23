package com.ssafy.fitmarket_be.seller.domain;

import java.util.Arrays;

public enum BusinessType {
  INDIVIDUAL("individual"),
  CORPORATION("corporation");

  private final String code;

  BusinessType(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }

  public static BusinessType from(String code) {
    return Arrays.stream(values())
        .filter(type -> type.code.equalsIgnoreCase(code))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("허용되지 않은 사업자 유형입니다: " + code));
  }
}
