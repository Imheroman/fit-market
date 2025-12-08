package com.ssafy.fitmarket_be.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Alias("Address")
public class Address {
  private Long id;

  private String postalCode;  // 우편 번호
  private String addressLine;  // 주소
  private String addressLineDetail;  // 상세 주소

  // 공통 날짜 필드
  private LocalDateTime createdDate;
  private LocalDateTime modifiedDate;
  private LocalDateTime deletedDate;
}