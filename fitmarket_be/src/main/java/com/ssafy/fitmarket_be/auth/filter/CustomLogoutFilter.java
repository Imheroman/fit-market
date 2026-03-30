package com.ssafy.fitmarket_be.auth.filter;

import com.ssafy.fitmarket_be.auth.CookieUtils;
import com.ssafy.fitmarket_be.auth.jwt.JwtUtil;
import com.ssafy.fitmarket_be.auth.service.RedisRefreshTokenService;
import com.ssafy.fitmarket_be.auth.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class CustomLogoutFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final RedisRefreshTokenService refreshTokenService;
  private final TokenBlacklistService blacklistService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    if (!request.getRequestURI().equals("/api/logout")
        || !request.getMethod().equalsIgnoreCase("POST")) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      Optional<String> tokenOpt = CookieUtils.find(request, "access_token");
      if (tokenOpt.isPresent()) {
        String accessToken = tokenOpt.get();
        try {
          Long userId = jwtUtil.getId(accessToken);
          // RT 삭제
          refreshTokenService.delete(userId);
          // AT Blacklist 등록
          String jti = jwtUtil.getJti(accessToken);
          long remaining = jwtUtil.getRemainingExpiration(accessToken);
          blacklistService.add(jti, remaining);
        } catch (Exception e) {
          // 토큰 파싱 실패해도 쿠키는 삭제
        }
      }
    } catch (Exception e) {
      // 무시 — 쿠키 삭제는 항상 수행
    }

    // AT + RT 쿠키 만료
    CookieUtils.addCookie(response, CookieUtils.createExpireAccessToken());
    CookieUtils.addCookie(response, CookieUtils.createExpireRefreshToken());
    response.setStatus(HttpStatus.OK.value());
  }
}
