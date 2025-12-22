package com.ssafy.fitmarket_be.payment.controller;

import com.ssafy.fitmarket_be.payment.dto.TossPaymentFailureResponse;
import com.ssafy.fitmarket_be.payment.dto.TossPaymentRequest;
import com.ssafy.fitmarket_be.payment.dto.TossPaymentResponse;
import com.ssafy.fitmarket_be.payment.service.TossPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 토스페이먼츠 결제 승인 및 실패 콜백을 처리하는 컨트롤러.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

  private final TossPaymentService paymentService;

  /**
   * 결제 승인 콜백을 처리하고 승인 결과를 반환한다.
   *
   * @param userId  인증된 사용자 식별자
   * @param request 토스페이먼츠 결제 승인 요청 본문
   * @return 승인 완료된 결제 응답
   */
  @PostMapping("/success")
  public ResponseEntity<TossPaymentResponse> paymentSuccess(
      @AuthenticationPrincipal(expression = "id") Long userId,
      @Valid @RequestBody TossPaymentRequest request
  ) {
    TossPaymentResponse response = paymentService.confirmPayment(userId, request);
    return ResponseEntity.ok(response);
  }

//  /**
//   * 결제 실패 콜백을 처리하고 사용자 안내 메시지를 반환한다.
//   *
//   * <p>토스페이먼츠 위젯 v2는 실패 시 {@code errorCode}, {@code errorMessage}로 전달하며,
//   * 이전 규격에서는 {@code code}, {@code message}를 사용한다. 두 경우 모두 처리한다.</p>
//   *
//   * @param code         (v1) 오류 코드
//   * @param message      (v1) 오류 메시지
//   * @param errorCode    (v2) 오류 코드
//   * @param errorMessage (v2) 오류 메시지
//   * @param orderId      상점 주문 번호(선택)
//   * @return 사용자 안내 문구가 포함된 실패 응답
//   */
//  @GetMapping("/fail")
//  public ResponseEntity<TossPaymentFailureResponse> paymentFail(
//      @RequestParam(name = "code", required = false) String code,
//      @RequestParam(name = "message", required = false) String message,
//      @RequestParam(name = "errorCode", required = false) String errorCode,
//      @RequestParam(name = "errorMessage", required = false) String errorMessage,
//      @RequestParam(name = "orderId", required = false) String orderId
//  ) {
//    String resolvedCode = resolveFirstNonEmpty(errorCode, code);
//    String resolvedMessage = resolveFirstNonEmpty(errorMessage, message);
//    TossPaymentFailureResponse response = paymentService.handlePaymentFailure(
//        resolvedCode, resolvedMessage, orderId
//    );
//    return ResponseEntity.badRequest().body(response);
//  }
//
//  private String resolveFirstNonEmpty(String primary, String fallback) {
//    if (StringUtils.hasText(primary)) {
//      return primary;
//    }
//    return fallback;
//  }
}
