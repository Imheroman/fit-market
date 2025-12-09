package com.ssafy.fitmarket_be.address.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AddressResponseDto {
  private final Long id;
  private final String name;
  private final String recipient;
  private final String phone;
  private final String memo;
  private final String postalCode;
  private final String addressLine;
  private final String addressLineDetail;
  private final boolean main;
  private final LocalDateTime createdDate;
  private final LocalDateTime modifiedDate;
  private final LocalDateTime deletedDate;
}
