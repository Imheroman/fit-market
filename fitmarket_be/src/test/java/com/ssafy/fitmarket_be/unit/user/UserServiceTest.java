package com.ssafy.fitmarket_be.unit.user;

import com.ssafy.fitmarket_be.entity.User;
import com.ssafy.fitmarket_be.user.dto.UserPasswordUpdateRequestDto;
import com.ssafy.fitmarket_be.user.dto.UserSignupRequestDto;
import com.ssafy.fitmarket_be.user.dto.UserUpdateResponseDto;
import com.ssafy.fitmarket_be.user.mapper.UserMapper;
import com.ssafy.fitmarket_be.user.repository.UserRepository;
import com.ssafy.fitmarket_be.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService")
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    UserMapper userMapper;

    @InjectMocks
    UserService userService;

    // ===== signup() =====

    @Test
    @DisplayName("signup: 정상 가입 시 save와 encode가 호출된다")
    void signup_정상_save호출() {
        // given
        UserSignupRequestDto request = new UserSignupRequestDto();
        request.setName("홍길동");
        request.setEmail("new@test.com");
        request.setPassword("Password1!");
        request.setPhone("01012345678");

        given(userRepository.findByEmail("new@test.com")).willReturn(Optional.empty());
        given(passwordEncoder.encode(any())).willReturn("encodedPw");
        given(userRepository.save(any(User.class))).willReturn(1);

        // when
        userService.signup(request);

        // then
        verify(userRepository).save(any());
        verify(passwordEncoder).encode(any());
    }

    @Test
    @DisplayName("signup: 중복 이메일이면 IllegalArgumentException을 던진다")
    void signup_중복이메일_IllegalArgumentException() {
        // given
        UserSignupRequestDto request = new UserSignupRequestDto();
        request.setName("중복유저");
        request.setEmail("dup@test.com");
        request.setPassword("Password1!");
        request.setPhone("01012345678");

        given(userRepository.findByEmail("dup@test.com")).willReturn(Optional.of(User.builder().build()));

        // when & then
        assertThatThrownBy(() -> userService.signup(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("이미 존재하는 회원입니다.");
    }

    @Test
    @DisplayName("signup: save 실패 시 RuntimeException을 던진다")
    void signup_save실패_RuntimeException() {
        // given
        UserSignupRequestDto request = new UserSignupRequestDto();
        request.setName("홍길동");
        request.setEmail("new@test.com");
        request.setPassword("Password1!");
        request.setPhone("01012345678");

        given(userRepository.findByEmail("new@test.com")).willReturn(Optional.empty());
        given(passwordEncoder.encode(any())).willReturn("encodedPw");
        given(userRepository.save(any(User.class))).willReturn(0);

        // when & then
        assertThatThrownBy(() -> userService.signup(request))
            .isInstanceOf(RuntimeException.class);
    }

    // ===== findById() =====

    @Test
    @DisplayName("findById: 존재하지 않는 ID이면 RuntimeException을 던진다")
    void findById_존재하지않는ID_RuntimeException() {
        // given
        given(userRepository.findBy(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.findById(999L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("존재하지 않는 유저입니다.");
    }

    // ===== delete() =====

    @Test
    @DisplayName("delete: 정상 삭제 시 예외가 발생하지 않는다")
    void delete_성공() {
        // given
        given(userRepository.delete(1L)).willReturn(1);

        // when & then (예외 없이 정상 종료)
        userService.delete(1L);

        verify(userRepository).delete(1L);
    }

    @Test
    @DisplayName("delete: 삭제 실패 시 RuntimeException을 던진다")
    void delete_실패_RuntimeException() {
        // given
        given(userRepository.delete(1L)).willReturn(0);

        // when & then
        assertThatThrownBy(() -> userService.delete(1L))
            .isInstanceOf(RuntimeException.class);
    }

    // ===== updatePassword() =====

    @Test
    @DisplayName("updatePassword: 현재 비밀번호 불일치 시 IllegalArgumentException을 던진다")
    void updatePassword_현재비번불일치_IllegalArgumentException() {
        // given
        String encodedPw = "encodedCurrentPw";
        User user = User.builder().id(1L).password(encodedPw).build();
        given(userRepository.findBy(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrong", encodedPw)).willReturn(false);

        UserPasswordUpdateRequestDto request = new UserPasswordUpdateRequestDto();
        request.setCurrentPassword("wrong");
        request.setNewPassword("NewPassword1!");

        // when & then
        assertThatThrownBy(() -> userService.updatePassword(1L, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("현재 비밀번호가 일치하지 않아요.");
    }

    @Test
    @DisplayName("updatePassword: 새 비밀번호가 현재 비밀번호와 동일하면 IllegalArgumentException을 던진다")
    void updatePassword_새비번동일_IllegalArgumentException() {
        // given
        String encodedPw = "encodedCurrentPw";
        String newPw = "SamePassword1!";
        User user = User.builder().id(1L).password(encodedPw).build();
        given(userRepository.findBy(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(newPw, encodedPw)).willReturn(true, true);

        UserPasswordUpdateRequestDto request = new UserPasswordUpdateRequestDto();
        request.setCurrentPassword(newPw);
        request.setNewPassword(newPw);

        // when & then
        assertThatThrownBy(() -> userService.updatePassword(1L, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("새 비밀번호는 현재 비밀번호와 달라야 해요.");
    }

    @Test
    @DisplayName("updatePassword: 정상 변경 시 updatePassword가 호출된다")
    void updatePassword_정상_updatePassword호출() {
        // given
        String encodedPw = "encodedCurrentPw";
        String currentPw = "CurrentPassword1!";
        String newPw = "NewPassword1!";
        User user = User.builder().id(1L).password(encodedPw).build();
        given(userRepository.findBy(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(currentPw, encodedPw)).willReturn(true);
        given(passwordEncoder.matches(newPw, encodedPw)).willReturn(false);
        given(passwordEncoder.encode(newPw)).willReturn("encodedNewPw");
        given(userRepository.updatePassword(eq(1L), any())).willReturn(1);

        UserPasswordUpdateRequestDto request = new UserPasswordUpdateRequestDto();
        request.setCurrentPassword(currentPw);
        request.setNewPassword(newPw);

        // when
        userService.updatePassword(1L, request);

        // then
        verify(userRepository).updatePassword(eq(1L), any());
    }

    // ===== updateName() =====

    @Test
    @DisplayName("updateName: 정상 변경 시 UserUpdateResponseDto를 반환한다")
    void updateName_성공_UserUpdateResponseDto반환() {
        // given
        given(userRepository.updateName(1L, "홍길동")).willReturn(1);

        // when
        UserUpdateResponseDto result = userService.updateName(1L, "홍길동");

        // then
        assertThat(result).isInstanceOf(UserUpdateResponseDto.class);
        assertThat(result.getProfile()).isEqualTo("홍길동");
    }

    // ===== updatePhone() =====

    @Test
    @DisplayName("updatePhone: 정상 변경 시 UserUpdateResponseDto를 반환한다")
    void updatePhone_성공_UserUpdateResponseDto반환() {
        // given
        given(userRepository.updatePhone(1L, "01011112222")).willReturn(1);

        // when
        UserUpdateResponseDto result = userService.updatePhone(1L, "01011112222");

        // then
        assertThat(result).isInstanceOf(UserUpdateResponseDto.class);
        assertThat(result.getProfile()).isEqualTo("01011112222");
    }
}
