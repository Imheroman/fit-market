package com.ssafy.fitmarket_be.user.service;

import com.ssafy.fitmarket_be.entity.User;
import com.ssafy.fitmarket_be.user.dto.UserDetailResponseDto;
import com.ssafy.fitmarket_be.user.dto.UserPasswordUpdateRequestDto;
import com.ssafy.fitmarket_be.user.dto.UserSignupRequestDto;
import com.ssafy.fitmarket_be.user.dto.UserUpdateResponseDto;
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

  public UserDetailResponseDto findById(Long id) {
    User user = this.userRepository.findBy(id)
        .orElseThrow(() -> new RuntimeException("존재하지 않는 유저입니다."));

    return this.userMapper.toDto(user);
  }

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
  public void delete(Long id) {
    int result = this.userRepository.delete(id);

    if (result <= 0) {
      throw new RuntimeException("회원 탈퇴 실패 이메일: ".concat(id.toString()));
    }
  }

  @Transactional
  public UserUpdateResponseDto updateName(Long id, String name) {
    int result = this.update(id, "name", name);

    if (result <= 0) {
      throw new RuntimeException("회원 이름 수정 실패 이메일: ".concat(id.toString()));
    }

    return new UserUpdateResponseDto(name);
  }

  @Transactional
  public UserUpdateResponseDto updatePhone(Long id, String phone) {
    int result = this.update(id, "phone", phone);

    if (result <= 0) {
      throw new RuntimeException("회원 전화번호 수정 실패 이메일: ".concat(id.toString()));
    }

    return new UserUpdateResponseDto(phone);
  }

  /**
   * 사용자의 현재 비밀번호를 검증한 뒤 새로운 비밀번호로 변경한다.
   *
   * @param id 사용자 식별자
   * @param request 비밀번호 변경 요청 DTO
   * @return 업데이트 결과 DTO
   * @throws RuntimeException 사용자를 찾지 못했거나 비밀번호 검증/변경에 실패한 경우
   */
  @Transactional
  public UserUpdateResponseDto updatePassword(Long id, UserPasswordUpdateRequestDto request) {
    User user = this.userRepository.findBy(id)
        .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없어요. 다시 확인해 주세요."));

    if (!this.passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
      throw new IllegalArgumentException("현재 비밀번호가 일치하지 않아요. 다시 확인해 주세요.");
    }

    if (this.passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
      throw new IllegalArgumentException("새 비밀번호는 현재 비밀번호와 달라야 해요.");
    }

    String encodedPassword = this.passwordEncoder.encode(request.getNewPassword());
    int result = this.update(id, "password", encodedPassword);

    if (result <= 0) {
      throw new RuntimeException("비밀번호 변경에 실패했어요. 잠시 후 다시 시도해 주세요.");
    }

    return new UserUpdateResponseDto("");
  }

  private int update(Long id, String column, String value) {
    log.trace("update userId: {}, column: {}", id, column);
    return this.userRepository.update(id, column, value);
  }
}
