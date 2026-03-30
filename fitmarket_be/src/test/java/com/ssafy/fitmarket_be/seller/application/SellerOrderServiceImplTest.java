package com.ssafy.fitmarket_be.seller.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.fitmarket_be.order.domain.OrderMode;
import com.ssafy.fitmarket_be.order.domain.OrderProductEntity;
import com.ssafy.fitmarket_be.order.domain.OrderSearchPeriod;
import com.ssafy.fitmarket_be.order.domain.OrderView;
import com.ssafy.fitmarket_be.order.dto.OrderDetailResponse;
import com.ssafy.fitmarket_be.order.dto.OrderSummaryResponse;
import com.ssafy.fitmarket_be.payment.domain.PaymentStatus;
import com.ssafy.fitmarket_be.seller.api.SellerOrderService;
import com.ssafy.fitmarket_be.seller.infrastructure.mybatis.SellerOrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.ssafy.fitmarket_be.order.domain.OrderApprovalStatus;
import com.ssafy.fitmarket_be.order.dto.OrderStatusUpdateRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * SellerOrderServiceImpl 단위 테스트.
 * seller.application 패키지에 위치하여 package-private 클래스에 접근한다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SellerOrderServiceImpl")
class SellerOrderServiceImplTest {

    @Mock
    private SellerOrderMapper sellerOrderMapper;

    private SellerOrderService sellerOrderService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String ADDRESS_SNAPSHOT_JSON = """
        {
          "name": "집",
          "recipient": "홍길동",
          "phone": "01012345678",
          "postalCode": "12345",
          "addressLine": "서울시 강남구",
          "addressLineDetail": "101호",
          "memo": ""
        }
        """;

    @BeforeEach
    void setUp() {
        sellerOrderService = new SellerOrderServiceImpl(sellerOrderMapper, objectMapper);
    }

    // ===== getOrderDetail() =====

    @Test
    @DisplayName("getOrderDetail: 배송비와 할인금액을 포함해 총액을 계산한다")
    void getOrderDetail_배송비와_할인금액을_포함한_총액을_반환한다() {
        // Arrange
        Long sellerId = 1L;
        String orderNumber = "ORD-001";

        OrderView order = buildOrderView(orderNumber, 3000L, 1000L);  // shippingFee=3000, discount=1000
        OrderProductEntity item = buildItem(1L, "단백질바", 2, 5000L, 10000L); // totalPrice=10000

        given(sellerOrderMapper.findOrderByNumberAndSellerId(orderNumber, sellerId))
            .willReturn(Optional.of(order));
        given(sellerOrderMapper.findOrderProductsByOrderIdsAndSellerId(
            List.of(order.getId()), sellerId))
            .willReturn(List.of(item));

        // Act
        OrderDetailResponse response = sellerOrderService.getOrderDetail(sellerId, orderNumber);

        // Assert
        // merchandiseAmount = 10000 (items)
        // shippingFee = 3000
        // discountAmount = 1000
        // totalAmount = 10000 + 3000 - 1000 = 12000
        assertThat(response.merchandiseAmount()).isEqualTo(10000L);
        assertThat(response.shippingFee()).isEqualTo(3000L);
        assertThat(response.discountAmount()).isEqualTo(1000L);
        assertThat(response.totalAmount()).isEqualTo(12000L);
    }

    @Test
    @DisplayName("getOrderDetail: 배송비와 할인이 없으면 상품금액이 총액이다")
    void getOrderDetail_배송비와_할인이_없으면_상품금액이_총액이다() {
        // Arrange
        Long sellerId = 1L;
        String orderNumber = "ORD-002";

        OrderView order = buildOrderView(orderNumber, 0L, 0L);
        OrderProductEntity item = buildItem(1L, "닭가슴살", 1, 8000L, 8000L);

        given(sellerOrderMapper.findOrderByNumberAndSellerId(orderNumber, sellerId))
            .willReturn(Optional.of(order));
        given(sellerOrderMapper.findOrderProductsByOrderIdsAndSellerId(
            List.of(order.getId()), sellerId))
            .willReturn(List.of(item));

        // Act
        OrderDetailResponse response = sellerOrderService.getOrderDetail(sellerId, orderNumber);

        // Assert
        assertThat(response.merchandiseAmount()).isEqualTo(8000L);
        assertThat(response.shippingFee()).isEqualTo(0L);
        assertThat(response.discountAmount()).isEqualTo(0L);
        assertThat(response.totalAmount()).isEqualTo(8000L);
    }

