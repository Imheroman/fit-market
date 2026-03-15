package com.ssafy.fitmarket_be.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.fitmarket_be.address.repository.AddressRepository;
import com.ssafy.fitmarket_be.cart.repository.ShoppingCartRepository;
import com.ssafy.fitmarket_be.address.entity.Address;
import com.ssafy.fitmarket_be.cart.entity.ShoppingCartProduct;
import com.ssafy.fitmarket_be.order.domain.OrderApprovalStatus;
import com.ssafy.fitmarket_be.order.domain.OrderMode;
import com.ssafy.fitmarket_be.order.domain.OrderReturnExchangeReason;
import com.ssafy.fitmarket_be.order.domain.OrderReturnExchangeType;
import com.ssafy.fitmarket_be.order.domain.OrderSearchPeriod;
import com.ssafy.fitmarket_be.order.domain.OrderView;
import com.ssafy.fitmarket_be.order.dto.OrderAddressUpdateRequest;
import com.ssafy.fitmarket_be.order.dto.OrderCreateRequest;
import com.ssafy.fitmarket_be.order.dto.OrderCreateResponse;
import com.ssafy.fitmarket_be.order.dto.OrderDetailResponse;
import com.ssafy.fitmarket_be.order.dto.OrderRefundEligibilityResponse;
import com.ssafy.fitmarket_be.order.dto.OrderRefundRequest;
import com.ssafy.fitmarket_be.order.dto.OrderReturnExchangeRequest;
import com.ssafy.fitmarket_be.order.dto.OrderReturnExchangeResponse;
import com.ssafy.fitmarket_be.order.dto.OrderSummaryResponse;
import com.ssafy.fitmarket_be.order.repository.OrderRepository;
import com.ssafy.fitmarket_be.payment.domain.PaymentStatus;
import com.ssafy.fitmarket_be.payment.repository.PaymentRepository;
import com.ssafy.fitmarket_be.product.domain.Product;
import com.ssafy.fitmarket_be.product.repository.ProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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
        // DELIVERED는 isTerminal()=false이므로 "배송 완료된 주문은 반품/교환을 이용해 주세요." 메시지가 반환된다
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

    // ===== S2: 주문 생성 케이스 =====

    @Test
    @DisplayName("DIRECT 모드 정상 주문 생성 시 OrderCreateResponse가 반환된다")
    void createOrderInternal_DIRECT모드_정상() throws Exception {
        // given
        Long addressId = 10L;
        Address address = Address.builder()
                .id(addressId)
                .recipient("홍길동")
                .phone("010-1234-5678")
                .postalCode("12345")
                .addressLine("서울시 강남구")
                .addressLineDetail("101호")
                .build();
        given(addressRepository.findByIdAndUserId(addressId, USER_ID))
                .willReturn(Optional.of(address));

        // Product 생성 (package-private 생성자 대신 mock 사용)
        Product product = org.mockito.Mockito.mock(Product.class);
        given(product.getStock()).willReturn(10);
        given(product.getPrice()).willReturn(5000L);
        given(product.getId()).willReturn(1L);
        given(product.getName()).willReturn("테스트 상품");
        given(productMapper.selectProductById(1L)).willReturn(product);

        given(orderRepository.insertOrder(any())).willAnswer(invocation -> {
            com.ssafy.fitmarket_be.order.entity.Order order = invocation.getArgument(0);
            // MyBatis useGeneratedKeys 시뮬레이션: reflection으로 id 설정
            java.lang.reflect.Field idField = com.ssafy.fitmarket_be.order.entity.Order.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(order, ORDER_ID);
            return 1;
        });
        given(orderRepository.insertOrderProducts(anyLong(), any())).willReturn(1);
        given(orderRepository.insertOrderAddress(anyLong(), any())).willReturn(1);
        given(orderRepository.updateApprovalStatus(anyLong(), anyString())).willReturn(1);

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
    void createOrderInternal_CART모드_정상() throws Exception {
        // given
        Long addressId = 10L;
        Address address = Address.builder()
                .id(addressId)
                .recipient("홍길동")
                .phone("010-1234-5678")
                .postalCode("12345")
                .addressLine("서울시 강남구")
                .addressLineDetail("101호")
                .build();
        given(addressRepository.findByIdAndUserId(addressId, USER_ID))
                .willReturn(Optional.of(address));

        List<Long> cartItemIds = List.of(1L, 2L);
        List<ShoppingCartProduct> cartItems = List.of(
                ShoppingCartProduct.builder().id(1L).productId(10L).productName("상품A").price(3000L).quantity(2).build(),
                ShoppingCartProduct.builder().id(2L).productId(20L).productName("상품B").price(2000L).quantity(1).build()
        );
        given(shoppingCartRepository.findByIds(USER_ID, cartItemIds)).willReturn(cartItems);

        given(orderRepository.insertOrder(any())).willAnswer(invocation -> {
            com.ssafy.fitmarket_be.order.entity.Order order = invocation.getArgument(0);
            java.lang.reflect.Field idField = com.ssafy.fitmarket_be.order.entity.Order.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(order, ORDER_ID);
            return 1;
        });
        given(orderRepository.insertOrderProducts(anyLong(), any())).willReturn(2);
        given(orderRepository.insertOrderAddress(anyLong(), any())).willReturn(1);
        given(orderRepository.updateApprovalStatus(anyLong(), anyString())).willReturn(1);

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
    void createOrderInternal_총금액0이하_IllegalArgumentException() throws Exception {
        // given
        Long addressId = 10L;
        Address address = Address.builder()
                .id(addressId)
                .recipient("홍길동")
                .phone("010-1234-5678")
                .postalCode("12345")
                .addressLine("서울시 강남구")
                .addressLineDetail("101호")
                .build();
        given(addressRepository.findByIdAndUserId(addressId, USER_ID))
                .willReturn(Optional.of(address));

        Product product = org.mockito.Mockito.mock(Product.class);
        given(product.getStock()).willReturn(10);
        given(product.getPrice()).willReturn(1000L);  // merchandiseAmount = 1000
        given(product.getId()).willReturn(1L);
        given(product.getName()).willReturn("테스트 상품");
        given(productMapper.selectProductById(1L)).willReturn(product);

        // discountAmount=2000, merchandiseAmount=1000 => totalAmount = 1000 - 2000 = -1000
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
    void createOrderInternal_재고부족_IllegalArgumentException() throws Exception {
        // given
        Long addressId = 10L;
        Address address = Address.builder()
                .id(addressId)
                .recipient("홍길동")
                .phone("010-1234-5678")
                .postalCode("12345")
                .addressLine("서울시 강남구")
                .addressLineDetail("101호")
                .build();
        given(addressRepository.findByIdAndUserId(addressId, USER_ID))
                .willReturn(Optional.of(address));

        Product product = org.mockito.Mockito.mock(Product.class);
        given(product.getStock()).willReturn(2);  // 재고 2
        given(productMapper.selectProductById(1L)).willReturn(product);

        OrderCreateRequest request = new OrderCreateRequest(
                null, "DIRECT", 1L, 5, null,  // 수량 5
                addressId, 0L, 0L, null
        );

        // when / then
        assertThatThrownBy(() -> orderService.createOrderWithOrderNumber(USER_ID, null, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("재고가 부족해요.");
    }

    @Test
    @DisplayName("존재하지 않는 상품이면 IllegalArgumentException을 던진다")
    void createOrderInternal_상품없음_IllegalArgumentException() throws Exception {
        // given
        Long addressId = 10L;
        Address address = Address.builder()
                .id(addressId)
                .recipient("홍길동")
                .phone("010-1234-5678")
                .postalCode("12345")
                .addressLine("서울시 강남구")
                .addressLineDetail("101호")
                .build();
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
    void createOrderInternal_장바구니아이템수불일치_IllegalArgumentException() throws Exception {
        // given
        Long addressId = 10L;
        Address address = Address.builder()
                .id(addressId)
                .recipient("홍길동")
                .phone("010-1234-5678")
                .postalCode("12345")
                .addressLine("서울시 강남구")
                .addressLineDetail("101호")
                .build();
        given(addressRepository.findByIdAndUserId(addressId, USER_ID))
                .willReturn(Optional.of(address));

        List<Long> cartItemIds = List.of(1L, 2L, 3L);  // 3개 요청
        List<ShoppingCartProduct> cartItems = List.of(  // 2개만 반환
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
    void createOrderInternal_수량100초과_IllegalArgumentException() throws Exception {
        // given
        Long addressId = 10L;
        Address address = Address.builder()
                .id(addressId)
                .recipient("홍길동")
                .phone("010-1234-5678")
                .postalCode("12345")
                .addressLine("서울시 강남구")
                .addressLineDetail("101호")
                .build();
        given(addressRepository.findByIdAndUserId(addressId, USER_ID))
                .willReturn(Optional.of(address));

        Product product = org.mockito.Mockito.mock(Product.class);
        given(product.getStock()).willReturn(200);
        given(productMapper.selectProductById(1L)).willReturn(product);

        OrderCreateRequest request = new OrderCreateRequest(
                null, "DIRECT", 1L, 101, null,  // 수량 101
                addressId, 0L, 0L, null
        );

        // when / then
        assertThatThrownBy(() -> orderService.createOrderWithOrderNumber(USER_ID, null, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("한 상품은 한 번에 최대 100개까지 주문할 수 있어요.");
    }

    @Test
    @DisplayName("중복 주문 번호 발생 시 IllegalStateException을 던진다")
    void createOrderInternal_중복주문번호_IllegalStateException() throws Exception {
        // given
        Long addressId = 10L;
        Address address = Address.builder()
                .id(addressId)
                .recipient("홍길동")
                .phone("010-1234-5678")
                .postalCode("12345")
                .addressLine("서울시 강남구")
                .addressLineDetail("101호")
                .build();
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

    // ===== S2: 주문 조회 케이스 =====

    @Test
    @DisplayName("주문이 있으면 OrderSummaryResponse 목록을 반환한다")
    void getOrders_주문있음_목록반환() {
        // given
        OrderView order1 = buildOrder(OrderApprovalStatus.APPROVED, PaymentStatus.PAID);
        OrderView order2 = buildOrderWithId(200L, "ORD-002", OrderApprovalStatus.SHIPPING, PaymentStatus.PAID);
        given(orderRepository.findOrdersByUserIdAndStartDate(eq(USER_ID), any()))
                .willReturn(List.of(order1, order2));
        given(orderRepository.findOrderProductsByOrderIds(any())).willReturn(List.of());

        // when
        List<OrderSummaryResponse> result = orderService.getOrders(USER_ID, OrderSearchPeriod.MONTH_3);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(r -> r instanceof OrderSummaryResponse);
    }

    @Test
    @DisplayName("주문이 없으면 빈 목록을 반환한다")
    void getOrders_주문없음_빈목록() {
        // given
        given(orderRepository.findOrdersByUserIdAndStartDate(eq(USER_ID), any()))
                .willReturn(List.of());

        // when
        List<OrderSummaryResponse> result = orderService.getOrders(USER_ID, OrderSearchPeriod.MONTH_3);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("주문 상세 조회 시 OrderDetailResponse를 반환한다")
    void getOrderDetail_정상() {
        // given
        OrderView order = buildOrder(OrderApprovalStatus.APPROVED, PaymentStatus.PAID);
        order.setAddressSnapshot("{\"recipient\":\"홍길동\",\"phone\":\"010-1234-5678\","
                + "\"postalCode\":\"12345\",\"addressLine\":\"서울시 강남구\",\"addressLineDetail\":\"101호\",\"memo\":null}");
        given(orderRepository.findOrderByNumberAndUserId(ORDER_NUMBER, USER_ID))
                .willReturn(Optional.of(order));
        given(orderRepository.findOrderProductsByOrderIds(any())).willReturn(List.of());
        given(orderRepository.findOrderReturnExchangeByOrderId(ORDER_ID)).willReturn(Optional.empty());
        given(paymentRepository.findApprovedAtByOrderId(ORDER_ID))
                .willReturn(Optional.of(LocalDateTime.now().minusDays(1)));

        // when
        OrderDetailResponse result = orderService.getOrderDetail(USER_ID, ORDER_NUMBER);

        // then
        assertThat(result).isInstanceOf(OrderDetailResponse.class);
    }

    @Test
    @DisplayName("타인의 주문 번호 조회 시 IllegalArgumentException을 던진다")
    void getOrderDetail_타인주문_IllegalArgumentException() {
        // given
        given(orderRepository.findOrderByNumberAndUserId(ORDER_NUMBER, 2L))
                .willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> orderService.getOrderDetail(2L, ORDER_NUMBER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("주문을 찾을 수 없어요.");
    }

    @Test
    @DisplayName("배송 시작 이후 배송지 변경 시 IllegalStateException을 던진다")
    void updateOrderAddress_배송시작후_IllegalStateException() {
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

    @Test
    @DisplayName("PENDING_APPROVAL 상태에서 CANCELLED로 상태 변경이 성공한다")
    void cancelOrder_PENDING_APPROVAL상태_성공() {
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
    @DisplayName("DELIVERED 상태 주문은 PENDING_APPROVAL이 아니므로 취소할 수 없는 주문 상태 IllegalStateException을 던진다")
    void cancelOrder_DELIVERED상태_취소불가() {
        // given - DELIVERED는 isTerminal()=false, but not PENDING_APPROVAL so cannot cancel
        OrderView order = buildOrder(OrderApprovalStatus.DELIVERED, PaymentStatus.PAID);
        given(orderRepository.findOrderByNumberAndUserId(ORDER_NUMBER, USER_ID))
                .willReturn(Optional.of(order));

        // when / then
        assertThatThrownBy(() -> orderService.cancelOrder(USER_ID, ORDER_NUMBER))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("취소할 수 없는 주문 상태예요.");
    }

    @Test
    @DisplayName("주문 소프트 삭제가 성공하면 주문 상품도 소프트 삭제된다")
    void deleteOrder_성공() {
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

    // ===== S2: 환불·반품교환·eligibility 케이스 =====

    @Test
    @DisplayName("결제 전(PENDING) 상태 주문은 환불 시 IllegalArgumentException을 던진다")
    void refundOrder_결제전_IllegalArgumentException() {
        // given
        OrderView order = buildOrder(OrderApprovalStatus.APPROVED, PaymentStatus.PENDING);
        given(orderRepository.findOrderByNumberAndUserId(ORDER_NUMBER, USER_ID))
                .willReturn(Optional.of(order));
        given(orderRepository.countOrderReturnExchanges(ORDER_ID)).willReturn(0);

        OrderRefundRequest request = new OrderRefundRequest(OrderReturnExchangeReason.OTHER, "환불 원함");

        // when / then
        assertThatThrownBy(() -> orderService.refundOrder(USER_ID, ORDER_NUMBER, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("결제 완료된 주문만 환불할 수 있어요.");
    }

    @Test
    @DisplayName("결제 후 3일 초과 주문은 환불 시 IllegalArgumentException을 던진다")
    void refundOrder_3일초과_IllegalArgumentException() {
        // given
        OrderView order = buildOrder(OrderApprovalStatus.APPROVED, PaymentStatus.PAID);
        given(orderRepository.findOrderByNumberAndUserId(ORDER_NUMBER, USER_ID))
                .willReturn(Optional.of(order));
        given(orderRepository.countOrderReturnExchanges(ORDER_ID)).willReturn(0);
        given(paymentRepository.findApprovedAtByOrderId(ORDER_ID))
                .willReturn(Optional.of(LocalDateTime.now().minusDays(4)));

        OrderRefundRequest request = new OrderRefundRequest(OrderReturnExchangeReason.OTHER, "환불 원함");

        // when / then
        assertThatThrownBy(() -> orderService.refundOrder(USER_ID, ORDER_NUMBER, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("결제 후 3일이 지나 환불할 수 없어요.");
    }

    @Test
    @DisplayName("이미 클레임이 접수된 주문은 환불 시 IllegalArgumentException을 던진다")
    void refundOrder_이미클레임접수_IllegalArgumentException() {
        // given
        OrderView order = buildOrder(OrderApprovalStatus.APPROVED, PaymentStatus.PAID);
        given(orderRepository.findOrderByNumberAndUserId(ORDER_NUMBER, USER_ID))
                .willReturn(Optional.of(order));
        given(orderRepository.countOrderReturnExchanges(ORDER_ID)).willReturn(1);

        OrderRefundRequest request = new OrderRefundRequest(OrderReturnExchangeReason.OTHER, "환불 원함");

        // when / then
        assertThatThrownBy(() -> orderService.refundOrder(USER_ID, ORDER_NUMBER, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 환불/반품/교환 요청이 접수된 주문이에요.");
    }

    @Test
    @DisplayName("배송 중인 주문은 환불 시 IllegalArgumentException을 던진다")
    void refundOrder_배송중_환불불가() {
        // given
        OrderView order = buildOrder(OrderApprovalStatus.SHIPPING, PaymentStatus.PAID);
        given(orderRepository.findOrderByNumberAndUserId(ORDER_NUMBER, USER_ID))
                .willReturn(Optional.of(order));
        given(orderRepository.countOrderReturnExchanges(ORDER_ID)).willReturn(0);

        OrderRefundRequest request = new OrderRefundRequest(OrderReturnExchangeReason.OTHER, "환불 원함");

        // when / then
        assertThatThrownBy(() -> orderService.refundOrder(USER_ID, ORDER_NUMBER, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("배송이 시작된 주문은 환불할 수 없어요.");
    }

    @Test
    @DisplayName("정상 환불 시 결제 상태가 REFUNDED로 변경되고 주문 상태가 CANCELLED로 변경된다")
    void refundOrder_정상_REFUNDED상태전환() {
        // given
        OrderView order = buildOrder(OrderApprovalStatus.APPROVED, PaymentStatus.PAID);
        given(orderRepository.findOrderByNumberAndUserId(ORDER_NUMBER, USER_ID))
                .willReturn(Optional.of(order));
        given(orderRepository.countOrderReturnExchanges(ORDER_ID)).willReturn(0);
        given(paymentRepository.findApprovedAtByOrderId(ORDER_ID))
                .willReturn(Optional.of(LocalDateTime.now().minusDays(1)));
        given(orderRepository.updatePaymentStatus(ORDER_ID, PaymentStatus.REFUNDED)).willReturn(1);
        given(paymentRepository.updateStatusByOrderId(ORDER_ID, PaymentStatus.REFUNDED)).willReturn(1);
        given(orderRepository.updateApprovalStatus(ORDER_ID, OrderApprovalStatus.CANCELLED.dbValue())).willReturn(1);
        given(orderRepository.insertOrderReturnExchange(any())).willReturn(1);

        OrderRefundRequest request = new OrderRefundRequest(OrderReturnExchangeReason.CHANGE_OF_MIND, "환불 원함");

        // when
        orderService.refundOrder(USER_ID, ORDER_NUMBER, request);

        // then
        verify(orderRepository).updatePaymentStatus(ORDER_ID, PaymentStatus.REFUNDED);
        verify(orderRepository).updateApprovalStatus(ORDER_ID, OrderApprovalStatus.CANCELLED.dbValue());
    }

    @Test
    @DisplayName("배송 완료 후 7일 이내 반품/교환 요청은 성공한다")
    void requestReturnOrExchange_배송완료7일이내_성공() {
        // given
        OrderView order = buildOrderWithDueDate(
                OrderApprovalStatus.DELIVERED,
                PaymentStatus.PAID,
                LocalDateTime.now().minusDays(3)
        );
        given(orderRepository.findOrderByNumberAndUserId(ORDER_NUMBER, USER_ID))
                .willReturn(Optional.of(order));
        given(orderRepository.countOrderReturnExchanges(ORDER_ID)).willReturn(0);
        given(orderRepository.insertOrderReturnExchange(any())).willReturn(1);

        OrderReturnExchangeRequest request = new OrderReturnExchangeRequest(
                OrderReturnExchangeType.RETURN,
                OrderReturnExchangeReason.QUALITY_ISSUE,
                "품질 불량"
        );

        // when
        OrderReturnExchangeResponse result = orderService.requestReturnOrExchange(USER_ID, ORDER_NUMBER, request);

        // then
        assertThat(result.eligible()).isTrue();
        verify(orderRepository).insertOrderReturnExchange(any());
    }

    @Test
    @DisplayName("배송 완료 후 7일 초과 시 반품/교환 요청은 false를 반환하고 insertOrderReturnExchange를 호출하지 않는다")
    void requestReturnOrExchange_7일초과_false반환() {
        // given
        OrderView order = buildOrderWithDueDate(
                OrderApprovalStatus.DELIVERED,
                PaymentStatus.PAID,
                LocalDateTime.now().minusDays(8)
        );
        given(orderRepository.findOrderByNumberAndUserId(ORDER_NUMBER, USER_ID))
                .willReturn(Optional.of(order));
        given(orderRepository.countOrderReturnExchanges(ORDER_ID)).willReturn(0);

        OrderReturnExchangeRequest request = new OrderReturnExchangeRequest(
                OrderReturnExchangeType.RETURN,
                OrderReturnExchangeReason.QUALITY_ISSUE,
                "품질 불량"
        );

        // when
        OrderReturnExchangeResponse result = orderService.requestReturnOrExchange(USER_ID, ORDER_NUMBER, request);

        // then
        assertThat(result.eligible()).isFalse();
        verify(orderRepository, never()).insertOrderReturnExchange(any());
    }

    @Test
    @DisplayName("주문 소프트 삭제 실패(0 반환) 시 IllegalStateException을 던진다")
    void deleteOrder_실패_IllegalStateException() {
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

    @Test
    @DisplayName("배송 완료 상태 주문은 반품/교환 안내 메시지와 함께 환불 불가를 반환한다")
    void evaluateRefundEligibility_배송완료상태_환불불가() {
        // given: DELIVERED는 isTerminal()=false, 별도 분기로 반품/교환 안내
        OrderView order = buildOrder(OrderApprovalStatus.DELIVERED, PaymentStatus.PAID);
        given(orderRepository.findOrderByNumberAndUserId(ORDER_NUMBER, USER_ID))
                .willReturn(Optional.of(order));
        given(orderRepository.countOrderReturnExchanges(ORDER_ID)).willReturn(0);

        // when
        OrderRefundEligibilityResponse result = orderService.getRefundEligibility(USER_ID, ORDER_NUMBER);

        // then: DELIVERED → "배송 완료된 주문은 반품/교환을 이용해 주세요."
        assertThat(result.eligible()).isFalse();
        assertThat(result.message()).contains("배송 완료된 주문은 반품/교환을 이용해 주세요.");
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

    private OrderView buildOrderWithId(Long id, String orderNumber, OrderApprovalStatus approvalStatus,
            PaymentStatus paymentStatus) {
        return OrderView.builder()
            .id(id)
            .orderNumber(orderNumber)
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

    private OrderView buildOrderWithDueDate(OrderApprovalStatus approvalStatus,
            PaymentStatus paymentStatus, LocalDateTime dueDate) {
        return OrderView.builder()
            .id(ORDER_ID)
            .orderNumber(ORDER_NUMBER)
            .orderMode(OrderMode.DIRECT)
            .approvalStatus(approvalStatus.dbValue())
            .userId(USER_ID)
            .orderDate(LocalDateTime.now().minusDays(10))
            .merchandiseAmount(10000L)
            .shippingFee(0L)
            .discountAmount(0L)
            .totalAmount(10000L)
            .paymentStatus(paymentStatus)
            .dueDate(dueDate)
            .build();
    }
}
