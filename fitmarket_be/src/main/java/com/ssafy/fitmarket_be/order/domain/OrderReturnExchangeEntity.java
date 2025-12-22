package com.ssafy.fitmarket_be.order.domain;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주문 반품/교환/환불 요청 정보를 저장하는 엔티티.
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderReturnExchangeEntity {

  private Long id;
  private Long orderId;
  private OrderReturnExchangeType type;
  private OrderReturnExchangeReason reason;
  private String detail;
  private String status;
  private LocalDateTime requestedAt;
  private LocalDateTime processedAt;
}
