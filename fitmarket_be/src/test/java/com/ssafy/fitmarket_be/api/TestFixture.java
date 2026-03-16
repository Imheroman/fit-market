package com.ssafy.fitmarket_be.api;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

public class TestFixture {

    public static final String TEST_JWT_SECRET = "dGVzdHNlY3JldGtleWZvcmpXdFRlc3RpbmdQdXJwb3NlT25seU5vdFByb2Q=";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(Base64.getDecoder().decode(TEST_JWT_SECRET));
    private static final long EXPIRATION_MS = 1000L * 60 * 30; // 30분

    public static final String VALID_USER_TOKEN = buildToken("1", "user@test.com", "ROLE_USER", EXPIRATION_MS);
    public static final String VALID_ADMIN_TOKEN = buildToken("2", "admin@test.com", "ROLE_ADMIN", EXPIRATION_MS);
    public static final String VALID_SELLER_TOKEN = buildToken("3", "seller@test.com", "ROLE_SELLER", EXPIRATION_MS);
    public static final String EXPIRED_TOKEN = buildToken("1", "user@test.com", "ROLE_USER", -1000L);

    private static String buildToken(String subject, String username, String role, long expirationMs) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .header().add("typ", "JWT").and()
                .subject(subject)
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(KEY)
                .compact();
    }

    public static Cookie userCookie() {
        return new Cookie("token", VALID_USER_TOKEN);
    }

    public static Cookie adminCookie() {
        return new Cookie("token", VALID_ADMIN_TOKEN);
    }

    public static Cookie sellerCookie() {
        return new Cookie("token", VALID_SELLER_TOKEN);
    }
}
