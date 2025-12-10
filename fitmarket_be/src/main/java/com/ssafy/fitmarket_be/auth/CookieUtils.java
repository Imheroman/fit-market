package com.ssafy.fitmarket_be.auth;

import com.ssafy.fitmarket_be.auth.exception.UnauthenticatedException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Objects;

public class CookieUtils {
  private static final String TOKEN_NAME = "token";
  private static final int EXPIRATION = 0;
//  private static final int THIRTY_MINUTES = 60 * 30;  // 30분
private static final int THIRTY_MINUTES = 60 * 60;  // 30분

  private CookieUtils() {
  }

  private static Cookie create(final String name, final String value, final int maxAge) {
    Cookie cookie = new Cookie(name, value);

    // cookie.setHttpOnly(true); // XSS 방지 (필수)
    // cookie.setSecure(true);   // HTTPS 적용 시 필수
    cookie.setHttpOnly(true);  // 통신에서만 (javascript에서 조작 금지)
    cookie.setMaxAge(maxAge);
    cookie.setPath("/");

    return cookie;
  }

  public static Cookie create(final String name, final String value) {
    return create(name, value, THIRTY_MINUTES);
  }

  /**
   * 토큰을 삭제함
   * @return
   */
  public static Cookie createExpireToken() {
    return create(TOKEN_NAME, null, EXPIRATION);
  }

  public static String find(final HttpServletRequest request, String name) {
    final Cookie[] cookies = request.getCookies();

    if (Objects.isNull(cookies)) {
      throw new UnauthenticatedException();
    }

    return Arrays.stream(cookies)
        .filter(c -> c.getName().equals(name))
        .findFirst()
        .map(Cookie::getValue)
        .orElseThrow(UnauthenticatedException::new);
  }
}
