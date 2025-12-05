package com.ssafy.fitmarket_be.user.controller;

import com.ssafy.fitmarket_be.entity.User;
import com.ssafy.fitmarket_be.user.dto.UserDetailResponseDto;
import com.ssafy.fitmarket_be.user.dto.UserSignupRequestDto;
import com.ssafy.fitmarket_be.user.service.UserService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
  public ResponseEntity<UserDetailResponseDto> find(@AuthenticationPrincipal UserDetails userDetails) {
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

//  @PutMapping
//  public ResponseEntity<?> update() {
//    this.userService.update(request);
//    return ResponseEntity.status(HttpStatus.OK).build();
//  }

  @DeleteMapping
  public ResponseEntity<Void> delete (@AuthenticationPrincipal UserDetails userDetails) {
    this.userService.delete(userDetails.getUsername());
    return ResponseEntity.status(HttpStatus.OK).build();
  }
}