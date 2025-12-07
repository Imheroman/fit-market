package com.ssafy.fitmarket_be.user.mapper;

import com.ssafy.fitmarket_be.entity.User;
import com.ssafy.fitmarket_be.user.dto.UserDetailResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
  UserDetailResponseDto toDto(User user);
}