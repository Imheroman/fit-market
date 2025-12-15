package com.ssafy.fitmarket_be.payment.service;


import com.ssafy.fitmarket_be.payment.dto.TossPaymentFailureResponse;
import com.ssafy.fitmarket_be.payment.dto.TossPaymentRequest;
import com.ssafy.fitmarket_be.payment.dto.TossPaymentResponse;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 토스페이먼츠 결제 승인 및 실패 처리를 담당하는 서비스.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TossPaymentService {

  private static final String GENERIC_FAIL_GUIDE = "결제가 정상적으로 처리되지 않았어요. 잠시 후 다시 시도해 주세요.";

  private final WebClient tossWebClient;
//  private final OrderRepository orderRepository;

  /**
   * 결제 위젯 v2 결제 승인 처리.
   *
   * @param request 토스페이먼츠 승인 요청 본문
   * @return 토스페이먼츠 결제 승인 응답
   */
  public TossPaymentResponse confirmPayment(TossPaymentRequest request) {
//    // 1. orderId로 주문 조회
//    Order order = orderRepository.findByOrderId(request.orderId())
//        .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
//
//    // 2. 금액 검증 (조작 방지)
//    if (!order.getAmount().equals(request.amount())) {
//      throw new IllegalStateException("결제 금액이 주문 금액과 다릅니다.");
//    }
//
//    // 3. 토스 결제 승인 API 호출
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
        .block(); // 데모용, 실제는 리액티브 or 별도 처리

    if (Objects.isNull(response)) {
      throw new RuntimeException("토스페이먼츠 응답이 비었습니다.");
    }

    if (isSuccessPayment(response)) {
      throw new IllegalStateException("결제 승인에 실패했습니다. status=" + response.status());
    }
//
//    // 4. 주문 상태 업데이트
//    order.markPaid(response.paymentKey());
//    orderRepository.save(order);
//    log.info("TossPayments confirm success. paymentKey={}, orderId={}",
//        resolvedResponse.paymentKey(), resolvedResponse.orderId());

    log.debug("Toss Payments success response: {}", response);

    return response;
  }

  private static boolean isSuccessPayment(TossPaymentResponse response) {
    return !"DONE".equals(response.status());
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
}
