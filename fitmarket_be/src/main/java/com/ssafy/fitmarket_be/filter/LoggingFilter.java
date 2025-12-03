package com.ssafy.fitmarket_be.filter; // 패키지 경로는 적절히 수정하세요

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

// @Component는 FilterRegistrationBean으로 등록할 경우 제거하거나 @Order와 함께 사용해야 합니다.
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoggingFilter implements Filter {

  private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

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
      // 1. 요청 전 로깅 (Pre-Handle)
      logRequest(requestWrapper);

      // 2. 다음 필터 또는 서블릿으로 요청 전달
      chain.doFilter(requestWrapper, responseWrapper);

      // 3. 응답 후 로깅 (Post-Handle)
      logResponse(requestWrapper, responseWrapper);

    } finally {
      // 응답 내용을 클라이언트에게 전달하기 위해 반드시 호출해야 합니다.
      responseWrapper.copyBodyToResponse();
    }
  }

  private void logRequest(ContentCachingRequestWrapper request)
      throws UnsupportedEncodingException {
    // 요청 URI
    String uri = request.getRequestURI();
    String method = request.getMethod();

    // 쿼리 파라미터
    String queryString = request.getQueryString();

    // 요청 바디 (ContentCachingRequestWrapper를 사용해야 읽을 수 있습니다)
    String requestBody = new String(request.getContentAsByteArray(),
        request.getCharacterEncoding());

    log.trace(">>>> Request [{}] URI: {} (Query: {}) | Body: {}", method, uri, queryString,
        requestBody);
  }

  private void logResponse(ContentCachingRequestWrapper request,
      ContentCachingResponseWrapper response) throws UnsupportedEncodingException {
    String uri = request.getRequestURI();
    int status = response.getStatus();

    // 응답 바디 (ContentCachingResponseWrapper를 사용해야 읽을 수 있습니다)
    String responseBody = new String(response.getContentAsByteArray(),
        response.getCharacterEncoding());

    log.trace("<<<< Response [{}] URI: {} | Status: {} | Body: {}", request.getMethod(), uri,
        status, responseBody);
  }

}