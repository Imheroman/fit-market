package com.ssafy.fitmarket_be.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Iterator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 전체 요청의 파라미터 등의 데이터를 확인하기 위한 interceptor
 */
@Component
@Slf4j
public class LoggingInterceptor implements HandlerInterceptor {

  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    log.debug("[{}] {} -> param: {}", request.getMethod(), request.getRequestURI(),
        getParams(request));

    return true;
  }

  /**
   * 요청 종료 후 결과 확인
   */
  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
      ModelAndView modelAndView) throws Exception {
    log.debug("결과 -> status: {}", response.getStatus());
  }

  /**
   * paramter 검색을 위한 method
   *
   * @param request http request
   * @return 파라미터 목록 문자열
   */
  private static StringBuilder getParams(HttpServletRequest request) {
    StringBuilder sb = new StringBuilder();
    Iterator<String> paramNames = request.getParameterNames().asIterator();
    while (paramNames.hasNext()) {
      String name = paramNames.next();
      sb.append("name: ").append(name).append(", val: ").append(request.getParameter(name))
          .append(" / ");
    }

    return sb;
  }
}
