package com.ssafy.fitmarket_be.unit.auth;

import com.ssafy.fitmarket_be.api.TestFixture;
import com.ssafy.fitmarket_be.auth.jwt.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtUtil")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    // TestFixture와 동일한 시크릿 키를 공유하여 중복 제거
    private static final String TEST_SECRET = TestFixture.TEST_JWT_SECRET;
    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(TEST_SECRET);
    }

    @Test
    @DisplayName("정상 토큰 생성 시 클레임이 일치한다")
    void create_정상토큰생성_클레임일치() {
        // given
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        // when
        String token = jwtUtil.create(1L, "user@test.com", authorities);

        // then
        assertThat(jwtUtil.getId(token)).isEqualTo(1L);
        assertThat(jwtUtil.getUsername(token)).isEqualTo("user@test.com");
        assertThat(jwtUtil.getRole(token)).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("방금 생성한 토큰은 만료되지 않는다")
    void isExpired_유효한토큰_false반환() {
        // given
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        String token = jwtUtil.create(1L, "user@test.com", authorities);

        // when
        Boolean expired = jwtUtil.isExpired(token);

        // then
        assertThat(expired).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰은 ExpiredJwtException을 던진다")
    void isExpired_만료토큰_ExpiredJwtException() throws Exception {
        // given: JJWT 빌더로 직접 만료 토큰 생성 (expiration을 과거 시점으로 설정)
        SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(TEST_SECRET));
        String expiredToken = Jwts.builder()
                .header().add("typ", "JWT").and()
                .subject("1")
                .claim("username", "user@test.com")
                .claim("role", "ROLE_USER")
                .issuedAt(new Date(System.currentTimeMillis() - 10000))
                .expiration(new Date(System.currentTimeMillis() - 5000))  // 이미 만료된 시각
                .signWith(key)
                .compact();

        // when / then
        assertThatThrownBy(() -> jwtUtil.isExpired(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("위조 토큰은 '유효하지 않은 토큰입니다.' 메시지가 포함된 RuntimeException을 던진다")
    void parseClaims_위조토큰_RuntimeException() {
        // when / then
        assertThatThrownBy(() -> jwtUtil.isExpired("invalid.token.value"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("유효하지 않은 토큰입니다.");
    }

    @Test
    @DisplayName("정상 토큰에서 getAuthentication은 UserDetails principal을 포함한 Authentication을 반환한다")
    void getAuthentication_정상토큰_UsernamePasswordToken() {
        // given
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        String token = jwtUtil.create(1L, "user@test.com", authorities);

        // when
        Authentication auth = jwtUtil.getAuthentication(token);

        // then
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isInstanceOf(UserDetails.class);
    }

    @Test
    @DisplayName("다중 권한은 콤마로 구분하여 role 클레임에 저장된다")
    void create_다중권한_콤마구분_반환() {
        // given
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_SELLER")
        );

        // when
        String token = jwtUtil.create(1L, "user@test.com", authorities);

        // then
        assertThat(jwtUtil.getRole(token)).isEqualTo("ROLE_USER,ROLE_SELLER");
    }
}
