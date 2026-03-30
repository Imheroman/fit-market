package com.ssafy.fitmarket_be.unit.auth;

import com.ssafy.fitmarket_be.auth.service.RedisRefreshTokenService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisRefreshTokenService")
class RedisRefreshTokenServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RedisRefreshTokenService redisRefreshTokenService;

    private static final String RT_PREFIX = "RT:";
    private static final long RT_TTL_DAYS = 7;

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("saveRefreshToken - RT를 SHA-256 해시하여 Redis에 7일 TTL로 저장한다")
    void save_정상저장_Redis에해시값과TTL저장() {
        // given
        Long userId = 1L;
        String refreshToken = "sample-refresh-token";
        String expectedKey = RT_PREFIX + userId;
        String expectedHash = sha256(refreshToken);

        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // when
        redisRefreshTokenService.save(userId, refreshToken);

        // then
        then(valueOperations).should().set(expectedKey, expectedHash, RT_TTL_DAYS, TimeUnit.DAYS);
    }

    @Test
    @DisplayName("validateRefreshToken 성공 - 유효한 토큰이면 true를 반환한다")
    void validate_유효한토큰_true반환() {
        // given
        Long userId = 1L;
        String refreshToken = "valid-refresh-token";
        String storedHash = sha256(refreshToken);

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(RT_PREFIX + userId)).willReturn(storedHash);

        // when
        boolean result = redisRefreshTokenService.validate(userId, refreshToken);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("validateRefreshToken 실패 - 잘못된 토큰이면 false를 반환한다")
    void validate_잘못된토큰_false반환() {
        // given
        Long userId = 1L;
        String storedHash = sha256("original-token");

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(RT_PREFIX + userId)).willReturn(storedHash);

        // when
        boolean result = redisRefreshTokenService.validate(userId, "wrong-token");

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("validateRefreshToken 실패 - 만료되어 Redis에 값이 없으면 false를 반환한다")
    void validate_만료된토큰_false반환() {
        // given
        Long userId = 1L;

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(RT_PREFIX + userId)).willReturn(null);

        // when
        boolean result = redisRefreshTokenService.validate(userId, "any-token");

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("deleteRefreshToken - Redis에서 RT 키를 삭제한다")
    void delete_정상삭제_Redis키삭제() {
        // given
        Long userId = 1L;
        String expectedKey = RT_PREFIX + userId;

        // when
        redisRefreshTokenService.delete(userId);

        // then
        then(redisTemplate).should().delete(expectedKey);
    }

    @Test
    @DisplayName("Redis 장애 시 - validate가 RedisConnectionFailureException을 잡고 false를 반환한다")
    void validate_Redis장애_false반환() {
        // given
        Long userId = 1L;

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(RT_PREFIX + userId))
                .willThrow(new RedisConnectionFailureException("Connection refused"));

        // when
        boolean result = redisRefreshTokenService.validate(userId, "any-token");

        // then
        assertThat(result).isFalse();
    }
}
