package com.ssafy.fitmarket_be.api.payments.success;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Alias("PaymentsSuccess_POST_specs")
@FitmarketApiTest
@AutoConfigureMockMvc
@DisplayName("POST /api/payments/success — 결제 승인 API")
class POST_specs {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("인증없이_결제승인요청하면_401반환")
    void 인증없이_결제승인요청하면_401반환() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("paymentKey", "pk", "orderId", "ord-001", "amount", 10000)
        );

        mockMvc.perform(post("/payments/success")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("결제실패_콜백_400반환")
    void 결제실패_콜백_400반환() throws Exception {
        // /payments/fail 는 인증된 사용자만 접근 가능 (security config: anyRequest().authenticated())
        mockMvc.perform(get("/payments/fail")
                        .cookie(TestFixture.userCookie())
                        .param("errorCode", "PAY_PROCESS_CANCELED")
                        .param("errorReason", "사용자취소"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("PAY_PROCESS_CANCELED"));
    }
}
