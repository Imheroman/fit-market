package com.ssafy.fitmarket_be.user.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class UserUpdateResponseDto {
  private final String profile;
}
