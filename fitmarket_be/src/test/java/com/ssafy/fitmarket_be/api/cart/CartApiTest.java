package com.ssafy.fitmarket_be.api.cart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.fitmarket_be.api.FitmarketApiTest;
import com.ssafy.fitmarket_be.api.TestFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@FitmarketApiTest
@AutoConfigureMockMvc
@DisplayName("Cart API 통합 테스트")
class CartApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("인증없이_장바구니조회하면_401반환")
    void 인증없이_장바구니조회하면_401반환() throws Exception {
        mockMvc.perform(get("/cart"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    @DisplayName("상품담기_성공")
    void 상품담기_성공() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("quantity", 2));

        mockMvc.perform(post("/cart/1")
                        .cookie(TestFixture.userCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("수량0으로_상품담기하면_400반환")
    void 수량0으로_상품담기하면_400반환() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("quantity", 0));

        mockMvc.perform(post("/cart/1")
                        .cookie(TestFixture.userCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    @DisplayName("장바구니상품_삭제성공")
    void 장바구니상품_삭제성공() throws Exception {
        mockMvc.perform(delete("/cart/1")
                        .cookie(TestFixture.userCookie()))
                .andExpect(status().isOk());
    }
}
