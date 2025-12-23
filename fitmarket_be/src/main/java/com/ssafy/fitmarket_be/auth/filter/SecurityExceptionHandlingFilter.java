package com.ssafy.fitmarket_be.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Slf4j
public class SecurityExceptionHandlingFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            if (e instanceof JwtException) {// JWT 관련 예외 처리
               log.debug("JWT 예외 발생: {}, {}", request.getRequestURI(), e);

                setErrorResponse(response, HttpStatus.UNAUTHORIZED, "TOKEN_ERROR");
            } else if (e instanceof BadCredentialsException) { // 로그인 실패 관련 처리
                setErrorResponse(response, HttpStatus.UNAUTHORIZED, e.getMessage());
            } else { // 기타 예외 처리
                setErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }
    }

    private void setErrorResponse(HttpServletResponse response, HttpStatus status, String message)
            throws IOException {
        log.debug("에러 집중 처리국 {}, {}", status, message);
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE); // default utf-8
        response.setCharacterEncoding("UTF-8");
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("status", status.value());
        errorDetails.put("message", message);
        response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
    }
}

// END
