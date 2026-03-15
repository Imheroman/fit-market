package com.ssafy.fitmarket_be.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.fitmarket_be.address.repository.AddressRepository;
import com.ssafy.fitmarket_be.cart.repository.ShoppingCartRepository;
import com.ssafy.fitmarket_be.order.domain.OrderApprovalStatus;
import com.ssafy.fitmarket_be.order.domain.OrderMode;
import com.ssafy.fitmarket_be.order.domain.OrderView;
import com.ssafy.fitmarket_be.order.dto.OrderRefundEligibilityResponse;
import com.ssafy.fitmarket_be.order.repository.OrderRepository;
import com.ssafy.fitmarket_be.payment.domain.PaymentStatus;
import com.ssafy.fitmarket_be.payment.repository.PaymentRepository;
import com.ssafy.fitmarket_be.product.repository.ProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * OrderService 단위 테스트 — 환불 가능 여부 로직 검증.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService — 환불 가능 여부")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ShoppingCartRepository shoppingCartRepository;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private PaymentRepository paymentRepository;

    private OrderService orderService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Long USER_ID = 1L;
    private static final Long ORDER_ID = 100L;
    private static final String ORDER_NUMBER = "ORD-TEST-001";

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
            orderRepository,
            shoppingCartRepository,
            productMapper,
            addressRepository,
            paymentRepository,
            objectMapper
        );
    }

    // ===== DELIVERED — 반품/교환 안내 =====

    @Test
    @DisplayName("배송 완료 주문은 환불 불가 — 반품/교환 안내 메시지를 반환한다")
    void 배송완료_주문은_환불불가_반품교환_안내_메시지를_반환한다() {
        // Arrange
        OrderView order = buildOrder(OrderApprovalStatus.DELIVERED, PaymentStatus.PAID);
        given(orderRepository.findOrderByNumberAndUserId(ORDER_NUMBER, USER_ID))
            .willReturn(Optional.of(order));
        given(orderRepository.countOrderReturnExchanges(ORDER_ID)).willReturn(0);

        // Act
        OrderRefundEligibilityResponse response =
            orderService.getRefundEligibility(USER_ID, ORDER_NUMBER);

        // Assert
        assertThat(response.eligible()).isFalse();
        assertThat(response.message()).isEqualTo("배송 완료된 주문은 반품/교환을 이용해 주세요.");
    }

    // ===== SHIPPING — 배송 중 환불 불가 =====

    @Test
    @DisplayName("배송 중 주문은 환불 불가 메시지를 반환한다")
    void 배송중_주문은_환불불가_메시지를_반환한다() {
        // Arrange
        OrderView order = buildOrder(OrderApprovalStatus.SHIPPING, PaymentStatus.PAID);
        given(orderRepository.findOrderByNumberAndUserId(ORDER_NUMBER, USER_ID))
            .willReturn(Optional.of(order));
        given(orderRepository.countOrderReturnExchanges(ORDER_ID)).willReturn(0);

        // Act
        OrderRefundEligibilityResponse response =
            orderService.getRefundEligibility(USER_ID, ORDER_NUMBER);

        // Assert
        assertThat(response.eligible()).isFalse();
        assertThat(response.message()).isEqualTo("배송이 시작된 주문은 환불할 수 없어요.");
    }

    // ===== CANCELLED / REJECTED — 종료 주문 =====

    @Test
    @DisplayName("취소된 주문은 이미 종료된 주문 메시지를 반환한다")
    void 취소된_주문은_이미_종료된_주문_메시지를_반환한다() {
        // Arrange
        OrderView order = buildOrder(OrderApprovalStatus.CANCELLED, PaymentStatus.PENDING);
        given(orderRepository.findOrderByNumberAndUserId(ORDER_NUMBER, USER_ID))
            .willReturn(Optional.of(order));
        given(orderRepository.countOrderReturnExchanges(ORDER_ID)).willReturn(0);

        // Act
        OrderRefundEligibilityResponse response =
            orderService.getRefundEligibility(USER_ID, ORDER_NUMBER);

        // Assert
        assertThat(response.eligible()).isFalse();
        assertThat(response.message()).isEqualTo("이미 종료된 주문이라 환불할 수 없어요.");
    }

    @Test
    @DisplayName("거절된 주문은 이미 종료된 주문 메시지를 반환한다")
    void 거절된_주문은_이미_종료된_주문_메시지를_반환한다() {
        // Arrange
        OrderView order = buildOrder(OrderApprovalStatus.REJECTED, PaymentStatus.PENDING);
        given(orderRepository.findOrderByNumberAndUserId(ORDER_NUMBER, USER_ID))
            .willReturn(Optional.of(order));
        given(orderRepository.countOrderReturnExchanges(ORDER_ID)).willReturn(0);

        // Act
        OrderRefundEligibilityResponse response =
            orderService.getRefundEligibility(USER_ID, ORDER_NUMBER);

        // Assert
        assertThat(response.eligible()).isFalse();
        assertThat(response.message()).isEqualTo("이미 종료된 주문이라 환불할 수 없어요.");
    }

    // ===== APPROVED + PAID — 환불 가능 기간 검사 =====

    @Test
    @DisplayName("결제 후 3일 이내 승인된 주문은 환불이 가능하다")
    void 결제후_3일_이내_승인된_주문은_환불이_가능하다() {
        // Arrange
        OrderView order = buildOrder(OrderApprovalStatus.APPROVED, PaymentStatus.PAID);
        given(orderRepository.findOrderByNumberAndUserId(ORDER_NUMBER, USER_ID))
            .willReturn(Optional.of(order));
        given(orderRepository.countOrderReturnExchanges(ORDER_ID)).willReturn(0);
        given(paymentRepository.findApprovedAtByOrderId(ORDER_ID))
            .willReturn(Optional.of(LocalDateTime.now().minusDays(1)));  // 1일 전 결제

        // Act
        OrderRefundEligibilityResponse response =
            orderService.getRefundEligibility(USER_ID, ORDER_NUMBER);

        // Assert
        assertThat(response.eligible()).isTrue();
        assertThat(response.message()).isEqualTo("환불이 가능해요.");
    }

    @Test
    @DisplayName("결제 후 3일이 지난 주문은 환불 기간 초과 메시지를 반환한다")
    void 결제후_3일이_지난_주문은_환불기간_초과_메시지를_반환한다() {
        // Arrange
        OrderView order = buildOrder(OrderApprovalStatus.APPROVED, PaymentStatus.PAID);
        given(orderRepository.findOrderByNumberAndUserId(ORDER_NUMBER, USER_ID))
            .willReturn(Optional.of(order));
        given(orderRepository.countOrderReturnExchanges(ORDER_ID)).willReturn(0);
        given(paymentRepository.findApprovedAtByOrderId(ORDER_ID))
            .willReturn(Optional.of(LocalDateTime.now().minusDays(5)));  // 5일 전 결제

        // Act
        OrderRefundEligibilityResponse response =
            orderService.getRefundEligibility(USER_ID, ORDER_NUMBER);

        // Assert
        assertThat(response.eligible()).isFalse();
        assertThat(response.message()).isEqualTo("결제 후 3일이 지나 환불할 수 없어요.");
    }

    // ===== 이미 환불/반품/교환 요청이 있는 경우 =====

    @Test
    @DisplayName("이미 환불 요청이 있는 주문은 중복 요청 불가 메시지를 반환한다")
    void 이미_환불요청이_있는_주문은_중복요청_불가_메시지를_반환한다() {
        // Arrange
        OrderView order = buildOrder(OrderApprovalStatus.APPROVED, PaymentStatus.PAID);
        given(orderRepository.findOrderByNumberAndUserId(ORDER_NUMBER, USER_ID))
            .willReturn(Optional.of(order));
        given(orderRepository.countOrderReturnExchanges(ORDER_ID)).willReturn(1);  // 이미 요청 있음

        // Act
        OrderRefundEligibilityResponse response =
            orderService.getRefundEligibility(USER_ID, ORDER_NUMBER);

        // Assert
        assertThat(response.eligible()).isFalse();
        assertThat(response.message()).isEqualTo("이미 환불/반품/교환 요청이 접수된 주문이에요.");
    }

    // ===== 테스트 픽스처 =====

    private OrderView buildOrder(OrderApprovalStatus approvalStatus, PaymentStatus paymentStatus) {
        return OrderView.builder()
            .id(ORDER_ID)
            .orderNumber(ORDER_NUMBER)
            .orderMode(OrderMode.DIRECT)
            .approvalStatus(approvalStatus.dbValue())
            .userId(USER_ID)
            .orderDate(LocalDateTime.now().minusDays(2))
            .merchandiseAmount(10000L)
            .shippingFee(0L)
            .discountAmount(0L)
            .totalAmount(10000L)
            .paymentStatus(paymentStatus)
            .build();
    }
}
