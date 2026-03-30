package com.ssafy.fitmarket_be.auth.controller;

import com.ssafy.fitmarket_be.auth.CookieUtils;
import com.ssafy.fitmarket_be.auth.jwt.JwtUtil;
import com.ssafy.fitmarket_be.auth.service.RedisRefreshTokenService;
import com.ssafy.fitmarket_be.global.common.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final JwtUtil jwtUtil;
    private final RedisRefreshTokenService refreshTokenService;

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Void>> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("UNAUTHORIZED", "인증이 필요합니다."));
        }

        try {
            if (jwtUtil.isExpired(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("TOKEN_EXPIRED", "토큰이 만료되었습니다."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("INVALID_TOKEN", "유효하지 않은 토큰입니다."));
        }

        if (!"refresh".equals(jwtUtil.getTokenType(refreshToken))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("INVALID_TOKEN_TYPE", "잘못된 토큰 유형입니다."));
        }

        Long userId = jwtUtil.getId(refreshToken);
        if (!refreshTokenService.validate(userId, refreshToken)) {
            refreshTokenService.delete(userId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("TOKEN_REVOKED", "토큰이 무효화되었습니다."));
        }

        // RT에서 정보 추출하여 새 AT + RT 생성 (DB 조회 불필요)
        String username = jwtUtil.getUsername(refreshToken);
        String role = jwtUtil.getRole(refreshToken);
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

        String newAccessToken = jwtUtil.createAccessToken(userId, username, authorities);
        String newRefreshToken = jwtUtil.createRefreshToken(userId, username, authorities);

        refreshTokenService.save(userId, newRefreshToken);

        CookieUtils.addCookie(response, CookieUtils.createAccessTokenCookie(newAccessToken));
        CookieUtils.addCookie(response, CookieUtils.createRefreshTokenCookie(newRefreshToken));

        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
