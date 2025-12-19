package com.ssafy.fitmarket_be.order.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 주문 상품 스냅샷을 저장하는 엔티티.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderProductEntity {

  private Long id;
  private Long orderId;
  private Long productId;
  private Long cartItemId;
  private String productName;
  private int quantity;
  private Long unitPrice;
  private Long totalPrice;
  private String optionInfo;
}
