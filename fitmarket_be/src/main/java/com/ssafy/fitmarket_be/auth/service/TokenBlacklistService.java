package com.ssafy.fitmarket_be.auth.service;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {
    private final StringRedisTemplate redisTemplate;
    private static final String BL_PREFIX = "BL:";

    public void add(String jti, long remainingMillis) {
        if (remainingMillis <= 0) return;
        try {
            redisTemplate.opsForValue().set(
                BL_PREFIX + jti,
                "revoked",
                remainingMillis,
                TimeUnit.MILLISECONDS
            );
        } catch (RedisConnectionFailureException e) {
            log.error("Failed to blacklist token jti={}", jti, e);
        }
    }

    public boolean isBlacklisted(String jti) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(BL_PREFIX + jti));
        } catch (RedisConnectionFailureException e) {
            log.error("Redis unavailable for blacklist check — fail-close: rejecting token jti={}", jti, e);
            return true;  // fail-close: Redis 장애 시 모든 토큰 거부
        }
    }
}
