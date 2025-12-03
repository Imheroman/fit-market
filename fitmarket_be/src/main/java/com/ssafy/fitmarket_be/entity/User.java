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
@Alias("Users")
public class User {

  private Long id;

  private String name;
  private String email;
  private String password;
  private String phone;
  private String role;

  private LocalDateTime createdDate;
  private LocalDateTime modifiedDate;
  private LocalDateTime deletedDate;

  public static User create(String name, String email, String encodedPassword, String phone) {
    return User.builder()
        .name(name)
        .email(email)
        .password(encodedPassword)
        .phone(phone)
        .build();
  }
}