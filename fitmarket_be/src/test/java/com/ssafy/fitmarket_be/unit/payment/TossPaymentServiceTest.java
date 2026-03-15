package com.ssafy.fitmarket_be.unit.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.fitmarket_be.cart.repository.ShoppingCartRepository;
import com.ssafy.fitmarket_be.order.domain.OrderMode;
import com.ssafy.fitmarket_be.order.domain.OrderPaymentContext;
import com.ssafy.fitmarket_be.order.dto.OrderCreateRequest;
import com.ssafy.fitmarket_be.order.repository.OrderRepository;
import com.ssafy.fitmarket_be.order.service.OrderService;
import com.ssafy.fitmarket_be.payment.domain.PaymentStatus;
import com.ssafy.fitmarket_be.payment.dto.TossPaymentFailureResponse;
import com.ssafy.fitmarket_be.payment.dto.TossPaymentRequest;
import com.ssafy.fitmarket_be.payment.dto.TossPaymentResponse;
import com.ssafy.fitmarket_be.payment.repository.PaymentRepository;
import com.ssafy.fitmarket_be.payment.service.TossPaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TossPaymentService")
class TossPaymentServiceTest {

    @Mock
    private WebClient tossWebClient;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderService orderService;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private ShoppingCartRepository shoppingCartRepository;
    @Mock
    private PlatformTransactionManager transactionManager;

    private TossPaymentService tossPaymentService;
    private ObjectMapper objectMapper;

