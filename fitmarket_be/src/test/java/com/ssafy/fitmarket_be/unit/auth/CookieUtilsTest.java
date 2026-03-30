package com.ssafy.fitmarket_be.unit.auth;

import com.ssafy.fitmarket_be.auth.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("CookieUtils")
class CookieUtilsTest {

    @Mock
    HttpServletRequest request;

    @Test
    @DisplayName("요청에 해당 이름의 쿠키가 있으면 Optional로 값을 반환한다")
    void find_쿠키있음_Optional값반환() {
        // given
        given(request.getCookies()).willReturn(new Cookie[]{new Cookie("token", "abc123")});

        // when
        Optional<String> value = CookieUtils.find(request, "token");

        // then
        assertThat(value).isPresent();
        assertThat(value.get()).isEqualTo("abc123");
    }

    @Test
    @DisplayName("쿠키 배열이 null이면 Optional.empty를 반환한다")
    void find_쿠키배열null_Optional_empty() {
        // given
        given(request.getCookies()).willReturn(null);

        // when
        Optional<String> value = CookieUtils.find(request, "token");

        // then
        assertThat(value).isEmpty();
    }

    @Test
    @DisplayName("요청에 해당 이름의 쿠키가 없으면 Optional.empty를 반환한다")
    void find_해당쿠키없음_Optional_empty() {
        // given
        given(request.getCookies()).willReturn(new Cookie[]{new Cookie("other", "val")});

        // when
        Optional<String> value = CookieUtils.find(request, "token");

        // then
        assertThat(value).isEmpty();
    }

    @Test
    @DisplayName("create로 생성한 쿠키는 HttpOnly, Secure, SameSite=Lax 설정이 포함된다")
    void create_보안속성확인() {
        // when
        ResponseCookie c = CookieUtils.create("token", "val");

        // then
        assertThat(c.isHttpOnly()).isTrue();
        assertThat(c.isSecure()).isTrue();
        assertThat(c.getSameSite()).isEqualTo("Lax");
    }

    @Test
    @DisplayName("create로 생성한 쿠키의 maxAge는 THIRTY_MINUTES(1800초)이다")
    void create_maxAge_THIRTY_MINUTES_1800초() {
        // when
        ResponseCookie c = CookieUtils.create("token", "val");

        // then
        assertThat(c.getMaxAge().getSeconds()).isEqualTo(1800);
    }

    @Test
    @DisplayName("createExpireToken으로 생성한 쿠키는 maxAge가 0이다")
    void createExpireToken_maxAge0() {
        // when
        ResponseCookie c = CookieUtils.createExpireToken();

        // then
        assertThat(c.getMaxAge().getSeconds()).isEqualTo(0);
    }
}
