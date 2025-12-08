package com.ssafy.fitmarket_be.address.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AddressUpdateRequestDto {

  @Size(max = 15, message = "우편번호는 15자 이내로 입력해 주세요.")
  private String postalCode;

  @Size(max = 255, message = "도로명 주소는 255자 이내로 입력해 주세요.")
  private String addressLine;

  @Size(max = 255, message = "상세 주소는 255자 이내로 입력해 주세요.")
  private String addressLineDetail;

  public boolean hasUpdatableField() {
    return postalCode != null || addressLine != null || addressLineDetail != null;
  }
}
