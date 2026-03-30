package com.ssafy.fitmarket_be.unit.auth;

import com.ssafy.fitmarket_be.auth.dto.CustomUserDetails;
import com.ssafy.fitmarket_be.auth.service.CustomUserDetailsService;
import com.ssafy.fitmarket_be.entity.User;
import com.ssafy.fitmarket_be.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService")
class CustomUserDetailsServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("loadUserByUsername: 유효한 이메일이면 올바른 UserDetails를 반환한다")
    void loadUserByUsername_성공() {
        // given
        User user = User.builder()
            .id(1L)
            .name("홍길동")
            .email("user@test.com")
            .password("encodedPassword")
            .role("USER")
            .build();

        given(userRepository.findByEmail("user@test.com")).willReturn(Optional.of(user));

        // when
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("user@test.com");

        // then
        assertThat(userDetails).isInstanceOf(CustomUserDetails.class);
        assertThat(userDetails.getUsername()).isEqualTo("user@test.com");
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword");
        assertThat(userDetails.getAuthorities())
            .extracting("authority")
            .containsExactly("ROLE_USER");

        CustomUserDetails customDetails = (CustomUserDetails) userDetails;
        assertThat(customDetails.getId()).isEqualTo(1L);
        assertThat(customDetails.getName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("loadUserByUsername: 존재하지 않는 이메일이면 UsernameNotFoundException을 던진다")
    void loadUserByUsername_존재하지않는이메일() {
        // given
        given(userRepository.findByEmail("unknown@test.com")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("unknown@test.com"))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessageContaining("존재하지 않는 유저");
    }

    @Test
    @DisplayName("loadUserByUsername: 탈퇴한 회원이면 RuntimeException을 던진다")
    void loadUserByUsername_탈퇴회원() {
        // given
        User deletedUser = User.builder()
            .id(2L)
            .name("탈퇴유저")
            .email("deleted@test.com")
            .password("encodedPassword")
            .role("USER")
            .deletedDate(LocalDateTime.of(2026, 3, 1, 0, 0))
            .build();

        given(userRepository.findByEmail("deleted@test.com")).willReturn(Optional.of(deletedUser));

        // when & then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("deleted@test.com"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("탈퇴한 회원입니다.");
    }

    @Test
    @DisplayName("loadUserByUsername: SELLER 역할이면 ROLE_SELLER 권한을 반환한다")
    void loadUserByUsername_SELLER역할_권한확인() {
        // given
        User seller = User.builder()
            .id(3L)
            .name("판매자")
            .email("seller@test.com")
            .password("encodedPassword")
            .role("SELLER")
            .build();

        given(userRepository.findByEmail("seller@test.com")).willReturn(Optional.of(seller));

        // when
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("seller@test.com");

        // then
        assertThat(userDetails.getAuthorities())
            .extracting("authority")
            .containsExactly("ROLE_SELLER");
    }
}
