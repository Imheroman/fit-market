package com.ssafy.fitmarket_be.user.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserSignupRequestDto {
  private String name;
  private String email;
  private String password;
  private String phone;
}
