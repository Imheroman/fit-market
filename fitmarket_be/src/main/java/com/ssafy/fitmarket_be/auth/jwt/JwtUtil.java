package com.ssafy.fitmarket_be.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
  private static final long EXPIRATION_TIME = 1000 * 60 * 30; // 30분
  private static final String USER_NAME = "username";
  private static final String ROLE = "role";

  private final SecretKey key;

  public JwtUtil(@Value("${jwt.secret}") String secret) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes());
  }

  public String create(String username, Collection<? extends GrantedAuthority> authorities) {
    String role = authorities.stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.joining(","));

    return Jwts.builder()
        .header().add("typ", "JWT") // 헤더: 타입 지정 (필수적이진 않음)
        .and()
        .claim(USER_NAME, username) // email
        .claim(ROLE, role)         // role
        .issuedAt(new Date(System.currentTimeMillis())) // 발행 시간
        .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 만료 시간
        .signWith(key) // signature
        .compact();
  }

  /**
   * 토큰에서 username 추출
   *
   * @param token
   * @return
   */
  public String getUsername(String token) {
    return parseClaims(token).get(USER_NAME, String.class);
  }

  /**
   * 토큰에서 user role 추출
   *
   * @param token
   * @return
   */
  public String getRole(String token) {
    return parseClaims(token).get(ROLE, String.class);
  }

  private List<SimpleGrantedAuthority> getAuthorities(String token) {
    final String role = getRole(token);
    return List.of(new SimpleGrantedAuthority(role));
  }

  /**
   * 토큰 만료 시간 확인
   *
   * @param token
   * @return
   */
  public Boolean isExpired(String token) {
    return parseClaims(token).getExpiration().before(new Date());
  }

  /**
   * 토큰 내부 정보 (payload) 추출
   *
   * @param token jwt token
   * @return parse 중 발생한 에러
   */
  private Claims parseClaims(String token) {
    try {
      return Jwts.parser()
          .verifyWith(key) // 서명 검증
          .build()
          .parseSignedClaims(token)
          .getPayload();
    } catch (ExpiredJwtException e) {
      throw e; // 만료된 토큰은 호출한 곳에서 처리 (Refresh Token 로직 등)
    } catch (MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
      throw new RuntimeException("유효하지 않은 토큰입니다.");
    }
  }

  public Authentication getAuthentication(final String token) {
    Claims claims = parseClaims(token);
    List<SimpleGrantedAuthority> authorities = getAuthorities(token);

    final User principal = new User(claims.getSubject(), "", authorities); // Security User
    return new UsernamePasswordAuthenticationToken(principal, token, authorities);
  }
}
