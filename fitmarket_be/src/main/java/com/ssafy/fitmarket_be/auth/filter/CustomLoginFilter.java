package com.ssafy.fitmarket_be.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
public class CustomLoginFilter extends UsernamePasswordAuthenticationFilter {

  private final ObjectMapper objectMapper;

  /**
   * 의존성 주입 후 경로 설정
   *
   * @param authenticationManager
   */
  public CustomLoginFilter(AuthenticationManager authenticationManager,
      ObjectMapper objectMapper) {
    super.setAuthenticationManager(authenticationManager);
    this.objectMapper = objectMapper;

    setFilterProcessesUrl("/auth/login");  // login uri
  }

  /**
   * 로그인 시도
   *
   * @return
   */
  @Override
  public Authentication attemptAuthentication(HttpServletRequest request,
      HttpServletResponse response) {
    if (!request.getMethod().equals("POST")) { // http method check
      throw new AuthenticationServiceException("지원하지 않는 로그인 메소드입니다: " + request.getMethod());
    }

    try {
      // 1) JSON Body 파싱
      LoginRequest loginRequest = this.objectMapper.readValue(request.getInputStream(),
          LoginRequest.class);

      log.trace("attempt login user email: {}", loginRequest.getEmail());

      // 2) AuthenticationManager에 위임
      return this.getAuthenticationManager().authenticate(new UsernamePasswordAuthenticationToken(
          loginRequest.getEmail(), loginRequest.getPassword()));
    } catch (IOException e) {
      log.error("로그인 요청 JSON 파싱 실패", e);
      throw new RuntimeException(e);
    }
  }

  static class LoginRequest {

    private String email;
    private String password;

    public LoginRequest() {
    }

    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }
  }
}
