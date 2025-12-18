package com.ssafy.fitmarket_be.order.domain;

/**
 * 주문 생성 모드(장바구니/바로구매)를 표현한다.
 */
public enum OrderMode {
  CART,
  DIRECT;

  /**
   * 문자열 값을 받아 enum으로 변환한다.
   *
   * @param value 입력 문자열
   * @return 매칭되는 주문 모드
   * @throws IllegalArgumentException 매칭되는 모드가 없을 때
   */
  public static OrderMode from(String value) {
    if (value == null) {
      throw new IllegalArgumentException("주문 모드가 비어 있어요. 장바구니 또는 바로구매를 선택해 주세요.");
    }
    for (OrderMode mode : values()) {
      if (mode.name().equalsIgnoreCase(value)) {
        return mode;
      }
    }
    throw new IllegalArgumentException("지원하지 않는 주문 모드예요. 장바구니 또는 바로구매로 요청해 주세요.");
  }

  /**
   * 장바구니 모드인지 여부를 반환한다.
   *
   * @return 장바구니 주문 여부
   */
  public boolean isCart() {
    return this == CART;
  }
}
