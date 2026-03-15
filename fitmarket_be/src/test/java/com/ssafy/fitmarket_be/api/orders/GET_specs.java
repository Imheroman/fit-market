package com.ssafy.fitmarket_be.api.orders;

import com.ssafy.fitmarket_be.api.FitmarketApiTest;
import com.ssafy.fitmarket_be.api.TestFixture;
import org.apache.ibatis.type.Alias;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Alias("Orders_GET_specs")
@FitmarketApiTest
@AutoConfigureMockMvc
@DisplayName("GET /api/orders — 주문 목록 조회")
class GET_specs {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("인증없이_주문목록조회하면_401반환")
    void 인증없이_주문목록조회하면_401반환() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("인증된_사용자_주문목록조회_200반환")
    void 인증된_사용자_주문목록조회_200반환() throws Exception {
        mockMvc.perform(get("/api/orders")
                        .cookie(TestFixture.userCookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
