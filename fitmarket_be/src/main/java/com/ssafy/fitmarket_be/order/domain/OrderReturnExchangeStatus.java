package com.ssafy.fitmarket_be.order.domain;

/**
 * 주문 반품/교환/환불 요청 처리 상태.
 */
public enum OrderReturnExchangeStatus {
  /**
   * 요청 접수 상태.
   */
  PENDING,
  /**
   * 승인 완료 상태.
   */
  APPROVED,
  /**
   * 반려 상태.
   */
  REJECTED,
  /**
   * 처리 완료 상태.
   */
  COMPLETED
}
