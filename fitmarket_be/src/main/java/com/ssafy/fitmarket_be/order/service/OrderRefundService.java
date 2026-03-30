package com.ssafy.fitmarket_be.order.service;

import com.ssafy.fitmarket_be.global.exception.BusinessException;
import com.ssafy.fitmarket_be.order.domain.OrderApprovalStatus;
import com.ssafy.fitmarket_be.order.domain.OrderProductEntity;
import com.ssafy.fitmarket_be.order.domain.OrderReturnExchangeEntity;
import com.ssafy.fitmarket_be.order.domain.OrderReturnExchangeReason;
import com.ssafy.fitmarket_be.order.domain.OrderReturnExchangeType;
import com.ssafy.fitmarket_be.order.domain.OrderView;
import com.ssafy.fitmarket_be.order.dto.OrderRefundEligibilityResponse;
import com.ssafy.fitmarket_be.order.dto.OrderRefundRequest;
import com.ssafy.fitmarket_be.order.dto.OrderReturnExchangeRequest;
import com.ssafy.fitmarket_be.order.dto.OrderReturnExchangeResponse;
import com.ssafy.fitmarket_be.order.repository.OrderRepository;
import com.ssafy.fitmarket_be.payment.domain.PaymentStatus;
import com.ssafy.fitmarket_be.payment.repository.PaymentRepository;
import com.ssafy.fitmarket_be.product.repository.ProductMapper;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 주문 환불/반품/교환 유스케이스를 담당한다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderRefundService {

  private static final String CLAIM_ALREADY_REQUESTED_MESSAGE = "이미 환불/반품/교환 요청이 접수된 주문이에요.";

  private final OrderRepository orderRepository;
  private final PaymentRepository paymentRepository;
  private final ProductMapper productMapper;
  private final OrderQueryService orderQueryService;

  @Transactional
  public OrderRefundEligibilityResponse refundOrder(Long userId, String orderNumber, OrderRefundRequest request) {
    OrderView order = orderQueryService.findOwnedOrder(userId, orderNumber);
    boolean hasReturnExchangeRequest = orderQueryService.hasReturnExchangeRequest(order.getId());
    OrderQueryService.RefundEligibility eligibility = orderQueryService.evaluateRefundEligibility(order, hasReturnExchangeRequest);
    if (!eligibility.eligible()) {
      throw new IllegalArgumentException(eligibility.message());
    }

    int updatedOrder = orderRepository.updatePaymentStatus(order.getId(), PaymentStatus.REFUNDED);
    if (updatedOrder <= 0) {
      throw new BusinessException("주문 결제 상태를 변경하지 못했어요.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
    int updatedPayment = paymentRepository.updateStatusByOrderId(order.getId(), PaymentStatus.REFUNDED);
    if (updatedPayment == 0) {
      log.info("payment row not found while refunding order {}", orderNumber);
    }
    orderRepository.updateApprovalStatus(order.getId(), OrderApprovalStatus.CANCELLED.dbValue());
    restoreStock(order.getId());

    saveReturnExchangeRequest(
        order.getId(),
        OrderReturnExchangeType.REFUND,
        resolveReason(request.reason()),
        resolveDetail(request.detail())
    );

    log.info("refund requested for order {} by user {} reason={}", orderNumber, userId,
        Objects.toString(request.reason(), "N/A"));

    return new OrderRefundEligibilityResponse(true, "환불 요청이 정상적으로 접수됐어요.");
  }

  @Transactional
  public OrderReturnExchangeResponse requestReturnOrExchange(
      Long userId,
      String orderNumber,
      OrderReturnExchangeRequest request
  ) {
    OrderView order = orderQueryService.findOwnedOrder(userId, orderNumber);
    boolean hasReturnExchangeRequest = orderQueryService.hasReturnExchangeRequest(order.getId());
    OrderQueryService.ReturnExchangeEligibility eligibility =
        orderQueryService.evaluateReturnExchangeEligibility(order, hasReturnExchangeRequest);
    if (!eligibility.eligible()) {
      return new OrderReturnExchangeResponse(false, eligibility.message(), request.type());
    }

    saveReturnExchangeRequest(
        order.getId(),
        request.type(),
        request.reason(),
        request.detail()
    );

    log.info("return/exchange requested for order {} by user {} type={} reason={} detail={}",
        orderNumber, userId, request.type(), request.reason(), request.detail());

    return new OrderReturnExchangeResponse(true, "요청이 접수됐어요. 빠르게 확인해 볼게요.", request.type());
  }

  private void restoreStock(Long orderId) {
    List<OrderProductEntity> items = orderRepository.findOrderProductsByOrderIds(List.of(orderId))
        .stream()
        .filter(item -> item.getOrderId().equals(orderId))
        .toList();
    for (OrderProductEntity item : items) {
      productMapper.increaseStock(item.getProductId(), item.getQuantity());
    }
  }

  private void saveReturnExchangeRequest(
      Long orderId,
      OrderReturnExchangeType type,
      OrderReturnExchangeReason reason,
      String detail
  ) {
    OrderReturnExchangeEntity request = OrderReturnExchangeEntity.builder()
        .orderId(orderId)
        .type(type)
        .reason(reason)
        .detail(detail)
        .build();
    try {
      int inserted = orderRepository.insertOrderReturnExchange(request);
      if (inserted <= 0) {
        throw new IllegalStateException("반품/교환/환불 요청을 저장하지 못했어요. 다시 시도해 주세요.");
      }
    } catch (DuplicateKeyException e) {
      throw new IllegalArgumentException(CLAIM_ALREADY_REQUESTED_MESSAGE, e);
    }
  }

  private OrderReturnExchangeReason resolveReason(OrderReturnExchangeReason reason) {
    return reason == null ? OrderReturnExchangeReason.OTHER : reason;
  }

  private String resolveDetail(String detail) {
    if (!StringUtils.hasText(detail)) {
      return "사유 미입력";
    }
    return detail.trim();
  }
}
