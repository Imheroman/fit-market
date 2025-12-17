package com.ssafy.fitmarket_be.order.domain;

import com.ssafy.fitmarket_be.payment.domain.PaymentStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 주문 조회 시 사용되는 읽기 전용 뷰 모델.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderView {

  private Long id;
  private String orderNumber;
  private OrderMode orderMode;
  private String approvalStatus;
  private Long addressId;
  private String addressSnapshot;
  private Long userId;
  private LocalDateTime orderDate;
  private LocalDateTime shipDate;
  private LocalDateTime dueDate;
  private Long merchandiseAmount;
  private Long shippingFee;
  private Long discountAmount;
  private Long totalAmount;
  private PaymentStatus paymentStatus;
  private String comment;
}
