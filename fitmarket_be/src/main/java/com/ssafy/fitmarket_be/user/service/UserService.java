package com.ssafy.fitmarket_be.user.service;

import com.ssafy.fitmarket_be.entity.User;
import com.ssafy.fitmarket_be.user.dto.UserDetailResponseDto;
import com.ssafy.fitmarket_be.user.dto.UserSignupRequestDto;
import com.ssafy.fitmarket_be.user.mapper.UserMapper;
import com.ssafy.fitmarket_be.user.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
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
    if (idx <= 0) throw new RuntimeException("회원가입 실패 이메일: ".concat(request.getEmail()));
  }

  public void delete(String email) {
    int result = this.userRepository.delete(email);

    if (result <= 0) throw new RuntimeException("회원 탈퇴 실패 이메일: ".concat(email));
  }
}
