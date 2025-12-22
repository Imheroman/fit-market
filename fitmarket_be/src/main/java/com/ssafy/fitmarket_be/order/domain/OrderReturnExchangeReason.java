package com.ssafy.fitmarket_be.order.domain;

/**
 * 반품/교환/환불 사유 코드.
 */
public enum OrderReturnExchangeReason {
  /**
   * 품질 문제.
   */
  QUALITY_ISSUE,
  /**
   * 단순 변심.
   */
  CHANGE_OF_MIND,
  /**
   * 상품 파손.
   */
  DAMAGED,
  /**
   * 오배송.
   */
  WRONG_ITEM,
  /**
   * 기타 사유.
   */
  OTHER
}
