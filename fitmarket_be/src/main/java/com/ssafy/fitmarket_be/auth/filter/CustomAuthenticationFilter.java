package com.ssafy.fitmarket_be.auth.filter;

import com.ssafy.fitmarket_be.auth.CookieUtils;
import com.ssafy.fitmarket_be.auth.jwt.JwtUtil;
import com.ssafy.fitmarket_be.auth.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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
  private final TokenBlacklistService blacklistService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    try {
      Optional<String> tokenOpt = CookieUtils.find(request, "access_token");
      if (tokenOpt.isPresent()) {
        String token = tokenOpt.get();
        if (!jwtUtil.isExpired(token) && "access".equals(jwtUtil.getTokenType(token))) {
          String jti = jwtUtil.getJti(token);
          if (!blacklistService.isBlacklisted(jti)) {
            Long id = jwtUtil.getId(token);
            String role = jwtUtil.getRole(token);

            AuthUserPrincipal principal = new AuthUserPrincipal(id, role);
            SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(
                    principal, null, principal.getAuthorities()
                ));
          }
        }
      }
    } catch (Exception e) {
      SecurityContextHolder.clearContext();
      request.setAttribute("exception", e);
    }

    filterChain.doFilter(request, response);
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
