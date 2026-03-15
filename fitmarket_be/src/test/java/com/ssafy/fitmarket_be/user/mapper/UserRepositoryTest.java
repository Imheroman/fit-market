package com.ssafy.fitmarket_be.user.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssafy.fitmarket_be.user.entity.User;
import com.ssafy.fitmarket_be.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@MybatisTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Rollback
@DisplayName("UserRepository — SQL 정합성")
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("findByEmail_존재하는이메일_User반환")
    void findByEmail_존재하는이메일_User반환() {
        Optional<User> result = userRepository.findByEmail("user@test.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("user@test.com");
    }

    @Test
    @DisplayName("findByEmail_없는이메일_Optional빈값")
    void findByEmail_없는이메일_Optional빈값() {
        Optional<User> result = userRepository.findByEmail("none@test.com");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("updateName_성공_반영확인")
    void updateName_성공_반영확인() {
        int affected = userRepository.updateName(1L, "변경이름");

        assertThat(affected).isEqualTo(1);
        Optional<User> found = userRepository.findBy(1L);
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("변경이름");
    }

    @Test
    @DisplayName("updatePhone_성공_반영확인")
    void updatePhone_성공_반영확인() {
        int affected = userRepository.updatePhone(1L, "01099998888");

        assertThat(affected).isEqualTo(1);
        Optional<User> found = userRepository.findBy(1L);
        assertThat(found).isPresent();
        assertThat(found.get().getPhone()).isEqualTo("01099998888");
    }

    @Test
    @DisplayName("updatePassword_성공_반영확인")
    void updatePassword_성공_반영확인() {
        int affected = userRepository.updatePassword(1L, "newEncodedPw");

        assertThat(affected).isEqualTo(1);
        Optional<User> found = userRepository.findBy(1L);
        assertThat(found).isPresent();
        assertThat(found.get().getPassword()).isEqualTo("newEncodedPw");
    }
}
