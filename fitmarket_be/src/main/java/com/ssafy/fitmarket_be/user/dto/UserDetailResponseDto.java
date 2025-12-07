package com.ssafy.fitmarket_be.user.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class UserDetailResponseDto {
  private final Long id;

  private final String name;
  private final String email;
  private final String phone;
  private final String role;

  private final LocalDateTime createdDate;
  private final LocalDateTime deletedDate;
}
