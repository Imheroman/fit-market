package com.ssafy.fitmarket_be.order.domain;

import com.ssafy.fitmarket_be.entity.Address;

/**
 * 주문 시점 배송지 스냅샷.
 *
 * @param name               배송지명
 * @param recipient          수령인
 * @param phone              연락처
 * @param postalCode         우편번호
 * @param addressLine        기본 주소
 * @param addressLineDetail  상세 주소
 * @param memo               배송 메모
 */
public record OrderAddressSnapshot(
    String name,
    String recipient,
    String phone,
    String postalCode,
    String addressLine,
    String addressLineDetail,
    String memo
) {

  /**
   * Address 엔티티를 스냅샷으로 변환한다.
   *
   * @param address 배송지 엔티티
   * @return 스냅샷 DTO
   */
  public static OrderAddressSnapshot from(Address address) {
    return new OrderAddressSnapshot(
        address.getName(),
        address.getRecipient(),
        address.getPhone(),
        address.getPostalCode(),
        address.getAddressLine(),
        address.getAddressLineDetail(),
        address.getMemo()
    );
  }
}
