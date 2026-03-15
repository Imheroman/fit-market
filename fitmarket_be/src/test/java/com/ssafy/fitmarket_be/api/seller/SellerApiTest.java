package com.ssafy.fitmarket_be.api.seller;

import com.ssafy.fitmarket_be.api.FitmarketApiTest;
import com.ssafy.fitmarket_be.api.TestFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@FitmarketApiTest
@AutoConfigureMockMvc
@DisplayName("Seller API 통합 테스트")
class SellerApiTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("ADMIN이_아닌_사용자가_판매자목록조회시_403반환")
    void ADMIN이_아닌_사용자가_판매자목록조회시_403반환() throws Exception {
        mockMvc.perform(get("/seller")
                        .cookie(TestFixture.userCookie()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN이_판매자목록조회_200반환")
    void ADMIN이_판매자목록조회_200반환() throws Exception {
        mockMvc.perform(get("/seller")
                        .cookie(TestFixture.adminCookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
