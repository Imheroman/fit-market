package com.ssafy.fitmarket_be.auth.controller;

import com.ssafy.fitmarket_be.auth.CookieUtils;
import com.ssafy.fitmarket_be.auth.dto.CustomUserDetails;
import com.ssafy.fitmarket_be.auth.jwt.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/auth")
public class AuthController {

  private final AuthenticationManager authenticationManager;
  private final JwtUtil jwtUtil;
//  private final CartService cartService;

@PostMapping("/login")
public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request,
    HttpServletResponse response) {
  log.trace("attempt login email: {}", request.getEmail());

  UsernamePasswordAuthenticationToken authToken =
      new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

  Authentication authentication = this.authenticationManager.authenticate(authToken);
  CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

  String token = this.jwtUtil.create(user.getId(), user.getUsername(), user.getAuthorities());
  Cookie cookie = CookieUtils.create("token", token);
  response.addCookie(cookie);

  log.trace("login success email: {}, token: {}", request.getEmail(), token);
//    int cartCount = cartService.countByUserId(user.getId());
  int cartCount = 0;
  return ResponseEntity.ok(new LoginResponse(user.getName(), cartCount));                                                // Changed
}

  @NoArgsConstructor
  @Getter
  @Setter
  @ToString
  public static class LoginRequest {
    private String email;
    private String password;
  }

  @AllArgsConstructor
  @Getter
  public static class LoginResponse {
    private String name;
    private int cartCount;
  }
}
