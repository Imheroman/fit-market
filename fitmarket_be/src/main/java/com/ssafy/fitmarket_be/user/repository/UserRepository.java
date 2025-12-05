package com.ssafy.fitmarket_be.user.repository;

import com.ssafy.fitmarket_be.entity.User;
import com.ssafy.fitmarket_be.user.dto.UserUpdateRequestDto;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserRepository {

  Optional<User> findByEmail(String email);

  int save(User user);

  int delete(String email);

  int update(@Param("email") String username,
      @Param("column") String column,
      @Param("value") String value);
}
