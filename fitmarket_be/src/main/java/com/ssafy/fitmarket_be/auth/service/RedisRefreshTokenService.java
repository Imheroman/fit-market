package com.ssafy.fitmarket_be.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisRefreshTokenService {
    private final StringRedisTemplate redisTemplate;
    private static final String RT_PREFIX = "RT:";
    private static final long RT_TTL_DAYS = 7;

    public void save(Long userId, String refreshToken) {
        String key = RT_PREFIX + userId;
        String hash = sha256(refreshToken);
        redisTemplate.opsForValue().set(key, hash, RT_TTL_DAYS, TimeUnit.DAYS);
    }

    public boolean validate(Long userId, String refreshToken) {
        try {
            String stored = redisTemplate.opsForValue().get(RT_PREFIX + userId);
            if (stored == null) return false;
            return stored.equals(sha256(refreshToken));
        } catch (RedisConnectionFailureException e) {
            log.error("Redis unavailable for RT validation", e);
            return false;
        }
    }

    public void delete(Long userId) {
        redisTemplate.delete(RT_PREFIX + userId);
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
