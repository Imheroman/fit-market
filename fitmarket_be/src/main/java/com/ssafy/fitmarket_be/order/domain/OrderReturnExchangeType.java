package com.ssafy.fitmarket_be.order.domain;

/**
 * 주문 반품/교환 유형.
 */
public enum OrderReturnExchangeType {
  /**
   * 환불 요청 유형.
   */
  REFUND,
  /**
   * 반품 요청 유형.
   */
  RETURN,
  /**
   * 교환 요청 유형.
   */
  EXCHANGE
}
