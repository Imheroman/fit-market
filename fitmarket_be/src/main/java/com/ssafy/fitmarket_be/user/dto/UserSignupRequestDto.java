package com.ssafy.fitmarket_be.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserSignupRequestDto {
  @NotBlank(message = "이름을 입력해 주세요.")
  private String name;

  @NotBlank(message = "이메일을 입력해 주세요.")
  @Email(message = "올바른 이메일 형식이 아닙니다.")
  private String email;

  @NotBlank(message = "비밀번호를 입력해 주세요.")
  @Size(min = 8, max = 64, message = "비밀번호는 8자 이상 64자 이하여야 합니다.")
  private String password;

  @NotBlank(message = "전화번호를 입력해 주세요.")
  @Pattern(regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$", message = "올바른 전화번호 형식이 아닙니다. (예: 010-1234-5678)")
  private String phone;
}
