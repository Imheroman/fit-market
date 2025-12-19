package com.ssafy.fitmarket_be.order.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 주문 배송지 이력을 저장하는 엔티티.
 *
 * <p>주문 시점의 배송 정보를 영구 보관하여 이후 변경 내역을 추적한다.</p>
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OrderAddress {

  private Long id;
  private Long orderId;
  private String recipient;
  private String phone;
  private String postalCode;
  private String addressLine;
  private String addressLineDetail;
  private String memo;
  private boolean current;
}
