package com.ssafy.fitmarket_be.user.controller;

import com.ssafy.fitmarket_be.user.dto.UserSignupRequestDto;
import com.ssafy.fitmarket_be.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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

  @PostMapping("/signup")
  public ResponseEntity<Void> signup(@RequestBody UserSignupRequestDto requestDto) {
    this.userService.signup(requestDto);
    log.trace("회원가입 성공 이메일: {}", requestDto.getEmail());

    return ResponseEntity.ok().build();
  }
}