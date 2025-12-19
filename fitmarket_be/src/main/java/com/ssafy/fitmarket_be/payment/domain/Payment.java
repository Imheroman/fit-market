package com.ssafy.fitmarket_be.payment.domain;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제 테이블 매핑 엔티티.
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Payment {

  private Long id;
  private Long orderId;
  private String paymentKey;
  private String provider;
  private String method;
  private PaymentStatus status;
  private Long amount;
  private LocalDateTime approvedAt;
  private String failedCode;
  private String failedMessage;
  private String rawResponse;
}
