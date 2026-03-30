package com.ssafy.fitmarket_be.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import org.springframework.http.ResponseCookie;

public class CookieUtils {
  private static final String TOKEN_NAME = "token";
  private static final int THIRTY_MINUTES = 60 * 30;
  private static final int ACCESS_TOKEN_MAX_AGE = 15 * 60;
  private static final int REFRESH_TOKEN_MAX_AGE = 7 * 24 * 60 * 60;

  private CookieUtils() {
  }

  private static ResponseCookie buildCookie(String name, String value, String path, long maxAge) {
    return ResponseCookie.from(name, value != null ? value : "")
        .httpOnly(true)
        .secure(true)
        .sameSite("Lax")
        .path(path)
        .maxAge(maxAge)
        .build();
  }

  public static ResponseCookie create(final String name, final String value) {
    return buildCookie(name, value, "/", THIRTY_MINUTES);
  }

  public static ResponseCookie createAccessTokenCookie(String token) {
    return buildCookie("access_token", token, "/", ACCESS_TOKEN_MAX_AGE);
  }

  public static ResponseCookie createRefreshTokenCookie(String token) {
    return buildCookie("refresh_token", token, "/api/auth", REFRESH_TOKEN_MAX_AGE);
  }

  public static ResponseCookie createExpireAccessToken() {
    return buildCookie("access_token", null, "/", 0);
  }

  public static ResponseCookie createExpireRefreshToken() {
    return buildCookie("refresh_token", null, "/api/auth", 0);
  }

  public static ResponseCookie createExpireToken() {
    return buildCookie(TOKEN_NAME, null, "/", 0);
  }

  /**
   * ResponseCookie를 HttpServletResponse에 추가하는 헬퍼.
   * response.addCookie(Cookie)를 대체한다.
   */
  public static void addCookie(jakarta.servlet.http.HttpServletResponse response, ResponseCookie cookie) {
    response.addHeader("Set-Cookie", cookie.toString());
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
