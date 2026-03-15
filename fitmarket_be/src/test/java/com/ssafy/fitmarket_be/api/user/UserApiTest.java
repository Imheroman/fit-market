package com.ssafy.fitmarket_be.api.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.fitmarket_be.api.FitmarketApiTest;
import com.ssafy.fitmarket_be.api.TestFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@FitmarketApiTest
@AutoConfigureMockMvc
@DisplayName("User API 통합 테스트")
class UserApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @Transactional
    @DisplayName("올바른_요청으로_회원가입하면_200반환")
    void 올바른_요청으로_회원가입하면_200반환() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of(
                        "email", "new@test.com",
                        "password", "secure123!",
                        "name", "홍길동",
                        "phone", "01012341234"
                )
        );

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("인증없이_내정보조회하면_401반환")
    void 인증없이_내정보조회하면_401반환() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    @DisplayName("비밀번호변경_성공")
    void 비밀번호변경_성공() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of(
                        "currentPassword", "password123",
                        "newPassword", "newSecure456!"
                )
        );

        mockMvc.perform(patch("/api/users/password")
                        .cookie(TestFixture.userCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    @Transactional
    @DisplayName("인증된_사용자가_회원탈퇴하면_200반환")
    void 인증된_사용자가_회원탈퇴하면_200반환() throws Exception {
        mockMvc.perform(delete("/api/users")
                        .cookie(TestFixture.userCookie()))
                .andExpect(status().isOk());
    }
}
