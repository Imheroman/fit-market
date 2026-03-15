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

  int updateName(@Param("id") Long id, @Param("name") String name);

  int updatePhone(@Param("id") Long id, @Param("phone") String phone);

  int updatePassword(@Param("id") Long id, @Param("password") String password);

  int updateRole(@Param("id") Long id, @Param("role") String role);
}
