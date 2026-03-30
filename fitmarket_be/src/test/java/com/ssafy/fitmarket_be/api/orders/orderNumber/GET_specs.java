package com.ssafy.fitmarket_be.api.orders.orderNumber;

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
@DisplayName("GET /api/orders/{orderNumber} — 주문 상세 조회")
class GET_specs {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("인증된_사용자_주문상세조회_200반환")
    void 인증된_사용자_주문상세조회_200반환() throws Exception {
        // test-data.sql: ORD-001 은 userId=1 소유, PENDING_APPROVAL 상태
        mockMvc.perform(get("/orders/ORD-001")
                        .cookie(TestFixture.userCookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").isNotEmpty());
    }
}
