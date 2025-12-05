package com.ssafy.fitmarket_be.user.repository;

import com.ssafy.fitmarket_be.entity.User;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserRepository {
  Optional<User> findByEmail(String email);

  int save(User user);

  int delete(String email);
}
