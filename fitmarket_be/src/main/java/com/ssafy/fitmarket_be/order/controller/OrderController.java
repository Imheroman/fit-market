package com.ssafy.fitmarket_be.order.controller;

import com.ssafy.fitmarket_be.global.common.ApiResponse;
import com.ssafy.fitmarket_be.order.dto.OrderAddressUpdateRequest;
import com.ssafy.fitmarket_be.order.dto.OrderDetailResponse;
import com.ssafy.fitmarket_be.order.dto.OrderRefundEligibilityResponse;
import com.ssafy.fitmarket_be.order.dto.OrderRefundRequest;
import com.ssafy.fitmarket_be.order.dto.OrderStatusUpdateRequest;
import com.ssafy.fitmarket_be.order.dto.OrderReturnExchangeRequest;
import com.ssafy.fitmarket_be.order.dto.OrderReturnExchangeResponse;
import com.ssafy.fitmarket_be.order.dto.OrderSummaryResponse;
import com.ssafy.fitmarket_be.order.domain.OrderSearchPeriod;
import com.ssafy.fitmarket_be.order.service.OrderQueryService;
import com.ssafy.fitmarket_be.order.service.OrderRefundService;
import com.ssafy.fitmarket_be.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 주문 조회, 상태 변경, 환불/반품/교환 엔드포인트를 제공한다.
 * 주문 생성은 결제 승인 콜백(POST /payments/success)에서 처리된다.
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;
  private final OrderQueryService orderQueryService;
  private final OrderRefundService orderRefundService;

  /**
   * 사용자의 주문 목록을 조회한다.
   *
   * @param userId 인증된 사용자 식별자
   * @param period 조회 기간 (ALL, 1M, 3M, 6M, 1Y)
   * @return 주문 목록
   */
  @GetMapping
  public ResponseEntity<ApiResponse<List<OrderSummaryResponse>>> getOrders(
      @AuthenticationPrincipal(expression = "id") Long userId,
      @RequestParam(name = "period", required = false) String period
  ) {
    OrderSearchPeriod searchPeriod = OrderSearchPeriod.from(period);
    List<OrderSummaryResponse> responses = orderQueryService.getOrders(userId, searchPeriod);
    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(responses));
  }

  /**
   * 주문 상세를 조회한다.
   *
   * @param userId      인증된 사용자 식별자
   * @param orderNumber 주문 번호
   * @return 주문 상세
   */
  @GetMapping("/{orderNumber}")
  public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(
      @AuthenticationPrincipal(expression = "id") Long userId,
      @PathVariable String orderNumber
  ) {
    OrderDetailResponse response = orderQueryService.getOrderDetail(userId, orderNumber);
    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
  }

  /**
   * 주문 배송지를 수정한다.
   *
   * @param userId      인증된 사용자 식별자
   * @param orderNumber 주문 번호
   * @param request     배송지 변경 요청
   * @return HTTP 200
   */
  @PatchMapping("/{orderNumber}/address")
  public ResponseEntity<ApiResponse<Void>> updateOrderAddress(
      @AuthenticationPrincipal(expression = "id") Long userId,
      @PathVariable String orderNumber,
      @Valid @RequestBody OrderAddressUpdateRequest request
  ) {
    orderService.updateOrderAddress(userId, orderNumber, request);
    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(null));
  }

  /**
   * 주문을 취소한다.
   *
   * @param userId      인증된 사용자 식별자
   * @param orderNumber 주문 번호
   * @return HTTP 204
   */
  @PostMapping("/{orderNumber}/cancel")
  public ResponseEntity<ApiResponse<Void>> cancelOrder(
      @AuthenticationPrincipal(expression = "id") Long userId,
      @PathVariable String orderNumber
  ) {
    orderService.cancelOrder(userId, orderNumber);
    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(null));
  }

  /**
   * 환불 가능 여부를 조회한다.
   *
   * @param userId      인증된 사용자 식별자
   * @param orderNumber 주문 번호
   * @return 환불 가능 여부
   */
  @GetMapping("/{orderNumber}/refund/eligibility")
  public ResponseEntity<ApiResponse<OrderRefundEligibilityResponse>> getRefundEligibility(
      @AuthenticationPrincipal(expression = "id") Long userId,
      @PathVariable String orderNumber
  ) {
    OrderRefundEligibilityResponse response = orderQueryService.getRefundEligibility(userId, orderNumber);
    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
  }

  /**
   * 결제 완료된 주문을 환불 처리한다.
   *
   * @param userId      인증된 사용자 식별자
   * @param orderNumber 주문 번호
   * @param request     환불 요청
   * @return 환불 처리 응답
   */
  @PostMapping("/{orderNumber}/refund")
  public ResponseEntity<ApiResponse<OrderRefundEligibilityResponse>> refundOrder(
      @AuthenticationPrincipal(expression = "id") Long userId,
      @PathVariable String orderNumber,
      @RequestBody(required = false) OrderRefundRequest request
  ) {
    OrderRefundEligibilityResponse response = orderRefundService.refundOrder(
        userId,
        orderNumber,
        request == null ? new OrderRefundRequest(null, null) : request
    );
    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
  }

  /**
   * 반품/교환을 요청한다.
   *
   * @param userId      인증된 사용자 식별자
   * @param orderNumber 주문 번호
   * @param request     반품/교환 요청
   * @return 반품/교환 가능 여부
   */
  @PostMapping("/{orderNumber}/return-exchange")
  public ResponseEntity<ApiResponse<OrderReturnExchangeResponse>> requestReturnOrExchange(
      @AuthenticationPrincipal(expression = "id") Long userId,
      @PathVariable String orderNumber,
      @Valid @RequestBody OrderReturnExchangeRequest request
  ) {
    OrderReturnExchangeResponse response = orderRefundService.requestReturnOrExchange(userId, orderNumber, request);
    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
  }

  /**
   * 주문 상태를 변경한다. (판매자용)
   *
   * @param userId      인증된 사용자 식별자
   * @param orderNumber 주문 번호
   * @param request     상태 변경 요청
   * @return HTTP 200
   */
  @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
  @PatchMapping("/{orderNumber}/status")
  public ResponseEntity<ApiResponse<Void>> updateOrderStatus(
      @AuthenticationPrincipal(expression = "id") Long userId,
      @PathVariable String orderNumber,
      @Valid @RequestBody OrderStatusUpdateRequest request
  ) {
    orderService.updateApprovalStatus(userId, orderNumber, request);
    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(null));
  }

  /**
   * 주문을 소프트 삭제한다.
   *
   * @param userId      인증된 사용자 식별자
   * @param orderNumber 주문 번호
   * @return HTTP 204
   */
  @DeleteMapping("/{orderNumber}")
  public ResponseEntity<ApiResponse<Void>> deleteOrder(
      @AuthenticationPrincipal(expression = "id") Long userId,
      @PathVariable String orderNumber
  ) {
    orderService.deleteOrder(userId, orderNumber);
    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(null));
  }
}
