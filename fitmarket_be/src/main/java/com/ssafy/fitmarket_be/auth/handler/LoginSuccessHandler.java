package com.ssafy.fitmarket_be.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.fitmarket_be.auth.CookieUtils;
import com.ssafy.fitmarket_be.auth.dto.CustomUserDetails;
import com.ssafy.fitmarket_be.auth.jwt.JwtUtil;
import com.ssafy.fitmarket_be.cart.service.CartService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

  private final JwtUtil jwtUtil;
  private final ObjectMapper objectMapper;
  private final CartService cartService;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {
    CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
    String token = jwtUtil.create(user.getId(), user.getUsername(), user.getAuthorities());

    Cookie cookie = CookieUtils.create("token", token);
    response.addCookie(cookie);

    // 4. Cart 정보 조회 (서비스 호출)
    int cartCount = cartService.countCartItems(user.getId());
    log.info("로그인 성공: userId={}, cartCount={}", user.getId(), cartCount);

    // 5. JSON 응답 생성
    sendJsonResponse(response, user.getName(), cartCount);
  }

  private void sendJsonResponse(HttpServletResponse response, String name, int cartCount)
      throws IOException {
    response.setStatus(HttpStatus.OK.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());

    LoginResponseDto responseDto = new LoginResponseDto(name, cartCount);
    objectMapper.writeValue(response.getWriter(), responseDto);
  }

  @Getter
  @AllArgsConstructor
  private static class LoginResponseDto {

    private String name;
    private int cartCount;
  }
}