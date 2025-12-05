package com.ssafy.fitmarket_be.user.controller;

import com.ssafy.fitmarket_be.user.dto.UserDetailResponseDto;
import com.ssafy.fitmarket_be.user.dto.UserSignupRequestDto;
import com.ssafy.fitmarket_be.user.dto.UserUpdateRequestDto;
import com.ssafy.fitmarket_be.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
      @AuthenticationPrincipal UserDetails userDetails) {
    UserDetailResponseDto user = this.userService.findByEmail(userDetails.getUsername());

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
  public ResponseEntity<Void> updateName(@AuthenticationPrincipal UserDetails userDetails,
      @RequestBody UserUpdateRequestDto request) {
    this.userService.updateName(userDetails.getUsername(), request.getValue());
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @PatchMapping("/phone")
  public ResponseEntity<Void> updatePhone(@AuthenticationPrincipal UserDetails userDetails,
      @RequestBody UserUpdateRequestDto request) {
    this.userService.updatePhone(userDetails.getUsername(), request.getValue());
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @PatchMapping("/password")
  public ResponseEntity<Void> updatePassword(@AuthenticationPrincipal UserDetails userDetails,
      @RequestBody UserUpdateRequestDto request) {
    this.userService.updatePassword(userDetails.getUsername(), request.getValue());
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @DeleteMapping
  public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails userDetails) {
    this.userService.delete(userDetails.getUsername());
    return ResponseEntity.status(HttpStatus.OK).build();
  }
}