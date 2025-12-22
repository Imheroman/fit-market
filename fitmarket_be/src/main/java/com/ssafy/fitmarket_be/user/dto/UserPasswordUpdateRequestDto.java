package com.ssafy.fitmarket_be.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserPasswordUpdateRequestDto {

  @NotBlank(message = "현재 비밀번호를 입력해주세요.")
  private String currentPassword;

  @Pattern(
      regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$",
      message = "비밀번호는 영문과 숫자를 포함해 8자 이상이어야 합니다. (특수문자 허용)"
  )
  private String newPassword;

}