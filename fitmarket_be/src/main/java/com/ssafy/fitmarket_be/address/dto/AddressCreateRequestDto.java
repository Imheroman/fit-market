package com.ssafy.fitmarket_be.address.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 배송지 생성 요청 정보를 담는 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class AddressCreateRequestDto {
  @Size(max = 16, message = "수령인은 16자 이내로 입력해주세요.")
  private String name;

  @Size(max = 100, message = "수령인은 100자 이내로 입력해주세요.")
  private String recipient;

  @NotBlank(message = "핸드폰 번호를 입력해 주세요.")
  @Size(max = 30, message = "전화번호는 30자 이내로 입력해 주세요.")
  private String phone;

  @Size(max = 255, message = "메모는 200자 이내로 입력해 주세요.")
  private String memo;

  @Size(max = 15, message = "우편번호는 15자 이내로 입력해 주세요.")
  private String postalCode;

  @NotBlank(message = "도로명 주소를 입력해 주세요.")
  @Size(max = 255, message = "도로명 주소는 255자 이내로 입력해 주세요.")
  private String addressLine;

  @NotBlank(message = "상세 주소를 입력해 주세요.")
  @Size(max = 255, message = "상세 주소는 255자 이내로 입력해 주세요.")
  private String addressLineDetail;

  /**
   * 기본 배송지 여부.
   */
  private Boolean main;
}
