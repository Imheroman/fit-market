package com.ssafy.fitmarket_be.auth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException authException) throws IOException {

    // 필터에서 넘겨준 예외가 있는지 확인
    Exception exception = (Exception) request.getAttribute("exception");

    // ★ 핵심: 에러 로그 레벨 조정 (System.out이나 e.printStackTrace 금지)
    // "토큰 만료" 같은 흔한 인증 에러는 error 레벨이 아니라 warn이나 debug로 찍습니다.
    // 스택 트레이스(e)를 로그에 남기지 않고 메시지만 남깁니다.
    if (exception != null) {
      log.warn("Authentication Failed: {} - Request URI: {}", exception.getMessage(), request.getRequestURI());
    } else {
      log.debug("Unauthorized access attempt: {}", request.getRequestURI());
    }

    // 클라이언트에게는 여전히 401을 명확히 줍니다.
    sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
  }

  // 공통 응답 메소드 (private으로 캡슐화)
  private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
    response.setStatus(status);
    response.setContentType("application/json;charset=UTF-8");
    // 예시 JSON 포맷. 실제 프로젝트에서는 ObjectMapper를 사용해 DTO를 변환하는 것이 좋습니다.
    response.getWriter().write(String.format("{\"status\": %d, \"error\": \"%s\"}", status, message));
  }
}