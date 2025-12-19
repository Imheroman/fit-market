package com.ssafy.fitmarket_be.order.domain;

import java.time.LocalDateTime;
import java.time.Period;

/**
 * 주문 목록 조회 시 사용할 기간 조건을 정의한다.
 */
public enum OrderSearchPeriod {
  /** 전체 기간. */
  ALL(null),
  /** 최근 1개월. */
  MONTH_1(Period.ofMonths(1)),
  /** 최근 3개월. */
  MONTH_3(Period.ofMonths(3)),
  /** 최근 6개월. */
  MONTH_6(Period.ofMonths(6)),
  /** 최근 1년. */
  YEAR_1(Period.ofYears(1));

  private final Period period;

  OrderSearchPeriod(Period period) {
    this.period = period;
  }

  /**
   * 요청 파라미터 문자열을 조회 기간으로 변환한다.
   *
   * @param value 요청 파라미터 값
   * @return 조회 기간
   * @throws IllegalArgumentException 허용되지 않은 기간 값이 전달된 경우
   */
  public static OrderSearchPeriod from(String value) {
    if (value == null || value.isBlank()) {
      return ALL;
    }

    String normalized = value.trim().toUpperCase();
    return switch (normalized) {
      case "ALL" -> ALL;
      case "1M", "MONTH_1", "ONE_MONTH" -> MONTH_1;
      case "3M", "MONTH_3", "THREE_MONTHS" -> MONTH_3;
      case "6M", "MONTH_6", "SIX_MONTHS" -> MONTH_6;
      case "1Y", "YEAR_1", "ONE_YEAR" -> YEAR_1;
      default -> throw new IllegalArgumentException("조회 기간을 다시 선택해 주세요.");
    };
  }

  /**
   * 기준 시점에서 조회 시작 시점을 계산한다.
   *
   * @param now 기준 시점
   * @return 조회 시작 시점, 전체 기간이라면 {@code null}
   */
  public LocalDateTime resolveStartDate(LocalDateTime now) {
    if (this.period == null) {
      return null;
    }
    return now.minus(this.period);
  }
}
