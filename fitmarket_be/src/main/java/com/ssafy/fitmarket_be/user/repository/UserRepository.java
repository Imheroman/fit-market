package com.ssafy.fitmarket_be.user.repository;

import com.ssafy.fitmarket_be.entity.User;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserRepository {

  Optional<User> findByEmail(String email);

  Optional<User> findBy(Long id);

  int save(User user);

  int delete(Long id);

  int update(@Param("id") Long id,
      @Param("column") String column,
      @Param("value") String value);
}
