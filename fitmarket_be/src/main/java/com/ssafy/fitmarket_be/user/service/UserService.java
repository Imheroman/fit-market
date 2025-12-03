package com.ssafy.fitmarket_be.user.service;

import com.ssafy.fitmarket_be.entity.User;
import com.ssafy.fitmarket_be.user.dto.UserSignupRequestDto;
import com.ssafy.fitmarket_be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public User findByEmail(String email) {
    return this.userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("존재하지 않는 유저입니다."));
  }

  @Transactional
  public void signup(UserSignupRequestDto request) {
    String pw = this.passwordEncoder.encode(request.getPassword());
    User user = User.create(request.getName(), request.getEmail(), pw, request.getPhone());

    int idx = userRepository.save(user);
    if (idx <= 0) throw new RuntimeException("회원가입 실패 이메일: ".concat(request.getEmail()));
  }
}
