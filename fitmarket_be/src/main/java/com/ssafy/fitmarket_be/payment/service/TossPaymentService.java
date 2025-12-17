package com.ssafy.fitmarket_be.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.fitmarket_be.cart.repository.ShoppingCartRepository;
import com.ssafy.fitmarket_be.order.domain.OrderApprovalStatus;
import com.ssafy.fitmarket_be.order.domain.OrderMode;
import com.ssafy.fitmarket_be.order.domain.OrderPaymentContext;
import com.ssafy.fitmarket_be.order.domain.OrderProductEntity;
import com.ssafy.fitmarket_be.order.repository.OrderRepository;
import com.ssafy.fitmarket_be.order.service.OrderService;
import com.ssafy.fitmarket_be.payment.domain.Payment;
import com.ssafy.fitmarket_be.payment.domain.PaymentStatus;
import com.ssafy.fitmarket_be.payment.dto.TossPaymentFailureResponse;
import com.ssafy.fitmarket_be.payment.dto.TossPaymentRequest;
import com.ssafy.fitmarket_be.payment.dto.TossPaymentResponse;
import com.ssafy.fitmarket_be.payment.repository.PaymentRepository;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.util.StringUtils;

/**
 * 토스페이먼츠 결제 승인 및 실패 처리를 담당하는 서비스.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TossPaymentService {

  private static final String GENERIC_FAIL_GUIDE = "결제가 정상적으로 처리되지 않았어요. 잠시 후 다시 시도해 주세요.";

  private final WebClient tossWebClient;
  private final OrderRepository orderRepository;
  private final OrderService orderService;
  private final PaymentRepository paymentRepository;
  private final ShoppingCartRepository shoppingCartRepository;
  private final PlatformTransactionManager transactionManager;
  private final ObjectMapper objectMapper;

  /**
   * 결제 위젯 v2 결제 승인 처리.
   *
   * @param userId  인증된 사용자 식별자
   * @param request 토스페이먼츠 승인 요청 본문
   * @return 토스페이먼츠 결제 승인 응답
   * <p>주문이 아직 생성되지 않은 선결제 흐름에서는 전달받은 주문 요청을 기반으로
   * 주문을 생성한 뒤 결제를 확정한다.</p>
   */
  public TossPaymentResponse confirmPayment(Long userId, TossPaymentRequest request) {
//    OrderPaymentContext paymentContext = orderRepository.findPaymentContextByOrderNumber(request.orderId())
//        .orElseGet(() -> createOrderAfterPayment(userId, request));
    OrderPaymentContext paymentContext = createOrderAfterPayment(userId, request);
    log.debug("payment context: {}", paymentContext);
    validatePaymentStatus(paymentContext);
    validateAmountAgainstOrder(request, paymentContext);

    TossPaymentResponse response = requestPaymentApproval(request);
    ensureSuccessPayment(response);
    validatePaymentAmounts(request, response);

    persistPaymentSuccess(paymentContext, request, response);
    log.debug("Toss Payments success response: {}", response);
    return response;
  }

  private static boolean isSuccessPayment(TossPaymentResponse response) {
    return "DONE".equalsIgnoreCase(response.status());
  }

  /**
   * 토스페이먼츠 결제 실패 시 프런트에 전달할 메시지를 생성한다.
   *
   * @param errorCode   토스페이먼츠 오류 코드
   * @param errorReason 토스페이먼츠 오류 메시지
   * @param orderId     상점 주문 번호(선택)
   * @return 사용자 안내 문구가 포함된 실패 응답 DTO
   */
  public TossPaymentFailureResponse handlePaymentFailure(
      String errorCode,
      String errorReason,
      String orderId
  ) {
    log.warn("TossPayments payment failed. orderId={}, code={}, message={}",
        orderId, errorCode, errorReason);

    String resolvedCode = Objects.toString(errorCode, "UNKNOWN_ERROR");
    String resolvedReason = Objects.toString(errorReason, "결제 실패 원인을 불러오지 못했어요.");
    return new TossPaymentFailureResponse(orderId, resolvedCode, resolvedReason, GENERIC_FAIL_GUIDE);
  }

  private OrderPaymentContext createOrderAfterPayment(Long userId, TossPaymentRequest request) {
    if (userId == null) {
      throw new IllegalStateException("로그인이 만료되었어요. 다시 로그인 후 결제를 진행해 주세요.");
    }

    if (request.orderRequest() == null) {
      throw new IllegalStateException("결제 내역과 연결할 주문 정보를 찾지 못했어요. 다시 시도해 주세요.");
    }
    orderService.createOrderWithOrderNumber(userId, request.orderId(), request.orderRequest());
    return orderRepository.findPaymentContextByOrderNumber(request.orderId())
        .orElseThrow(() -> new IllegalStateException("주문 정보를 불러오지 못했어요. 결제를 다시 진행해 주세요."));
  }

  private void validateAmountAgainstOrder(TossPaymentRequest request, OrderPaymentContext paymentContext) {
    if (!Objects.equals(paymentContext.totalAmount(), request.amount())) {
      throw new IllegalStateException("결제 금액이 주문 정보와 달라요. 다시 결제를 시도해 주세요.");
    }
  }

  private void validatePaymentStatus(OrderPaymentContext paymentContext) {
    if (paymentContext.paymentStatus() == PaymentStatus.PAID) {
      throw new IllegalStateException("이미 결제가 완료된 주문이에요.");
    }
  }

  private TossPaymentResponse requestPaymentApproval(TossPaymentRequest request) {
    log.debug("payment request: {}", request);
    TossPaymentResponse response = tossWebClient.post()
        .uri("/v1/payments/confirm")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .retrieve()
        .onStatus(HttpStatusCode::isError, clientResponse ->
            clientResponse.bodyToMono(String.class)
                .map(body -> {
                  log.error("TossPayments confirm failed. status={}, body={}",
                      clientResponse.statusCode(), body);
                  return new IllegalStateException(GENERIC_FAIL_GUIDE);
                })
        )
        .bodyToMono(TossPaymentResponse.class)
        .block();

    if (Objects.isNull(response)) {
      throw new IllegalStateException("토스페이먼츠 응답이 비었어요. 다시 시도해 주세요.");
    }
    return response;
  }

  private void ensureSuccessPayment(TossPaymentResponse response) {
    if (!isSuccessPayment(response)) {
      throw new IllegalStateException("결제 승인에 실패했습니다. 잠시 후 다시 시도해 주세요.");
    }
  }

  private void validatePaymentAmounts(TossPaymentRequest request, TossPaymentResponse response) {
    if (!Objects.equals(request.amount(), response.totalAmount())) {
      throw new IllegalStateException("결제 금액 검증에 실패했어요. 다시 시도해 주세요.");
    }
  }

  private void persistPaymentSuccess(
      OrderPaymentContext paymentContext,
      TossPaymentRequest request,
      TossPaymentResponse response
  ) {
    if (!paymentContext.orderNumber().equals(response.orderId())) {
      throw new IllegalStateException("결제 주문 번호가 일치하지 않아요. 다시 확인해 주세요.");
    }
    if (!Objects.equals(response.totalAmount(), paymentContext.totalAmount())) {
      throw new IllegalStateException("결제 승인 금액이 주문 정보와 달라요. 다시 확인해 주세요.");
    }

    TransactionTemplate template = new TransactionTemplate(transactionManager);
    template.executeWithoutResult(status -> {
      ensureOrderProducts(paymentContext);
      int paymentUpdated = orderRepository.updatePaymentStatus(paymentContext.orderId(), PaymentStatus.PAID);
      if (paymentUpdated <= 0) {
        throw new IllegalStateException("주문 결제 상태를 갱신하지 못했어요.");
      }
      int approvalUpdated = orderRepository.updateApprovalStatus(paymentContext.orderId(),
          OrderApprovalStatus.PENDING_APPROVAL.dbValue());
      if (approvalUpdated <= 0) {
        throw new IllegalStateException("주문 승인 상태를 갱신하지 못했어요.");
      }

      Payment payment = Payment.builder()
          .orderId(paymentContext.orderId())
          .paymentKey(request.paymentKey())
          .provider("toss")
          .method(response.method())
          .status(PaymentStatus.PAID)
          .amount(response.totalAmount())
          .approvedAt(resolveApprovedAt(response))
          .rawResponse(writeRawResponse(response))
          .build();
      paymentRepository.upsert(payment);

      if (paymentContext.orderMode() == OrderMode.CART) {
        deletePurchasedCartItems(paymentContext);
      }
    });
  }

  private void ensureOrderProducts(OrderPaymentContext paymentContext) {
    int existingProducts = orderRepository.countOrderProducts(paymentContext.orderId());
    if (existingProducts > 0) {
      return;
    }
    if (!StringUtils.hasText(paymentContext.itemsSnapshot())) {
      throw new IllegalStateException("주문 상품 정보를 불러오지 못했어요. 잠시 후 다시 시도해 주세요.");
    }
    List<OrderProductEntity> products = parseOrderProducts(paymentContext.itemsSnapshot());
    int insertedProducts = orderRepository.insertOrderProducts(paymentContext.orderId(), products);
    if (insertedProducts != products.size()) {
      throw new IllegalStateException("주문 상품 정보를 저장하지 못했어요. 잠시 후 다시 시도해 주세요.");
    }
    orderRepository.clearItemsSnapshot(paymentContext.orderId());
  }

  private List<OrderProductEntity> parseOrderProducts(String snapshot) {
    try {
      return objectMapper.readValue(snapshot, new TypeReference<>() {
      });
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("주문 상품 정보를 불러오지 못했어요. 잠시 후 다시 시도해 주세요.", e);
    }
  }

  private void deletePurchasedCartItems(OrderPaymentContext paymentContext) {
    List<Long> cartItemIds = orderRepository.findCartItemIdsByOrderId(paymentContext.orderId());
    if (cartItemIds.isEmpty()) {
      return;
    }
    int deleted = shoppingCartRepository.softDeleteByIds(cartItemIds, paymentContext.userId());
    if (deleted != cartItemIds.size()) {
      log.info("cart items already removed or partially removed. orderId={}, deleted={}, expected={}",
          paymentContext.orderId(), deleted, cartItemIds.size());
    }
  }

  private String writeRawResponse(TossPaymentResponse response) {
    try {
      return objectMapper.writeValueAsString(response);
    } catch (JsonProcessingException e) {
      log.warn("failed to serialize toss payment response", e);
      return null;
    }
  }

  private java.time.LocalDateTime resolveApprovedAt(TossPaymentResponse response) {
    if (response.approvedAt() == null) {
      return null;
    }
    try {
      return OffsetDateTime.parse(response.approvedAt()).toLocalDateTime();
    } catch (DateTimeParseException e) {
      log.warn("failed to parse approvedAt: {}", response.approvedAt(), e);
      return null;
    }
  }
}