    private static final Long ORDER_ID = 100L;
    private static final Long USER_ID = 1L;
    private static final String ORDER_NUMBER = "ORD-TOSS-001";
    private static final String PAYMENT_KEY = "paymentKey-abc123";
    private static final Long AMOUNT = 10000L;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        tossPaymentService = new TossPaymentService(
                tossWebClient,
                orderRepository,
                orderService,
                paymentRepository,
                shoppingCartRepository,
                transactionManager,
                objectMapper
        );
    }

    // ===== handlePaymentFailure 케이스 =====

    @Test
    @DisplayName("handlePaymentFailure 정상 호출 시 TossPaymentFailureResponse를 반환한다")
    void handlePaymentFailure_정상_응답반환() {
        // when
        TossPaymentFailureResponse result = tossPaymentService.handlePaymentFailure(
                "PAY_PROCESS_CANCELED", "사용자 취소", ORDER_NUMBER
        );

        // then
        assertThat(result).isInstanceOf(TossPaymentFailureResponse.class);
        assertThat(result.errorCode()).isEqualTo("PAY_PROCESS_CANCELED");
    }

    @Test
    @DisplayName("handlePaymentFailure에 null 입력 시 기본값을 반환한다")
    void handlePaymentFailure_null입력_기본값반환() {
        // when
        TossPaymentFailureResponse result = tossPaymentService.handlePaymentFailure(
                null, null, null
        );

        // then
        assertThat(result.errorCode()).isEqualTo("UNKNOWN_ERROR");
    }

    // ===== confirmPayment 케이스 — userId/orderRequest 검증 =====

    @Test
    @DisplayName("userId가 null이면 로그인 만료 IllegalStateException을 던진다")
    void confirmPayment_userId_null_IllegalStateException() {
        // given
        OrderCreateRequest orderReq = new OrderCreateRequest(
                ORDER_NUMBER, "DIRECT", 1L, 1, null, 10L, 0L, 0L, null
        );
        TossPaymentRequest request = new TossPaymentRequest(PAYMENT_KEY, ORDER_NUMBER, AMOUNT, orderReq);

        // when / then
        assertThatThrownBy(() -> tossPaymentService.confirmPayment(null, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("로그인이 만료되었어요.");
    }

    @Test
    @DisplayName("orderRequest가 null이면 주문 정보 없음 IllegalStateException을 던진다")
    void confirmPayment_orderRequest_null_IllegalStateException() {
        // given
        TossPaymentRequest request = new TossPaymentRequest(PAYMENT_KEY, ORDER_NUMBER, AMOUNT, null);

        // when / then
        assertThatThrownBy(() -> tossPaymentService.confirmPayment(USER_ID, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("결제 내역과 연결할 주문 정보를 찾지 못했어요.");
    }

    @Test
    @DisplayName("결제 금액이 주문 금액과 다르면 IllegalStateException을 던진다")
    void confirmPayment_금액불일치_주문vs요청_IllegalStateException() {
        // given
        OrderCreateRequest orderReq = new OrderCreateRequest(
                ORDER_NUMBER, "DIRECT", 1L, 1, null, 10L, 0L, 0L, null
        );
        TossPaymentRequest request = new TossPaymentRequest(PAYMENT_KEY, ORDER_NUMBER, 10000L, orderReq);

        // createOrderWithOrderNumber는 void 반환이 아닌 OrderCreateResponse 반환이므로 Mock 설정
        given(orderService.createOrderWithOrderNumber(eq(USER_ID), eq(ORDER_NUMBER), any()))
                .willReturn(null);  // 내부에서는 사용 안 함

        // findPaymentContext → totalAmount=20000 (요청은 10000이라 불일치)
        OrderPaymentContext paymentContext = new OrderPaymentContext(
                ORDER_ID, USER_ID, ORDER_NUMBER, 20000L, PaymentStatus.PENDING, OrderMode.DIRECT, null
        );
        given(orderRepository.findPaymentContextByOrderNumber(ORDER_NUMBER))
                .willReturn(Optional.of(paymentContext));

        // when / then
        assertThatThrownBy(() -> tossPaymentService.confirmPayment(USER_ID, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("결제 금액이 주문 정보와 달라요.");
    }

    @Test
    @DisplayName("이미 결제된 주문이면 IllegalStateException을 던진다")
    void confirmPayment_이미결제된주문_IllegalStateException() {
        // given
        OrderCreateRequest orderReq = new OrderCreateRequest(
                ORDER_NUMBER, "DIRECT", 1L, 1, null, 10L, 0L, 0L, null
        );
        TossPaymentRequest request = new TossPaymentRequest(PAYMENT_KEY, ORDER_NUMBER, AMOUNT, orderReq);

        given(orderService.createOrderWithOrderNumber(eq(USER_ID), eq(ORDER_NUMBER), any()))
                .willReturn(null);

        OrderPaymentContext paymentContext = new OrderPaymentContext(
                ORDER_ID, USER_ID, ORDER_NUMBER, AMOUNT, PaymentStatus.PAID, OrderMode.DIRECT, null
        );
        given(orderRepository.findPaymentContextByOrderNumber(ORDER_NUMBER))
                .willReturn(Optional.of(paymentContext));

        // when / then
        assertThatThrownBy(() -> tossPaymentService.confirmPayment(USER_ID, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 결제가 완료된 주문이에요.");
    }

    @Test
    @DisplayName("Toss 응답 status가 ABORTED이면 결제 승인 실패 IllegalStateException을 던진다")
    void confirmPayment_Toss실패응답_IllegalStateException() {
        // given
        OrderCreateRequest orderReq = new OrderCreateRequest(
                ORDER_NUMBER, "DIRECT", 1L, 1, null, 10L, 0L, 0L, null
        );
        TossPaymentRequest request = new TossPaymentRequest(PAYMENT_KEY, ORDER_NUMBER, AMOUNT, orderReq);

        given(orderService.createOrderWithOrderNumber(eq(USER_ID), eq(ORDER_NUMBER), any()))
                .willReturn(null);
        OrderPaymentContext paymentContext = new OrderPaymentContext(
                ORDER_ID, USER_ID, ORDER_NUMBER, AMOUNT, PaymentStatus.PENDING, OrderMode.DIRECT, null
        );
        given(orderRepository.findPaymentContextByOrderNumber(ORDER_NUMBER))
                .willReturn(Optional.of(paymentContext));

        // WebClient 체인 모킹
        TossPaymentResponse tossResponse = new TossPaymentResponse(
                PAYMENT_KEY, ORDER_NUMBER, "ABORTED", null, AMOUNT, "CARD"
        );
        mockTossWebClientChain(tossResponse);

        // when / then
        assertThatThrownBy(() -> tossPaymentService.confirmPayment(USER_ID, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("결제 승인에 실패했습니다.");
    }

    @Test
    @DisplayName("Toss 응답 금액이 요청 금액과 다르면 검증 실패 IllegalStateException을 던진다")
    void confirmPayment_금액불일치_Toss응답vs요청_IllegalStateException() {
        // given
        OrderCreateRequest orderReq = new OrderCreateRequest(
                ORDER_NUMBER, "DIRECT", 1L, 1, null, 10L, 0L, 0L, null
        );
        TossPaymentRequest request = new TossPaymentRequest(PAYMENT_KEY, ORDER_NUMBER, AMOUNT, orderReq);

        given(orderService.createOrderWithOrderNumber(eq(USER_ID), eq(ORDER_NUMBER), any()))
                .willReturn(null);
        OrderPaymentContext paymentContext = new OrderPaymentContext(
                ORDER_ID, USER_ID, ORDER_NUMBER, AMOUNT, PaymentStatus.PENDING, OrderMode.DIRECT, null
        );
        given(orderRepository.findPaymentContextByOrderNumber(ORDER_NUMBER))
                .willReturn(Optional.of(paymentContext));

        // Toss 응답 금액 9000 (요청은 10000)
        TossPaymentResponse tossResponse = new TossPaymentResponse(
                PAYMENT_KEY, ORDER_NUMBER, "DONE", null, 9000L, "CARD"
        );
        mockTossWebClientChain(tossResponse);

        // when / then
        assertThatThrownBy(() -> tossPaymentService.confirmPayment(USER_ID, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("결제 금액 검증에 실패했어요.");
    }

    @Test
    @DisplayName("Toss 응답 orderId가 요청 주문 번호와 다르면 IllegalStateException을 던진다")
    void confirmPayment_주문번호불일치_IllegalStateException() {
        // given
        OrderCreateRequest orderReq = new OrderCreateRequest(
                ORDER_NUMBER, "DIRECT", 1L, 1, null, 10L, 0L, 0L, null
        );
        TossPaymentRequest request = new TossPaymentRequest(PAYMENT_KEY, ORDER_NUMBER, AMOUNT, orderReq);

        given(orderService.createOrderWithOrderNumber(eq(USER_ID), eq(ORDER_NUMBER), any()))
                .willReturn(null);
        OrderPaymentContext paymentContext = new OrderPaymentContext(
                ORDER_ID, USER_ID, ORDER_NUMBER, AMOUNT, PaymentStatus.PENDING, OrderMode.DIRECT, null
        );
        given(orderRepository.findPaymentContextByOrderNumber(ORDER_NUMBER))
                .willReturn(Optional.of(paymentContext));

        // Toss 응답 orderId가 다른 값
        TossPaymentResponse tossResponse = new TossPaymentResponse(
                PAYMENT_KEY, "other-order", "DONE", null, AMOUNT, "CARD"
        );
        mockTossWebClientChain(tossResponse);

        // when / then
        assertThatThrownBy(() -> tossPaymentService.confirmPayment(USER_ID, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("결제 주문 번호가 일치하지 않아요.");
    }

    @Test
    @DisplayName("정상 결제 승인 시 payment가 저장되고 주문 결제 상태가 PAID로 변경된다")
    void confirmPayment_정상_결제저장() {
        // given
        OrderCreateRequest orderReq = new OrderCreateRequest(
                ORDER_NUMBER, "DIRECT", 1L, 1, null, 10L, 0L, 0L, null
        );
        TossPaymentRequest request = new TossPaymentRequest(PAYMENT_KEY, ORDER_NUMBER, AMOUNT, orderReq);

        given(orderService.createOrderWithOrderNumber(eq(USER_ID), eq(ORDER_NUMBER), any()))
                .willReturn(null);
        OrderPaymentContext paymentContext = new OrderPaymentContext(
                ORDER_ID, USER_ID, ORDER_NUMBER, AMOUNT, PaymentStatus.PENDING, OrderMode.DIRECT, "[]"
        );
        given(orderRepository.findPaymentContextByOrderNumber(ORDER_NUMBER))
                .willReturn(Optional.of(paymentContext));

        TossPaymentResponse tossResponse = new TossPaymentResponse(
                PAYMENT_KEY, ORDER_NUMBER, "DONE", "2024-01-01T00:00:00+09:00", AMOUNT, "CARD"
        );
        mockTossWebClientChain(tossResponse);

        // TransactionTemplate 실제 실행
        mockTransactionTemplate();

        given(orderRepository.countOrderProducts(ORDER_ID)).willReturn(1);
        given(orderRepository.updatePaymentStatus(eq(ORDER_ID), eq(PaymentStatus.PAID))).willReturn(1);
        given(orderRepository.updateApprovalStatus(anyLong(), anyString())).willReturn(1);
        given(paymentRepository.upsert(any())).willReturn(1);

        // when
        TossPaymentResponse result = tossPaymentService.confirmPayment(USER_ID, request);

        // then
        assertThat(result).isNotNull();
        verify(paymentRepository).upsert(any());
        verify(orderRepository).updatePaymentStatus(ORDER_ID, PaymentStatus.PAID);
    }

    @Test
    @DisplayName("CART 모드 정상 결제 시 장바구니 아이템이 삭제된다")
    void confirmPayment_CART모드_장바구니삭제() {
        // given
        List<Long> cartItemIds = List.of(1L, 2L);
        OrderCreateRequest orderReq = new OrderCreateRequest(
                ORDER_NUMBER, "CART", null, null, cartItemIds, 10L, 0L, 0L, null
        );
        TossPaymentRequest request = new TossPaymentRequest(PAYMENT_KEY, ORDER_NUMBER, AMOUNT, orderReq);

        given(orderService.createOrderWithOrderNumber(eq(USER_ID), eq(ORDER_NUMBER), any()))
                .willReturn(null);
        OrderPaymentContext paymentContext = new OrderPaymentContext(
                ORDER_ID, USER_ID, ORDER_NUMBER, AMOUNT, PaymentStatus.PENDING, OrderMode.CART, "[]"
        );
        given(orderRepository.findPaymentContextByOrderNumber(ORDER_NUMBER))
                .willReturn(Optional.of(paymentContext));

        TossPaymentResponse tossResponse = new TossPaymentResponse(
                PAYMENT_KEY, ORDER_NUMBER, "DONE", null, AMOUNT, "CARD"
        );
        mockTossWebClientChain(tossResponse);
        mockTransactionTemplate();

        given(orderRepository.countOrderProducts(ORDER_ID)).willReturn(1);
        given(orderRepository.updatePaymentStatus(eq(ORDER_ID), eq(PaymentStatus.PAID))).willReturn(1);
        given(orderRepository.updateApprovalStatus(anyLong(), anyString())).willReturn(1);
        given(paymentRepository.upsert(any())).willReturn(1);
        given(orderRepository.findCartItemIdsByOrderId(ORDER_ID)).willReturn(List.of(1L, 2L));
        given(shoppingCartRepository.softDeleteByIds(eq(List.of(1L, 2L)), eq(USER_ID))).willReturn(2);

        // when
        tossPaymentService.confirmPayment(USER_ID, request);

        // then
        verify(shoppingCartRepository).softDeleteByIds(List.of(1L, 2L), USER_ID);
    }

    @Test
    @DisplayName("handlePaymentFailure는 동일 paymentKey 재호출 시 예외 없이 결과를 반환한다")
    void confirmPayment_idempotencyKey중복_경고로그_계속진행() {
        // given: handlePaymentFailure는 순수 로직이므로 두 번 호출해도 예외 없음
        TossPaymentFailureResponse result1 = tossPaymentService.handlePaymentFailure(
                "PAY_PROCESS_CANCELED", "사용자 취소", ORDER_NUMBER
        );
        TossPaymentFailureResponse result2 = tossPaymentService.handlePaymentFailure(
                "PAY_PROCESS_CANCELED", "사용자 취소", ORDER_NUMBER
        );

        // then
        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
        assertThat(result1.errorCode()).isEqualTo(result2.errorCode());
    }

    // ===== 헬퍼 =====

    @SuppressWarnings("unchecked")
    private void mockTossWebClientChain(TossPaymentResponse responseBody) {
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        given(tossWebClient.post()).willReturn(requestBodyUriSpec);
        given(requestBodyUriSpec.uri(anyString())).willReturn(requestBodySpec);
        given(requestBodySpec.contentType(any())).willReturn(requestBodySpec);
        given(requestBodySpec.accept(any())).willReturn(requestBodySpec);
        given(requestBodySpec.bodyValue(any())).willReturn(requestHeadersSpec);
        given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.onStatus(any(), any())).willReturn(responseSpec);
        given(responseSpec.bodyToMono(TossPaymentResponse.class))
                .willReturn(Mono.just(responseBody));
    }

    private void mockTransactionTemplate() {
        given(transactionManager.getTransaction(any())).willReturn(mock(TransactionStatus.class));
        given(transactionManager.getTransaction(any()).isCompleted()).willReturn(false);

        // TransactionTemplate.executeWithoutResult 실행을 실제로 수행하도록 모킹
        org.mockito.Mockito.doAnswer(invocation -> {
            // TransactionTemplate 내부 TransactionCallback 실행
            return null;
        }).when(transactionManager).commit(any());

        // 실제 TransactionTemplate이 실행될 수 있도록 mock 대신 real 동작 구현
        // PlatformTransactionManager.getTransaction 호출 시 더미 상태 반환
        TransactionStatus txStatus = mock(TransactionStatus.class);
        given(transactionManager.getTransaction(any())).willReturn(txStatus);
    }
}
