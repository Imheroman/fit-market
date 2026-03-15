package com.ssafy.fitmarket_be.order.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssafy.fitmarket_be.order.entity.Order;
import com.ssafy.fitmarket_be.order.domain.OrderApprovalStatus;
import com.ssafy.fitmarket_be.order.domain.OrderMode;
import com.ssafy.fitmarket_be.order.domain.OrderView;
import com.ssafy.fitmarket_be.order.repository.OrderRepository;
import com.ssafy.fitmarket_be.payment.domain.PaymentStatus;
import java.util.Optional;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@MybatisTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Rollback
@DisplayName("OrderRepository — SQL 정합성")
class OrderRepositoryTest {

    @Autowired
    OrderRepository orderRepository;

    @Test
    @DisplayName("insertOrder_정상_저장확인")
    void insertOrder_정상_저장확인() {
        Order order = Order.builder()
                .orderNumber("ORD-NEW-001")
                .orderMode(OrderMode.DIRECT)
                .addressId(1L)
                .addressSnapshot("{}")
                .userId(1L)
                .merchandiseAmount(5000L)
                .shippingFee(2500L)
                .discountAmount(0L)
                .totalAmount(7500L)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        int result = orderRepository.insertOrder(order);
        assertThat(result).isEqualTo(1);

        Optional<OrderView> found = orderRepository.findOrderByNumberAndUserId("ORD-NEW-001", 1L);
        assertThat(found).isPresent();
        assertThat(found.get().getOrderNumber()).isEqualTo("ORD-NEW-001");
    }

    @Test
    @DisplayName("findOrderByNumberAndUserId_존재하는주문_반환")
    void findOrderByNumberAndUserId_존재하는주문_반환() {
        // test-data: ORD-001, userId=1
        Optional<OrderView> result = orderRepository.findOrderByNumberAndUserId("ORD-001", 1L);

        assertThat(result).isPresent();
        assertThat(result.get().getOrderNumber()).isEqualTo("ORD-001");
    }

    @Test
    @DisplayName("findOrderByNumberAndUserId_다른userId_Optional빈값")
    void findOrderByNumberAndUserId_다른userId_Optional빈값() {
        // ORD-001은 userId=1 소유 → userId=2로 조회 시 빈값
        Optional<OrderView> result = orderRepository.findOrderByNumberAndUserId("ORD-001", 2L);

        assertThat(result).isEmpty();
    }

    @Test
    @Disabled("updateApprovalStatus SQL이 'UPDATE ... JOIN' MySQL 전용 문법을 사용하여 H2에서 실행 불가")
    @DisplayName("updateApprovalStatus_성공")
    void updateApprovalStatus_성공() {
        // test-data: orderId=1, orderNumber=ORD-001, status=pending_approval
        int affected = orderRepository.updateApprovalStatus(1L, OrderApprovalStatus.CANCELLED.dbValue());

        assertThat(affected).isEqualTo(1);

        Optional<OrderView> found = orderRepository.findOrderByNumberAndUserId("ORD-001", 1L);
        assertThat(found).isPresent();
        assertThat(found.get().getApprovalStatus()).isEqualToIgnoringCase("cancelled");
    }

    @Test
    @DisplayName("softDeleteOrder_성공_deleted_at설정")
    void softDeleteOrder_성공_deleted_at설정() {
        // test-data: orderId=1, userId=1 존재
        int result = orderRepository.softDeleteOrder(1L, 1L);

        assertThat(result).isEqualTo(1);

        // 소프트 삭제 후 findOrderByNumberAndUserId는 deleted_date IS NULL 조건으로 조회 → 빈값
        Optional<OrderView> found = orderRepository.findOrderByNumberAndUserId("ORD-001", 1L);
        assertThat(found).isEmpty();
    }
}
