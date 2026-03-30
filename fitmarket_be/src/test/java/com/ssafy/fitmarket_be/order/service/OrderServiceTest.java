package com.ssafy.fitmarket_be.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.fitmarket_be.address.repository.AddressRepository;
import com.ssafy.fitmarket_be.cart.repository.ShoppingCartRepository;
import com.ssafy.fitmarket_be.entity.Address;
import com.ssafy.fitmarket_be.entity.ShoppingCartProduct;
import com.ssafy.fitmarket_be.order.domain.OrderApprovalStatus;
import com.ssafy.fitmarket_be.order.domain.OrderMode;
import com.ssafy.fitmarket_be.order.domain.OrderView;
import com.ssafy.fitmarket_be.order.dto.OrderAddressUpdateRequest;
import com.ssafy.fitmarket_be.order.dto.OrderCreateRequest;
import com.ssafy.fitmarket_be.order.dto.OrderCreateResponse;
import com.ssafy.fitmarket_be.order.repository.OrderRepository;
import com.ssafy.fitmarket_be.payment.domain.PaymentStatus;
import com.ssafy.fitmarket_be.product.domain.Product;
import com.ssafy.fitmarket_be.product.repository.ProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * OrderService 단위 테스트 — 주문 생성/수정/삭제 로직 검증.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService")
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
    private com.ssafy.fitmarket_be.ranking.service.ProductRankingService rankingService;

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
            objectMapper,
            rankingService
        );
    }

    // ===== 테스트 픽스처 =====

    private Address buildAddress(Long addressId) {
        return Address.builder()
                .id(addressId)
                .recipient("홍길동")
                .phone("010-1234-5678")
                .postalCode("12345")
                .addressLine("서울시 강남구")
                .addressLineDetail("101호")
                .build();
    }

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

    @Nested
    @DisplayName("createOrderWithOrderNumber")
    class CreateOrderWithOrderNumber {

        @Test
        @DisplayName("DIRECT 모드 정상 주문 생성 시 OrderCreateResponse가 반환된다")
        void DIRECT모드_정상_주문생성() {
            // given
            Long addressId = 10L;
            Address address = buildAddress(addressId);
            given(addressRepository.findByIdAndUserId(addressId, USER_ID))
                    .willReturn(Optional.of(address));

            Product product = org.mockito.Mockito.mock(Product.class);
            given(product.getStock()).willReturn(10);
            given(product.getPrice()).willReturn(5000L);
            given(product.getId()).willReturn(1L);
            given(product.getName()).willReturn("테스트 상품");
            given(productMapper.selectProductById(1L)).willReturn(product);

            given(orderRepository.insertOrder(any())).willAnswer(invocation -> {
                com.ssafy.fitmarket_be.entity.Order order = invocation.getArgument(0);
                ReflectionTestUtils.setField(order, "id", ORDER_ID);
                return 1;
            });
            given(orderRepository.insertOrderProducts(anyLong(), any())).willReturn(1);
            given(orderRepository.insertOrderAddress(anyLong(), any())).willReturn(1);
            given(orderRepository.updateApprovalStatus(anyLong(), anyString())).willReturn(1);
            given(productMapper.decreaseStock(anyLong(), anyInt())).willReturn(1);

            OrderCreateRequest request = new OrderCreateRequest(
                    "ORD-NEW-001", "DIRECT", 1L, 1, null,
                    addressId, 0L, 0L, null
            );

            // when
            OrderCreateResponse result = orderService.createOrderWithOrderNumber(USER_ID, "ORD-NEW-001", request);

            // then
            assertThat(result).isInstanceOf(OrderCreateResponse.class);
            verify(orderRepository).insertOrder(any());
        }

        @Test
        @DisplayName("CART 모드 정상 주문 생성 시 OrderCreateResponse가 반환된다")
        void CART모드_정상_주문생성() {
            // given
            Long addressId = 10L;
            Address address = buildAddress(addressId);
            given(addressRepository.findByIdAndUserId(addressId, USER_ID))
                    .willReturn(Optional.of(address));

            List<Long> cartItemIds = List.of(1L, 2L);
            List<ShoppingCartProduct> cartItems = List.of(
                    ShoppingCartProduct.builder().id(1L).productId(10L).productName("상품A").price(3000L).quantity(2).build(),
                    ShoppingCartProduct.builder().id(2L).productId(20L).productName("상품B").price(2000L).quantity(1).build()
            );
            given(shoppingCartRepository.findByIds(USER_ID, cartItemIds)).willReturn(cartItems);

            Product productA = org.mockito.Mockito.mock(Product.class);
            given(productA.getStock()).willReturn(10);
            given(productMapper.selectProductById(10L)).willReturn(productA);

            Product productB = org.mockito.Mockito.mock(Product.class);
            given(productB.getStock()).willReturn(10);
            given(productMapper.selectProductById(20L)).willReturn(productB);

            given(orderRepository.insertOrder(any())).willAnswer(invocation -> {
                com.ssafy.fitmarket_be.entity.Order order = invocation.getArgument(0);
                java.lang.reflect.Field idField = com.ssafy.fitmarket_be.entity.Order.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(order, ORDER_ID);
                return 1;
            });
            given(orderRepository.insertOrderProducts(anyLong(), any())).willReturn(2);
            given(orderRepository.insertOrderAddress(anyLong(), any())).willReturn(1);
            given(orderRepository.updateApprovalStatus(anyLong(), anyString())).willReturn(1);
            given(productMapper.decreaseStock(anyLong(), anyInt())).willReturn(1);

            OrderCreateRequest request = new OrderCreateRequest(
                    "ORD-CART-001", "CART", null, null, cartItemIds,
                    addressId, 0L, 0L, null
            );

            // when
            OrderCreateResponse result = orderService.createOrderWithOrderNumber(USER_ID, "ORD-CART-001", request);

            // then
            assertThat(result).isInstanceOf(OrderCreateResponse.class);
            verify(orderRepository).insertOrder(any());
        }

        @Test
        @DisplayName("총 금액이 0 이하이면 IllegalArgumentException을 던진다")
        void 총금액_0이하_예외발생() {
            // given
            Long addressId = 10L;
            Address address = buildAddress(addressId);
            given(addressRepository.findByIdAndUserId(addressId, USER_ID))
                    .willReturn(Optional.of(address));

            Product product = org.mockito.Mockito.mock(Product.class);
            given(product.getStock()).willReturn(10);
            given(product.getPrice()).willReturn(1000L);
            given(product.getId()).willReturn(1L);
            given(product.getName()).willReturn("테스트 상품");
            given(productMapper.selectProductById(1L)).willReturn(product);

            OrderCreateRequest request = new OrderCreateRequest(
                    null, "DIRECT", 1L, 1, null,
                    addressId, 0L, 2000L, null
            );

            // when / then
            assertThatThrownBy(() -> orderService.createOrderWithOrderNumber(USER_ID, null, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("결제 금액이 0원 이하예요.");
        }

        @Test
        @DisplayName("상품 재고가 부족하면 IllegalArgumentException을 던진다")
        void 재고_부족_예외발생() {
            // given
            Long addressId = 10L;
            Address address = buildAddress(addressId);
            given(addressRepository.findByIdAndUserId(addressId, USER_ID))
                    .willReturn(Optional.of(address));

            Product product = org.mockito.Mockito.mock(Product.class);
            given(product.getStock()).willReturn(2);
            given(productMapper.selectProductById(1L)).willReturn(product);

            OrderCreateRequest request = new OrderCreateRequest(
                    null, "DIRECT", 1L, 5, null,
                    addressId, 0L, 0L, null
            );

            // when / then
            assertThatThrownBy(() -> orderService.createOrderWithOrderNumber(USER_ID, null, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("재고가 부족해요.");
        }

        @Test
        @DisplayName("존재하지 않는 상품이면 IllegalArgumentException을 던진다")
        void 상품_미존재_예외발생() {
            // given
            Long addressId = 10L;
            Address address = buildAddress(addressId);
            given(addressRepository.findByIdAndUserId(addressId, USER_ID))
                    .willReturn(Optional.of(address));
            given(productMapper.selectProductById(1L)).willReturn(null);

            OrderCreateRequest request = new OrderCreateRequest(
                    null, "DIRECT", 1L, 1, null,
                    addressId, 0L, 0L, null
            );

            // when / then
            assertThatThrownBy(() -> orderService.createOrderWithOrderNumber(USER_ID, null, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("상품 정보를 찾을 수 없어요.");
        }

        @Test
        @DisplayName("장바구니 아이템 수 불일치 시 IllegalArgumentException을 던진다")
        void 장바구니_아이템수_불일치_예외발생() {
            // given
            Long addressId = 10L;
            Address address = buildAddress(addressId);
            given(addressRepository.findByIdAndUserId(addressId, USER_ID))
                    .willReturn(Optional.of(address));

            List<Long> cartItemIds = List.of(1L, 2L, 3L);
            List<ShoppingCartProduct> cartItems = List.of(
                    ShoppingCartProduct.builder().id(1L).productId(10L).productName("상품A").price(3000L).quantity(2).build(),
                    ShoppingCartProduct.builder().id(2L).productId(20L).productName("상품B").price(2000L).quantity(1).build()
            );
            given(shoppingCartRepository.findByIds(USER_ID, cartItemIds)).willReturn(cartItems);

            OrderCreateRequest request = new OrderCreateRequest(
                    null, "CART", null, null, cartItemIds,
                    addressId, 0L, 0L, null
            );

            // when / then
            assertThatThrownBy(() -> orderService.createOrderWithOrderNumber(USER_ID, null, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("선택한 장바구니 상품을 모두 찾을 수 없어요.");
        }

        @Test
        @DisplayName("수량 100 초과 시 IllegalArgumentException을 던진다")
        void 수량_100초과_예외발생() {
            // given
            Long addressId = 10L;
            Address address = buildAddress(addressId);
            given(addressRepository.findByIdAndUserId(addressId, USER_ID))
                    .willReturn(Optional.of(address));

            Product product = org.mockito.Mockito.mock(Product.class);
            given(product.getStock()).willReturn(200);
            given(productMapper.selectProductById(1L)).willReturn(product);

            OrderCreateRequest request = new OrderCreateRequest(
                    null, "DIRECT", 1L, 101, null,
                    addressId, 0L, 0L, null
            );

            // when / then
            assertThatThrownBy(() -> orderService.createOrderWithOrderNumber(USER_ID, null, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("한 상품은 한 번에 최대 100개까지 주문할 수 있어요.");
        }

        @Test
        @DisplayName("중복 주문 번호 발생 시 IllegalStateException을 던진다")
        void 중복_주문번호_예외발생() {
            // given
            Long addressId = 10L;
            Address address = buildAddress(addressId);
            given(addressRepository.findByIdAndUserId(addressId, USER_ID))
                    .willReturn(Optional.of(address));

            Product product = org.mockito.Mockito.mock(Product.class);
            given(product.getStock()).willReturn(10);
            given(product.getPrice()).willReturn(5000L);
            given(product.getId()).willReturn(1L);
            given(product.getName()).willReturn("테스트 상품");
            given(productMapper.selectProductById(1L)).willReturn(product);
            given(orderRepository.insertOrder(any())).willThrow(new DuplicateKeyException("Duplicate key"));

            OrderCreateRequest request = new OrderCreateRequest(
                    "DUP-ORDER-001", "DIRECT", 1L, 1, null,
                    addressId, 0L, 0L, null
            );

            // when / then
            assertThatThrownBy(() -> orderService.createOrderWithOrderNumber(USER_ID, "DUP-ORDER-001", request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("이미 처리 중인 주문 번호예요.");
        }
    }

    @Nested
    @DisplayName("updateOrderAddress")
    class UpdateOrderAddress {

        @Test
        @DisplayName("배송 시작 이후 배송지 변경 시 IllegalStateException을 던진다")
        void 배송시작_이후_배송지변경_예외발생() {
            // given
            OrderView order = buildOrder(OrderApprovalStatus.SHIPPING, PaymentStatus.PAID);
            given(orderRepository.findOrderByNumberAndUserId(ORDER_NUMBER, USER_ID))
                    .willReturn(Optional.of(order));

            OrderAddressUpdateRequest request = new OrderAddressUpdateRequest(10L);

            // when / then
            assertThatThrownBy(() -> orderService.updateOrderAddress(USER_ID, ORDER_NUMBER, request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("배송이 시작된 주문은 배송지를 바꿀 수 없어요.");
        }
    }

    @Nested
    @DisplayName("cancelOrder")
    class CancelOrder {

        @Test
        @DisplayName("PENDING_APPROVAL 상태에서 취소가 성공한다")
        void PENDING_APPROVAL_상태_취소_성공() {
            // given
            OrderView order = buildOrder(OrderApprovalStatus.PENDING_APPROVAL, PaymentStatus.PENDING);
            given(orderRepository.findOrderByNumberAndUserId(ORDER_NUMBER, USER_ID))
                    .willReturn(Optional.of(order));
            given(orderRepository.updateApprovalStatus(anyLong(), anyString())).willReturn(1);

            // when
            orderService.cancelOrder(USER_ID, ORDER_NUMBER);

            // then
            verify(orderRepository).updateApprovalStatus(
                    eq(ORDER_ID), eq(OrderApprovalStatus.CANCELLED.dbValue())
            );
        }

        @Test
        @DisplayName("DELIVERED 상태 주문은 취소할 수 없어 IllegalStateException을 던진다")
        void DELIVERED_상태_취소불가_예외발생() {
            // given
            OrderView order = buildOrder(OrderApprovalStatus.DELIVERED, PaymentStatus.PAID);
            given(orderRepository.findOrderByNumberAndUserId(ORDER_NUMBER, USER_ID))
                    .willReturn(Optional.of(order));

            // when / then
            assertThatThrownBy(() -> orderService.cancelOrder(USER_ID, ORDER_NUMBER))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("취소할 수 없는 주문 상태예요.");
        }
    }

    @Nested
    @DisplayName("deleteOrder")
    class DeleteOrder {

        @Test
        @DisplayName("주문 소프트 삭제가 성공하면 주문 상품도 소프트 삭제된다")
        void 주문_소프트삭제_성공() {
            // given
            OrderView order = buildOrder(OrderApprovalStatus.CANCELLED, PaymentStatus.PENDING);
            given(orderRepository.findOrderByNumberAndUserId(ORDER_NUMBER, USER_ID))
                    .willReturn(Optional.of(order));
            given(orderRepository.softDeleteOrder(ORDER_ID, USER_ID)).willReturn(1);
            given(orderRepository.softDeleteOrderProducts(ORDER_ID)).willReturn(1);

            // when
            orderService.deleteOrder(USER_ID, ORDER_NUMBER);

            // then
            verify(orderRepository).softDeleteOrderProducts(ORDER_ID);
        }

        @Test
        @DisplayName("주문 소프트 삭제 실패(0 반환) 시 IllegalStateException을 던진다")
        void 주문_소프트삭제_실패_예외발생() {
            // given
            OrderView order = buildOrder(OrderApprovalStatus.CANCELLED, PaymentStatus.PENDING);
            given(orderRepository.findOrderByNumberAndUserId(ORDER_NUMBER, USER_ID))
                    .willReturn(Optional.of(order));
            given(orderRepository.softDeleteOrder(ORDER_ID, USER_ID)).willReturn(0);

            // when / then
            assertThatThrownBy(() -> orderService.deleteOrder(USER_ID, ORDER_NUMBER))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("주문을 삭제하지 못했어요.");
        }
    }
}
