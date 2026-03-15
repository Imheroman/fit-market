package com.ssafy.fitmarket_be.api.orders.orderNumber.refund;

import com.ssafy.fitmarket_be.api.FitmarketApiTest;
import com.ssafy.fitmarket_be.api.TestFixture;
import org.apache.ibatis.type.Alias;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Alias("OrderCancel_POST_specs")
@FitmarketApiTest
@AutoConfigureMockMvc
@DisplayName("POST /api/orders/{orderNumber}/cancel — 주문 취소 및 삭제")
class POST_specs {

    @Autowired
    MockMvc mockMvc;

    @Test
    @Disabled("updateApprovalStatus SQL이 'UPDATE ... JOIN' MySQL 전용 문법을 사용하여 H2에서 실행 불가")
    @DisplayName("주문취소_성공_204반환")
    void 주문취소_성공_204반환() throws Exception {
        // test-data.sql: ORD-001 은 userId=1 소유, PENDING_APPROVAL 상태
        mockMvc.perform(post("/orders/ORD-001/cancel")
                        .cookie(TestFixture.userCookie()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DirtiesContext
    @DisplayName("주문삭제_성공_204반환")
    void 주문삭제_성공_204반환() throws Exception {
        // test-data.sql: ORD-003 은 userId=1 소유 (softDelete 전용)
        mockMvc.perform(delete("/orders/ORD-003")
                        .cookie(TestFixture.userCookie()))
                .andExpect(status().isNoContent());
    }
}
