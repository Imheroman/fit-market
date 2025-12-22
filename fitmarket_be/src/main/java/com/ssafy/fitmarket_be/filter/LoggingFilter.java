package com.ssafy.fitmarket_be.filter; // 패키지 경로는 적절히 수정하세요

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * HTTP 요청/응답의 쿼리 스트링과 바디를 로그로 남기는 서블릿 필터.
 */
// @Component는 FilterRegistrationBean으로 등록할 경우 제거하거나 @Order와 함께 사용해야 합니다.
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoggingFilter implements Filter {

  private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

  /**
   * 요청/응답을 캐싱 래퍼로 감싸 쿼리 스트링과 바디 내용을 로깅한다.
   *
   * @param request 요청 객체
   * @param response 응답 객체
   * @param chain 다음 필터 체인
   * @throws IOException I/O 오류가 발생하는 경우
   * @throws ServletException 서블릿 예외가 발생하는 경우
   */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    // HTTP 요청/응답이 아니면 체인 통과
    if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
      chain.doFilter(request, response);
      return;
    }

    // ContentCaching Wrapper를 사용하여 InputStream을 다시 읽을 수 있게 합니다. (중요!)
    ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(
        (HttpServletRequest) request);
    ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(
        (HttpServletResponse) response);

    try {
      chain.doFilter(requestWrapper, responseWrapper);
    } finally {
      logRequest(requestWrapper);
      logResponse(requestWrapper, responseWrapper);
      // 응답 내용을 클라이언트에게 전달하기 위해 반드시 호출해야 합니다.
      responseWrapper.copyBodyToResponse();
    }
  }

  /**
   * 요청 URI, 쿼리 스트링, 파라미터 맵, 바디를 로깅한다.
   *
   * @param request 요청 캐싱 래퍼
   */
  private void logRequest(ContentCachingRequestWrapper request) {
    // 요청 URI
    String uri = request.getRequestURI();
    String method = request.getMethod();

    // 쿼리 파라미터
    String queryString = request.getQueryString();
    Map<String, String[]> parameterMap = request.getParameterMap();

    // 요청 바디 (ContentCachingRequestWrapper를 사용해야 읽을 수 있습니다)
    String requestBody = new String(request.getContentAsByteArray(), StandardCharsets.UTF_8);

    log.trace(">>>> Request [{}] URI: {} (Query: {}) | Params: {} | Body: {}", method, uri,
        queryString, parameterMap, requestBody);
  }

  /**
   * 응답 상태와 바디를 로깅한다.
   *
   * @param request 요청 캐싱 래퍼
   * @param response 응답 캐싱 래퍼
   */
  private void logResponse(ContentCachingRequestWrapper request,
      ContentCachingResponseWrapper response) {
    String uri = request.getRequestURI();
    int status = response.getStatus();

    // 응답 바디 (ContentCachingResponseWrapper를 사용해야 읽을 수 있습니다)
    String responseBody = new String(response.getContentAsByteArray(), StandardCharsets.UTF_8);

    log.trace("<<<< Response [{}] URI: {} | Status: {} | Body: {}", request.getMethod(), uri,
        status, responseBody);
  }

}
