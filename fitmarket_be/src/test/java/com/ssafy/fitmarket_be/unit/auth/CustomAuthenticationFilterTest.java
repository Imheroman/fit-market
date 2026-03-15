package com.ssafy.fitmarket_be.unit.auth;

import com.ssafy.fitmarket_be.auth.filter.CustomAuthenticationFilter;
import com.ssafy.fitmarket_be.auth.jwt.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomAuthenticationFilter")
class CustomAuthenticationFilterTest {

    @Mock
    JwtUtil jwtUtil;
    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;
    @Mock
    FilterChain filterChain;

    @InjectMocks
    CustomAuthenticationFilter filter;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("쿠키가 없으면 SecurityContext에 인증 정보를 설정하지 않고 필터를 통과한다")
    void doFilterInternal_쿠키없음_필터통과() throws Exception {
        // given
        given(request.getCookies()).willReturn(null);

        // when: protected doFilterInternal은 doFilter를 통해 간접 호출
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("유효한 토큰이 있으면 SecurityContext에 인증 정보가 설정된다")
    void doFilterInternal_유효토큰_인증설정() throws Exception {
        // given
        String tokenValue = "valid.token.value";
        given(request.getCookies()).willReturn(validCookies(tokenValue));
        given(jwtUtil.isExpired(tokenValue)).willReturn(false);
        given(jwtUtil.getId(tokenValue)).willReturn(1L);
        given(jwtUtil.getRole(tokenValue)).willReturn("ROLE_USER");
        given(jwtUtil.getUsername(tokenValue)).willReturn("user@test.com");
        given(jwtUtil.create(any(), any(), any())).willReturn("new.token.value");

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    @DisplayName("유효한 토큰이 있으면 Sliding Window 방식으로 새 쿠키를 발급한다")
    void doFilterInternal_유효토큰_새쿠키발급() throws Exception {
        // given
        String tokenValue = "valid.token.value";
        given(request.getCookies()).willReturn(validCookies(tokenValue));
        given(jwtUtil.isExpired(tokenValue)).willReturn(false);
        given(jwtUtil.getId(tokenValue)).willReturn(1L);
        given(jwtUtil.getRole(tokenValue)).willReturn("ROLE_USER");
        given(jwtUtil.getUsername(tokenValue)).willReturn("user@test.com");
        given(jwtUtil.create(any(), any(), any())).willReturn("new.token.value");

        // when
        filter.doFilter(request, response, filterChain);

        // then
        verify(response).addCookie(argThat(c -> "token".equals(c.getName())));
    }

    @Test
    @DisplayName("만료된 토큰이면 SecurityContext를 비우고 필터를 통과한다")
    void doFilterInternal_만료토큰_필터통과() throws Exception {
        // given
        String tokenValue = "expired.token.value";
        given(request.getCookies()).willReturn(validCookies(tokenValue));
        given(jwtUtil.isExpired(tokenValue)).willThrow(
                new ExpiredJwtException(null, null, "Token expired"));

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("위조된 토큰이면 SecurityContext를 비운다")
    void doFilterInternal_위조토큰_SecurityContext비움() throws Exception {
        // given
        String tokenValue = "forged.token.value";
        given(request.getCookies()).willReturn(validCookies(tokenValue));
        given(jwtUtil.isExpired(tokenValue)).willThrow(
                new RuntimeException("유효하지 않은 토큰입니다."));

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    // ===== 헬퍼 =====

    private Cookie[] validCookies(String tokenValue) {
        Cookie cookie = new Cookie("token", tokenValue);
        return new Cookie[]{cookie};
    }
}
