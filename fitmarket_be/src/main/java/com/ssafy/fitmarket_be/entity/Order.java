package com.ssafy.fitmarket_be.entity;

import com.ssafy.fitmarket_be.order.domain.OrderMode;
import com.ssafy.fitmarket_be.payment.domain.PaymentStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주문 테이블에 매핑되는 도메인 엔티티.
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Order {

  private Long id;
  private String orderNumber;
  private OrderMode orderMode;
  private Long orderApprovalStatusId;
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
  private String itemsSnapshot;
}