    @Test
    @DisplayName("getOrderDetail: 존재하지 않는 주문이면 예외를 던진다")
    void getOrderDetail_존재하지_않는_주문이면_예외를_던진다() {
        // Arrange
        Long sellerId = 1L;
        String orderNumber = "NOT-EXIST";

        given(sellerOrderMapper.findOrderByNumberAndSellerId(orderNumber, sellerId))
            .willReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> sellerOrderService.getOrderDetail(sellerId, orderNumber))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("주문을 찾을 수 없어요");
    }

    // ===== getOrders() =====

    @Test
    @DisplayName("getOrders: 각 주문 요약에 배송비와 할인을 반영한다")
    void getOrders_각_주문_요약에_배송비와_할인을_반영한다() {
        // Arrange
        Long sellerId = 1L;

        OrderView order = buildOrderView("ORD-003", 2500L, 500L);
        OrderProductEntity item = buildItem(1L, "영양제", 3, 3000L, 9000L);

        given(sellerOrderMapper.findOrdersBySellerIdAndStartDate(eq(sellerId), any()))
            .willReturn(List.of(order));
        given(sellerOrderMapper.findOrderProductsByOrderIdsAndSellerId(any(), eq(sellerId)))
            .willReturn(List.of(item));

        // Act
        List<OrderSummaryResponse> result = sellerOrderService.getOrders(sellerId, OrderSearchPeriod.ALL);

        // Assert
        // merchandiseAmount = 9000, shippingFee = 2500, discount = 500
        // totalAmount = 9000 + 2500 - 500 = 11000
        assertThat(result).hasSize(1);
        OrderSummaryResponse summary = result.get(0);
        assertThat(summary.merchandiseAmount()).isEqualTo(9000L);
        assertThat(summary.shippingFee()).isEqualTo(2500L);
        assertThat(summary.discountAmount()).isEqualTo(500L);
        assertThat(summary.totalAmount()).isEqualTo(11000L);
    }

    @Test
    @DisplayName("getOrders: 주문이 없으면 빈 목록을 반환한다")
    void getOrders_주문이_없으면_빈_목록을_반환한다() {
        // Arrange
        given(sellerOrderMapper.findOrdersBySellerIdAndStartDate(any(), any()))
            .willReturn(List.of());

        // Act
        List<OrderSummaryResponse> result = sellerOrderService.getOrders(1L, OrderSearchPeriod.ALL);

        // Assert
        assertThat(result).isEmpty();
    }

    // ===== updateOrderStatus() =====

    @Test
    @DisplayName("updateOrderStatus: APPROVED 상태에서 SHIPPING으로 변경에 성공한다")
    void updateOrderStatus_APPROVED에서SHIPPING_성공() {
        // Arrange
        Long sellerId = 1L;
        String orderNumber = "ORD-010";
        OrderView order = buildOrderView(orderNumber, 0L, 0L);
        order.setApprovalStatus(OrderApprovalStatus.APPROVED.dbValue());

        given(sellerOrderMapper.findOrderByNumberAndSellerId(orderNumber, sellerId))
            .willReturn(Optional.of(order));
        given(sellerOrderMapper.updateApprovalStatus(order.getId(), OrderApprovalStatus.SHIPPING.dbValue()))
            .willReturn(1);

        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest(OrderApprovalStatus.SHIPPING.dbValue());

        // Act
        sellerOrderService.updateOrderStatus(sellerId, orderNumber, request);

        // Assert
        verify(sellerOrderMapper).updateApprovalStatus(order.getId(), OrderApprovalStatus.SHIPPING.dbValue());
    }

    @Test
    @DisplayName("updateOrderStatus: APPROVED에서 DELIVERED로 변경 시도 시 IllegalStateException을 던진다")
    void updateOrderStatus_APPROVED에서DELIVERED_IllegalStateException() {
        // Arrange
        Long sellerId = 1L;
        String orderNumber = "ORD-011";
        OrderView order = buildOrderView(orderNumber, 0L, 0L);
        order.setApprovalStatus(OrderApprovalStatus.APPROVED.dbValue());

        given(sellerOrderMapper.findOrderByNumberAndSellerId(orderNumber, sellerId))
            .willReturn(Optional.of(order));

        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest(OrderApprovalStatus.DELIVERED.dbValue());

        // Act & Assert
        assertThatThrownBy(() -> sellerOrderService.updateOrderStatus(sellerId, orderNumber, request))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("updateOrderStatus: SHIPPING 상태에서 DELIVERED로 변경에 성공한다")
    void updateOrderStatus_SHIPPING에서DELIVERED_성공() {
        // Arrange
        Long sellerId = 1L;
        String orderNumber = "ORD-012";
        OrderView order = buildOrderView(orderNumber, 0L, 0L);
        order.setApprovalStatus(OrderApprovalStatus.SHIPPING.dbValue());

        given(sellerOrderMapper.findOrderByNumberAndSellerId(orderNumber, sellerId))
            .willReturn(Optional.of(order));
        given(sellerOrderMapper.updateApprovalStatus(order.getId(), OrderApprovalStatus.DELIVERED.dbValue()))
            .willReturn(1);

        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest(OrderApprovalStatus.DELIVERED.dbValue());

        // Act
        sellerOrderService.updateOrderStatus(sellerId, orderNumber, request);

        // Assert
        verify(sellerOrderMapper).updateApprovalStatus(order.getId(), OrderApprovalStatus.DELIVERED.dbValue());
    }

    @Test
    @DisplayName("updateOrderStatus: CANCELLED 상태로 변경 시도 시 IllegalArgumentException을 던진다")
    void updateOrderStatus_CANCELLED설정시도_IllegalArgumentException() {
        // Arrange
        Long sellerId = 1L;
        String orderNumber = "ORD-013";
        OrderView order = buildOrderView(orderNumber, 0L, 0L);
        order.setApprovalStatus(OrderApprovalStatus.APPROVED.dbValue());

        given(sellerOrderMapper.findOrderByNumberAndSellerId(orderNumber, sellerId))
            .willReturn(Optional.of(order));

        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest(OrderApprovalStatus.CANCELLED.dbValue());

        // Act & Assert
        assertThatThrownBy(() -> sellerOrderService.updateOrderStatus(sellerId, orderNumber, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("판매자는 배송/거절 상태만 변경할 수 있어요.");
    }

    @Test
    @DisplayName("updateOrderStatus: 현재와 동일한 상태로 변경 시 updateApprovalStatus가 호출되지 않는다")
    void updateOrderStatus_동일상태_조기종료() {
        // Arrange
        Long sellerId = 1L;
        String orderNumber = "ORD-014";
        OrderView order = buildOrderView(orderNumber, 0L, 0L);
        order.setApprovalStatus(OrderApprovalStatus.SHIPPING.dbValue());

        given(sellerOrderMapper.findOrderByNumberAndSellerId(orderNumber, sellerId))
            .willReturn(Optional.of(order));

        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest(OrderApprovalStatus.SHIPPING.dbValue());

        // Act
        sellerOrderService.updateOrderStatus(sellerId, orderNumber, request);

        // Assert
        verify(sellerOrderMapper, never()).updateApprovalStatus(any(), any());
    }

    @Test
    @DisplayName("getOrderDetail: 다른 판매자의 주문 조회 시 IllegalArgumentException을 던진다")
    void getOrderDetail_타인주문_IllegalArgumentException() {
        // Arrange
        Long differentSellerId = 99L;
        String orderNumber = "ORD-015";

        given(sellerOrderMapper.findOrderByNumberAndSellerId(orderNumber, differentSellerId))
            .willReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> sellerOrderService.getOrderDetail(differentSellerId, orderNumber))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("주문을 찾을 수 없어요.");
    }

    // ===== 테스트 픽스처 =====

    private OrderView buildOrderView(String orderNumber, Long shippingFee, Long discountAmount) {
        return OrderView.builder()
            .id(100L)
            .orderNumber(orderNumber)
            .orderMode(OrderMode.CART)
            .approvalStatus("approved")
            .addressSnapshot(ADDRESS_SNAPSHOT_JSON)
            .userId(10L)
            .orderDate(LocalDateTime.now())
            .merchandiseAmount(0L)
            .shippingFee(shippingFee)
            .discountAmount(discountAmount)
            .totalAmount(0L)
            .paymentStatus(PaymentStatus.PAID)
            .build();
    }

    private OrderProductEntity buildItem(
        Long productId, String productName, int quantity, Long unitPrice, Long totalPrice
    ) {
        return OrderProductEntity.builder()
            .id(1L)
            .orderId(100L)
            .productId(productId)
            .productName(productName)
            .quantity(quantity)
            .unitPrice(unitPrice)
            .totalPrice(totalPrice)
            .build();
    }
}
