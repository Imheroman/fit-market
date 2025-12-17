package com.ssafy.fitmarket_be.order.controller;

import com.ssafy.fitmarket_be.order.dto.OrderCreateRequest;
import com.ssafy.fitmarket_be.order.dto.OrderCreateResponse;
import com.ssafy.fitmarket_be.order.dto.OrderAddressUpdateRequest;
import com.ssafy.fitmarket_be.order.dto.OrderDetailResponse;
import com.ssafy.fitmarket_be.order.dto.OrderRefundRequest;
import com.ssafy.fitmarket_be.order.dto.OrderStatusUpdateRequest;
import com.ssafy.fitmarket_be.order.dto.OrderSummaryResponse;
import com.ssafy.fitmarket_be.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 주문 생성 엔드포인트를 제공한다.
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;

  /**
   * 사용자의 주문 목록을 조회한다.
   *
   * @param userId 인증된 사용자 식별자
   * @return 주문 목록
   */
  @GetMapping
  public ResponseEntity<List<OrderSummaryResponse>> getOrders(
      @AuthenticationPrincipal(expression = "id") Long userId
  ) {
    List<OrderSummaryResponse> responses = orderService.getOrders(userId);
    return ResponseEntity.ok(responses);
  }

  /**
   * 주문 상세를 조회한다.
   *
   * @param userId      인증된 사용자 식별자
   * @param orderNumber 주문 번호
   * @return 주문 상세
   */
  @GetMapping("/{orderNumber}")
  public ResponseEntity<OrderDetailResponse> getOrderDetail(
      @AuthenticationPrincipal(expression = "id") Long userId,
      @PathVariable String orderNumber
  ) {
    OrderDetailResponse response = orderService.getOrderDetail(userId, orderNumber);
    return ResponseEntity.ok(response);
  }

//  /**
//   * 장바구니/바로구매 요청을 받아 주문을 생성한다.
//   *
//   * @param userId  인증된 사용자 식별자
//   * @param request 주문 생성 요청
//   * @return 생성된 주문 정보
//   */
//  @PostMapping
//  public ResponseEntity<OrderCreateResponse> createOrder(
//      @AuthenticationPrincipal(expression = "id") Long userId,
//      @Valid @RequestBody OrderCreateRequest request
//  ) {
//    OrderCreateResponse response = orderService.createOrder(userId, request);
//    return ResponseEntity.status(HttpStatus.CREATED).body(response);
//  }

  /**
   * 주문 배송지를 수정한다.
   *
   * @param userId      인증된 사용자 식별자
   * @param orderNumber 주문 번호
   * @param request     배송지 변경 요청
   * @return HTTP 200
   */
  @PatchMapping("/{orderNumber}/address")
  public ResponseEntity<Void> updateOrderAddress(
      @AuthenticationPrincipal(expression = "id") Long userId,
      @PathVariable String orderNumber,
      @Valid @RequestBody OrderAddressUpdateRequest request
  ) {
    orderService.updateOrderAddress(userId, orderNumber, request);
    return ResponseEntity.ok().build();
  }

  /**
   * 주문 승인 상태를 변경한다.
   *
   * @param userId      인증된 사용자 식별자
   * @param orderNumber 주문 번호
   * @param request     상태 변경 요청
   * @return HTTP 200
   */
  @PatchMapping("/{orderNumber}/status")
  public ResponseEntity<Void> updateOrderStatus(
      @AuthenticationPrincipal(expression = "id") Long userId,
      @PathVariable String orderNumber,
      @Valid @RequestBody OrderStatusUpdateRequest request
  ) {
    orderService.updateApprovalStatus(userId, orderNumber, request);
    return ResponseEntity.ok().build();
  }

  /**
   * 결제 완료된 주문을 환불 처리한다.
   *
   * @param userId      인증된 사용자 식별자
   * @param orderNumber 주문 번호
   * @param request     환불 요청
   * @return HTTP 200
   */
  @PostMapping("/{orderNumber}/refund")
  public ResponseEntity<Void> refundOrder(
      @AuthenticationPrincipal(expression = "id") Long userId,
      @PathVariable String orderNumber,
      @RequestBody(required = false) OrderRefundRequest request
  ) {
    orderService.refundOrder(userId, orderNumber, request == null ? new OrderRefundRequest(null) : request);
    return ResponseEntity.ok().build();
  }

  /**
   * 주문을 소프트 삭제한다.
   *
   * @param userId      인증된 사용자 식별자
   * @param orderNumber 주문 번호
   * @return HTTP 204
   */
  @DeleteMapping("/{orderNumber}")
  public ResponseEntity<Void> deleteOrder(
      @AuthenticationPrincipal(expression = "id") Long userId,
      @PathVariable String orderNumber
  ) {
    orderService.deleteOrder(userId, orderNumber);
    return ResponseEntity.noContent().build();
  }
}
