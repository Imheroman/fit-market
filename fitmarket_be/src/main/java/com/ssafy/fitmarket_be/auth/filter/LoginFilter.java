package com.ssafy.fitmarket_be.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.fitmarket_be.auth.CookieUtils;
import com.ssafy.fitmarket_be.auth.jwt.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

  private final AuthenticationManager authenticationManager;
  private final JwtUtil jwtUtil;

  /**
   * 의존성 주입 후 경로 설정
   *
   * @param authenticationManager
   * @param jwtUtil
   */
  public LoginFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
    this.authenticationManager = authenticationManager;
    this.jwtUtil = jwtUtil;

    setFilterProcessesUrl("/login");  // uri
  }

  /**
   * 로그인 시도
   *
   * @param request  from which to extract parameters and perform the authentication
   * @param response the response, which may be needed if the implementation has to do a redirect as
   *                 part of a multi-stage authentication process (such as OIDC).
   * @return
   */
  @Override
  public Authentication attemptAuthentication(HttpServletRequest request,
      HttpServletResponse response) {
    try {
      // 1) JSON Body 파싱
      ObjectMapper mapper = new ObjectMapper(); // 필드로 빼도 됨
      LoginRequest loginRequest = mapper.readValue(request.getInputStream(), LoginRequest.class);

      log.debug("login email: {}, pw: {}", loginRequest.getUsername(), loginRequest.getPassword());

      // 2) AuthenticationManager에 위임
      Authentication result = this.authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
              loginRequest.getPassword()));

      System.out.println("login result: " + result.isAuthenticated());

      return result;

    } catch (IOException e) {
      log.error("로그인 요청 JSON 파싱 실패", e);
      throw new RuntimeException(e);
    }
  }

  /**
   * 로그인 성공 후 로직
   *
   * @param request
   * @param response
   * @param chain
   * @param authResult the object returned from the <tt>attemptAuthentication</tt> method.
   * @throws IOException
   * @throws ServletException
   */
  @Override
  protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain, Authentication authResult) {
    User user = (User) authResult.getPrincipal();

    String token = this.jwtUtil.create(user.getUsername(), user.getAuthorities());
    Cookie cookie = CookieUtils.create("token", token);

    response.addCookie(cookie);
    response.setStatus(HttpStatus.OK.value());
  }

  @NoArgsConstructor
  @Getter
  @Setter
  static class LoginRequest {

    private String username;
    private String password;
  }
}
