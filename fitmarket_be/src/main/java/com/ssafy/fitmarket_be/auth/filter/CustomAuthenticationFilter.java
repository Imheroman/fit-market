package com.ssafy.fitmarket_be.auth.filter;

import com.ssafy.fitmarket_be.auth.CookieUtils;
import com.ssafy.fitmarket_be.auth.jwt.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;

//  @Override
//  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
//      FilterChain filterChain) throws ServletException, IOException {
//
//    if (isPermitRequest(request.getRequestURI())) {
//      filterChain.doFilter(request, response);
//      return;
//    }
//
//    String token = CookieUtils.find(request, "token");
//
//    if (!(token == null || this.jwtUtil.isExpired(token))) {
//      Long id = this.jwtUtil.getId(token);
//      String role = this.jwtUtil.getRole(token);
//
//      AuthUserPrincipal principal = new AuthUserPrincipal(id, role);
//      SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
//          principal, null, principal.getAuthorities()
//      ));
//    }
//
//    filterChain.doFilter(request, response);
//  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    // 1. 굳이 여기서 URL 검사를 할 필요 없음 (SecurityConfig에 맡김)


    // 2. try-catch로 감싸서 토큰 오류가 나도 요청을 죽이지 않음
    try {
      String token = CookieUtils.find(request, "token");
      if (token != null && !jwtUtil.isExpired(token)) {
        Long id = this.jwtUtil.getId(token);
        String role = this.jwtUtil.getRole(token);

        AuthUserPrincipal principal = new AuthUserPrincipal(id, role);
        SecurityContextHolder.getContext()
            .setAuthentication(new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
            ));

        String newToken = jwtUtil.create(principal.getId(), jwtUtil.getUsername(token), principal.getAuthorities());

        // 새로운 토큰을 쿠키에 설정 (기존 토큰 덮어쓰기)

        Cookie cookie = CookieUtils.create("token", newToken);
        response.addCookie(cookie);
      }
    } catch (Exception e) {
      // ★ 핵심: 토큰 파싱 중에 에러가 나도(만료, 위조 등)
      // 그냥 로그만 찍고(선택사항) '넘어갑니다'.
      // 그러면 SecurityContext가 비어있는 상태로 다음으로 넘어가고,
      // permitAll 페이지면 통과, 아니면 403이 뜹니다.
      request.setAttribute("exception", e); // 필요 시 에러 저장
    }

    // 3. 무조건 다음 필터로 진행
    filterChain.doFilter(request, response);
  }

//  private static boolean isPermitRequest(String path) {
////    return path.startsWith("/api/auth/login") || path.startsWith("/api/logout") ||
////        path.startsWith("/api/users/signup");
//    return path.startsWith("/api/auth/login") || path.startsWith("/api/logout") ||
//        path.startsWith("/api/users/signup") || path.startsWith("/api/products");
//  }

  @RequiredArgsConstructor
  static public class AuthUserPrincipal {

    private final Long id;
    private final String role;

    public Long getId() {
      return id;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
      return List.of(new SimpleGrantedAuthority(role));
    }
  }
}
