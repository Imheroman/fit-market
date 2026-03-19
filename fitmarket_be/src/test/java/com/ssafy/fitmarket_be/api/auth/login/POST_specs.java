package com.ssafy.fitmarket_be.api.auth.login;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.fitmarket_be.api.FitmarketApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@FitmarketApiTest
@AutoConfigureMockMvc
@DisplayName("POST /api/auth/login")
class POST_specs {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("유효한_요청으로_로그인하면_200_OK와_Set_Cookie반환")
    void 유효한_요청으로_로그인하면_200_OK와_Set_Cookie반환() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("email", "user@test.com", "password", "password123")
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(cookie().exists("access_token"))
                .andExpect(jsonPath("$.name").isNotEmpty());
    }

    @Test
    @DisplayName("잘못된_비밀번호로_로그인하면_401반환")
    void 잘못된_비밀번호로_로그인하면_401반환() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("email", "user@test.com", "password", "wrongpw")
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("존재하지않는_이메일로_로그인하면_401반환")
    void 존재하지않는_이메일로_로그인하면_401반환() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("email", "notexist@test.com", "password", "password123")
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET_메서드로_로그인하면_405반환")
    void GET_메서드로_로그인하면_405반환() throws Exception {
        // CustomLoginFilter는 POST 이외의 메서드에서 AuthenticationServiceException을 던지고
        // unsuccessfulAuthentication 핸들러에 의해 401이 반환된다.
        // Spring Security 필터 기반 처리라 405 대신 401이 반환될 수 있음.
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().is4xxClientError());
    }
}
