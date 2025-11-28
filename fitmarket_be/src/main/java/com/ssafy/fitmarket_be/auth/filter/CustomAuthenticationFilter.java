package com.ssafy.fitmarket_be.auth.filter;

import com.ssafy.fitmarket_be.auth.CookieUtils;
import com.ssafy.fitmarket_be.auth.jwt.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    System.out.println("========================================");
    System.out.println("Method: " + request.getMethod()); // GET, POST, OPTIONS 등
    System.out.println("URI   : " + request.getRequestURI()); // /api/login
    System.out.println("========================================");

    if (isPermitRequest(request.getRequestURI())) {
      System.out.println("if -> pass");
      filterChain.doFilter(request, response);
      return;
    }

    System.out.println("else");
    String token = CookieUtils.find(request, "token");

    if (!(token == null || this.jwtUtil.isExpired(token))) {
      String username = this.jwtUtil.getUsername(token);
      String role = this.jwtUtil.getRole(token);

      User user = new User(username, "", Collections.singleton(new SimpleGrantedAuthority(role)));
      Authentication auth = new UsernamePasswordAuthenticationToken(user, null,
          user.getAuthorities());

      SecurityContextHolder.getContext().setAuthentication(auth);
    }

    filterChain.doFilter(request, response);
  }

  private static boolean isPermitRequest(String path) {
    return path.startsWith("/api/login") || path.startsWith("/api/logout");
  }
}
