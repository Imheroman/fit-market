package com.ssafy.fitmarket_be.user.service;

import com.ssafy.fitmarket_be.entity.User;
import com.ssafy.fitmarket_be.user.dto.UserDetailResponseDto;
import com.ssafy.fitmarket_be.user.dto.UserSignupRequestDto;
import com.ssafy.fitmarket_be.user.mapper.UserMapper;
import com.ssafy.fitmarket_be.user.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;

  public UserDetailResponseDto findByEmail(String email) {
    User user = this.userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("존재하지 않는 유저입니다."));

    return this.userMapper.toDto(user);
  }

  @Transactional
  public void signup(UserSignupRequestDto request) {
    Optional<User> existUser = this.userRepository.findByEmail(request.getEmail());

    if (existUser.isPresent()) {
      throw new RuntimeException("이미 존재하는 회원입니다.");
    }

    String pw = this.passwordEncoder.encode(request.getPassword());
    User user = User.create(request.getName(), request.getEmail(), pw, request.getPhone());

    int idx = userRepository.save(user);
    if (idx <= 0) {
      throw new RuntimeException("회원가입 실패 이메일: ".concat(request.getEmail()));
    }
  }

  @Transactional
  public void delete(String email) {
    int result = this.userRepository.delete(email);

    if (result <= 0) {
      throw new RuntimeException("회원 탈퇴 실패 이메일: ".concat(email));
    }
  }

  @Transactional
  public void updateName(String email, String name) {
    int result = this.update(email, "name", name);

    if (result <= 0) {
      throw new RuntimeException("회원 이름 수정 실패 이메일: ".concat(email));
    }
  }

  @Transactional
  public void updatePhone(String email, String phone) {
    int result = this.update(email, "phone", phone);

    if (result <= 0) {
      throw new RuntimeException("회원 전화번호 수정 실패 이메일: ".concat(email));
    }
  }

  @Transactional
  public void updatePassword(String email, String password) {
    int result = this.update(email, "password", this.passwordEncoder.encode(password));

    if (result <= 0) {
      throw new RuntimeException("회원 이름 수정 실패 이메일: ".concat(email));
    }
  }

  private int update(String email, String column, String value) {
    log.trace("update email: {}, column: {}, value: {}", email, column, value);
    return this.userRepository.update(email, column, value);
  }
}
