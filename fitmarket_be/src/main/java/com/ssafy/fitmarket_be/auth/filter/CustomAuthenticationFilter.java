package com.ssafy.fitmarket_be.auth.filter;

import com.ssafy.fitmarket_be.auth.CookieUtils;
import com.ssafy.fitmarket_be.auth.jwt.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    if (isPermitRequest(request.getRequestURI())) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = CookieUtils.find(request, "token");

    if (!(token == null || this.jwtUtil.isExpired(token))) {
      Long id = this.jwtUtil.getId(token);
      String role = this.jwtUtil.getRole(token);

      AuthUserPrincipal principal = new AuthUserPrincipal(id, role);
      SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
          principal, null, principal.getAuthorities()
      ));
    }

    filterChain.doFilter(request, response);
  }

  private static boolean isPermitRequest(String path) {
    return path.startsWith("/api/auth/login") || path.startsWith("/api/logout") ||
        path.startsWith("/api/users/signup");
  }

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
