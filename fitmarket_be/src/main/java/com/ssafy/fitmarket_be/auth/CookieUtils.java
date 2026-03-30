package com.ssafy.fitmarket_be.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class CookieUtils {
  private static final String TOKEN_NAME = "token";
  private static final int EXPIRATION = 0;
  private static final int THIRTY_MINUTES = 60 * 30;  // 30분 = 1800초
  private static final int ACCESS_TOKEN_MAX_AGE = 15 * 60; // 15분
  private static final int REFRESH_TOKEN_MAX_AGE = 7 * 24 * 60 * 60; // 7일

  private CookieUtils() {
  }

  private static Cookie create(final String name, final String value, final int maxAge) {
    Cookie cookie = new Cookie(name, value);

    cookie.setHttpOnly(true);
    cookie.setMaxAge(maxAge);
    cookie.setPath("/");

    return cookie;
  }

  public static Cookie create(final String name, final String value) {
    return create(name, value, THIRTY_MINUTES);
  }

  public static Cookie createAccessTokenCookie(String token) {
    return create("access_token", token, ACCESS_TOKEN_MAX_AGE);
  }

  public static Cookie createRefreshTokenCookie(String token) {
    Cookie cookie = new Cookie("refresh_token", token);
    cookie.setHttpOnly(true);
    cookie.setPath("/api/auth");
    cookie.setMaxAge(REFRESH_TOKEN_MAX_AGE);
    return cookie;
  }

  public static Cookie createExpireAccessToken() {
    Cookie cookie = new Cookie("access_token", null);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge(0);
    return cookie;
  }

  public static Cookie createExpireRefreshToken() {
    Cookie cookie = new Cookie("refresh_token", null);
    cookie.setHttpOnly(true);
    cookie.setPath("/api/auth");
    cookie.setMaxAge(0);
    return cookie;
  }

  /**
   * 토큰을 삭제함 (하위 호환)
   * @return
   */
  public static Cookie createExpireToken() {
    return create(TOKEN_NAME, null, EXPIRATION);
  }

  public static Optional<String> find(final HttpServletRequest request, String name) {
    final Cookie[] cookies = request.getCookies();

    if (Objects.isNull(cookies)) {
      return Optional.empty();
    }

    return Arrays.stream(cookies)
        .filter(c -> c.getName().equals(name))
        .findFirst()
        .map(Cookie::getValue);
  }
}
