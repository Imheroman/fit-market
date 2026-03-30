package com.ssafy.fitmarket_be.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.util.*;
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
  private static final String USER_NAME = "username";
  private static final String ROLE = "role";
  private static final String TYPE = "type";

  private final SecretKey key;
  private final long accessExpirationTime;
  private final long refreshExpirationTime;

  public JwtUtil(
      @Value("${jwt.secret}") String secret,
      @Value("${jwt.access-expiration-time}") long accessExpirationTime,
      @Value("${jwt.refresh-expiration-time}") long refreshExpirationTime
  ) {
    this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
    this.accessExpirationTime = accessExpirationTime;
    this.refreshExpirationTime = refreshExpirationTime;
  }

  public String createAccessToken(Long id, String username, Collection<? extends GrantedAuthority> authorities) {
    String role = authorities.stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.joining(","));

    return Jwts.builder()
        .header().add("typ", "JWT").and()
        .id(UUID.randomUUID().toString())
        .subject(String.valueOf(id))
        .claim(USER_NAME, username)
        .claim(ROLE, role)
        .claim(TYPE, "access")
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + accessExpirationTime))
        .signWith(key)
        .compact();
  }

  public String createRefreshToken(Long id, String username, Collection<? extends GrantedAuthority> authorities) {
    String role = authorities.stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.joining(","));

    return Jwts.builder()
        .header().add("typ", "JWT").and()
        .id(UUID.randomUUID().toString())
        .subject(String.valueOf(id))
        .claim(USER_NAME, username)
        .claim(ROLE, role)
        .claim(TYPE, "refresh")
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + refreshExpirationTime))
        .signWith(key)
        .compact();
  }

  public Long getId(String token) {
    return Long.parseLong(parseClaims(token).getSubject());
  }

  public String getUsername(String token) {
    return parseClaims(token).get(USER_NAME, String.class);
  }

  public String getRole(String token) {
    return parseClaims(token).get(ROLE, String.class);
  }

  public String getJti(String token) {
    return parseClaims(token).getId();
  }

  public String getTokenType(String token) {
    return parseClaims(token).get(TYPE, String.class);
  }

  public long getRemainingExpiration(String token) {
    Date expiration = parseClaims(token).getExpiration();
    return Math.max(0, expiration.getTime() - System.currentTimeMillis());
  }

  public Boolean isExpired(String token) {
    return parseClaims(token).getExpiration().before(new Date());
  }

  private List<SimpleGrantedAuthority> getAuthorities(String token) {
    return List.of(new SimpleGrantedAuthority(getRole(token)));
  }

  private Claims parseClaims(String token) {
    try {
      return Jwts.parser()
          .verifyWith(key)
          .build()
          .parseSignedClaims(token)
          .getPayload();
    } catch (ExpiredJwtException e) {
      throw e;
    } catch (MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
      throw new RuntimeException("유효하지 않은 토큰입니다.");
    }
  }

  public Authentication getAuthentication(String token) {
    Claims claims = parseClaims(token);
    List<SimpleGrantedAuthority> authorities = getAuthorities(token);
    User principal = new User(claims.getSubject(), "", authorities);
    return new UsernamePasswordAuthenticationToken(principal, token, authorities);
  }
}
