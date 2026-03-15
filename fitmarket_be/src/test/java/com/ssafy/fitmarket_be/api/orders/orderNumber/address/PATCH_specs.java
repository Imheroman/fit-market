package com.ssafy.fitmarket_be.api.orders.orderNumber.address;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.fitmarket_be.api.FitmarketApiTest;
import com.ssafy.fitmarket_be.api.TestFixture;
import org.apache.ibatis.type.Alias;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Alias("OrderAddress_PATCH_specs")
@FitmarketApiTest
@AutoConfigureMockMvc
@DisplayName("PATCH /api/orders/{orderNumber}/address — 배송지 수정")
class PATCH_specs {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("배송지수정_배송중_400반환")
    void 배송지수정_배송중_400반환() throws Exception {
        // test-data.sql: ORD-002 는 userId=1 소유, SHIPPING 상태 → 배송지 수정 불가
        String body = objectMapper.writeValueAsString(Map.of("addressId", 1));

        mockMvc.perform(patch("/orders/ORD-002/address")
                        .cookie(TestFixture.userCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
