package com.ssafy.fitmarket_be.user.controller;

import com.ssafy.fitmarket_be.user.dto.UserDetailResponseDto;
import com.ssafy.fitmarket_be.user.dto.UserSignupRequestDto;
import com.ssafy.fitmarket_be.user.dto.UserUpdateRequestDto;
import com.ssafy.fitmarket_be.user.dto.UserUpdateResponseDto;
import com.ssafy.fitmarket_be.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/users")
public class UserController {

  private final UserService userService;

  @GetMapping
  public ResponseEntity<UserDetailResponseDto> find(
      @AuthenticationPrincipal(expression = "id") Long id) {
    UserDetailResponseDto user = this.userService.findById(id);

    return ResponseEntity.status(HttpStatus.OK)
        .body(user);
  }

  @PostMapping("/signup")
  public ResponseEntity<Void> signup(@RequestBody UserSignupRequestDto requestDto) {
    this.userService.signup(requestDto);
    log.trace("회원가입 성공 이메일: {}", requestDto.getEmail());

    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @PatchMapping("/name")
  public ResponseEntity<UserUpdateResponseDto> updateName(
      @AuthenticationPrincipal(expression = "id") Long id,
      @RequestBody UserUpdateRequestDto request) {
    UserUpdateResponseDto profile = this.userService.updateName(id,
        request.getValue());
    return ResponseEntity.status(HttpStatus.OK).body(profile);
  }

  @PatchMapping("/phone")
  public ResponseEntity<UserUpdateResponseDto> updatePhone(
      @AuthenticationPrincipal(expression = "id") Long id,
      @RequestBody UserUpdateRequestDto request) {
    UserUpdateResponseDto profile = this.userService.updatePhone(id,
        request.getValue());
    return ResponseEntity.status(HttpStatus.OK).body(profile);
  }

  @PatchMapping("/password")
  public ResponseEntity<UserUpdateResponseDto> updatePassword(
      @AuthenticationPrincipal(expression = "id") Long id,
      @RequestBody UserUpdateRequestDto request) {
    UserUpdateResponseDto profile = this.userService.updatePassword(id,
        request.getValue());
    return ResponseEntity.status(HttpStatus.OK).body(profile);
  }

  @DeleteMapping
  public ResponseEntity<Void> delete(@AuthenticationPrincipal(expression = "id") Long id) {
    this.userService.delete(id);
    return ResponseEntity.status(HttpStatus.OK).build();
  }
}