package com.ssafy.fitmarket_be.unit.auth;

import com.ssafy.fitmarket_be.auth.service.TokenBlacklistService;
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
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenBlacklistService")
class TokenBlacklistServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private TokenBlacklistService tokenBlacklistService;

    private static final String BL_PREFIX = "BL:";

    @Test
    @DisplayName("addToBlacklist - 토큰을 남은 TTL과 함께 Redis에 저장한다")
    void add_정상등록_Redis에TTL과함께저장() {
        // given
        String jti = "abc-123-def";
        long remainingMillis = 300000L; // 5분
        String expectedKey = BL_PREFIX + jti;

        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // when
        tokenBlacklistService.add(jti, remainingMillis);

        // then
        then(valueOperations).should().set(expectedKey, "revoked", remainingMillis, TimeUnit.MILLISECONDS);
    }

    @Test
    @DisplayName("addToBlacklist - remainingMillis가 0 이하이면 Redis에 저장하지 않는다")
    void add_만료시간이0이하_Redis저장안함() {
        // given
        String jti = "abc-123-def";

        // when
        tokenBlacklistService.add(jti, 0L);

        // then
        then(redisTemplate).should(never()).opsForValue();
    }

    @Test
    @DisplayName("isBlacklisted true - 블랙리스트에 등록된 토큰이면 true를 반환한다")
    void isBlacklisted_등록된토큰_true반환() {
        // given
        String jti = "blacklisted-jti";

        given(redisTemplate.hasKey(BL_PREFIX + jti)).willReturn(Boolean.TRUE);

        // when
        boolean result = tokenBlacklistService.isBlacklisted(jti);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isBlacklisted false - 블랙리스트에 없는 토큰이면 false를 반환한다")
    void isBlacklisted_미등록토큰_false반환() {
        // given
        String jti = "valid-jti";

        given(redisTemplate.hasKey(BL_PREFIX + jti)).willReturn(Boolean.FALSE);

        // when
        boolean result = tokenBlacklistService.isBlacklisted(jti);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isBlacklisted - hasKey가 null을 반환하면 false를 반환한다")
    void isBlacklisted_hasKey가null반환_false반환() {
        // given
        String jti = "unknown-jti";

        given(redisTemplate.hasKey(BL_PREFIX + jti)).willReturn(null);

        // when
        boolean result = tokenBlacklistService.isBlacklisted(jti);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Redis 장애 시 - isBlacklisted는 fail-close로 true를 반환한다")
    void isBlacklisted_Redis장애_failClose_true반환() {
        // given
        String jti = "any-jti";

        given(redisTemplate.hasKey(BL_PREFIX + jti))
                .willThrow(new RedisConnectionFailureException("Connection refused"));

        // when
        boolean result = tokenBlacklistService.isBlacklisted(jti);

        // then: fail-close — Redis 장애 시 모든 토큰을 블랙리스트 처리
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Redis 장애 시 - add는 RedisConnectionFailureException을 잡고 예외를 전파하지 않는다")
    void add_Redis장애_예외전파안함() {
        // given
        String jti = "abc-123-def";
        long remainingMillis = 300000L;

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        willThrow(new RedisConnectionFailureException("Connection refused"))
                .given(valueOperations).set(BL_PREFIX + jti, "revoked", remainingMillis, TimeUnit.MILLISECONDS);

        // when — 예외가 전파되지 않아야 한다
        tokenBlacklistService.add(jti, remainingMillis);

        // then: 메서드가 정상적으로 종료됨 (예외 전파 없음)
    }
}
