package com.ssafy.fitmarket_be.api.product;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@FitmarketApiTest
@AutoConfigureMockMvc
@DisplayName("Product API 통합 테스트")
class ProductApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("인증없이_상품목록조회_200반환")
    void 인증없이_상품목록조회_200반환() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("인증없이_상품상세조회_200반환")
    void 인증없이_상품상세조회_200반환() throws Exception {
        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("인증없이_상품등록_401반환")
    void 인증없이_상품등록_401반환() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of(
                        "name", "테스트상품등록",
                        "categoryId", 1,
                        "price", 10000,
                        "description", "테스트 상품 설명입니다",
                        "weightG", 100,
                        "stock", 10,
                        "userId", 3
                )
        );

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("인증없이_상품삭제_401반환")
    void 인증없이_상품삭제_401반환() throws Exception {
        mockMvc.perform(delete("/products/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    @DisplayName("본인_상품수정_성공")
    void 본인_상품수정_성공() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of(
                        "name", "수정된 상품명",
                        "categoryId", 1,
                        "price", 15000,
                        "description", "수정된 상품 설명입니다",
                        "weightG", 200,
                        "stock", 10,
                        "userId", 3
                )
        );

        mockMvc.perform(put("/products/1")
                        .cookie(TestFixture.sellerCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("타인_상품수정시도_403반환")
    void 타인_상품수정시도_403반환() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of(
                        "name", "수정된 상품명",
                        "categoryId", 1,
                        "price", 15000,
                        "description", "수정된 상품 설명입니다",
                        "weightG", 200,
                        "stock", 10,
                        "userId", 1
                )
        );

        mockMvc.perform(put("/products/1")
                        .cookie(TestFixture.userCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }
}
