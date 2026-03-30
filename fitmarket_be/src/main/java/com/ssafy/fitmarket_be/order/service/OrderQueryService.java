package com.ssafy.fitmarket_be.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.fitmarket_be.global.exception.BusinessException;
import com.ssafy.fitmarket_be.order.domain.OrderApprovalStatus;
import com.ssafy.fitmarket_be.order.domain.OrderAddressSnapshot;
import com.ssafy.fitmarket_be.order.domain.OrderMode;
import com.ssafy.fitmarket_be.order.domain.OrderProductEntity;
import com.ssafy.fitmarket_be.order.domain.OrderReturnExchangeEntity;
import com.ssafy.fitmarket_be.order.domain.OrderSearchPeriod;
import com.ssafy.fitmarket_be.order.domain.OrderView;
import com.ssafy.fitmarket_be.order.dto.OrderDetailResponse;
import com.ssafy.fitmarket_be.order.dto.OrderItemResponse;
import com.ssafy.fitmarket_be.order.dto.OrderRefundEligibilityResponse;
import com.ssafy.fitmarket_be.order.dto.OrderReturnExchangeStatusResponse;
import com.ssafy.fitmarket_be.order.dto.OrderSummaryResponse;
import com.ssafy.fitmarket_be.order.repository.OrderRepository;
import com.ssafy.fitmarket_be.payment.domain.PaymentStatus;
import com.ssafy.fitmarket_be.payment.repository.PaymentRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 주문 조회 유스케이스를 담당한다 (CQRS Query 측).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderQueryService {

  private static final int REFUND_AVAILABLE_DAYS = 3;
  private static final int RETURN_EXCHANGE_AVAILABLE_DAYS = 7;
  private static final String CLAIM_ALREADY_REQUESTED_MESSAGE = "이미 환불/반품/교환 요청이 접수된 주문이에요.";

  private final OrderRepository orderRepository;
  private final PaymentRepository paymentRepository;
  private final ObjectMapper objectMapper;

  @Transactional(readOnly = true)
  public List<OrderSummaryResponse> getOrders(Long userId, OrderSearchPeriod period) {
    LocalDateTime startDate = period.resolveStartDate(LocalDateTime.now());
    List<OrderView> orders = orderRepository.findOrdersByUserIdAndStartDate(userId, startDate);
    if (orders.isEmpty()) {
      return List.of();
    }
    Map<Long, List<OrderProductEntity>> productsByOrderId = findOrderProductsGrouped(orders);

    return orders.stream()
        .map(order -> toSummary(order, productsByOrderId.getOrDefault(order.getId(), List.of())))
        .toList();
  }

  @Transactional(readOnly = true)
  public OrderDetailResponse getOrderDetail(Long userId, String orderNumber) {
    OrderView order = findOwnedOrder(userId, orderNumber);
    Map<Long, List<OrderProductEntity>> productsByOrderId =
        findOrderProductsGrouped(List.of(order));
    List<OrderProductEntity> products = productsByOrderId.getOrDefault(order.getId(), List.of());
    String orderName = resolveOrderName(products);
    OrderReturnExchangeEntity returnExchange = orderRepository.findOrderReturnExchangeByOrderId(order.getId())
        .orElse(null);
    boolean hasReturnExchangeRequest = returnExchange != null;
    RefundEligibility refundEligibility = evaluateRefundEligibility(order, hasReturnExchangeRequest);
    ReturnExchangeEligibility returnEligibility =
        evaluateReturnExchangeEligibility(order, hasReturnExchangeRequest);
    ReturnExchangeEligibility exchangeEligibility =
        evaluateReturnExchangeEligibility(order, hasReturnExchangeRequest);

    return new OrderDetailResponse(
        order.getOrderNumber(),
        order.getOrderMode(),
        order.getApprovalStatus(),
        order.getPaymentStatus(),
        orderName,
        order.getTotalAmount(),
        order.getMerchandiseAmount(),
        order.getShippingFee(),
        order.getDiscountAmount(),
        refundEligibility.eligible(),
        returnEligibility.eligible(),
        exchangeEligibility.eligible(),
        returnExchange == null ? null : toReturnExchangeStatusResponse(returnExchange),
        order.getOrderDate(),
        order.getComment(),
        parseSnapshot(order.getAddressSnapshot()),
        products.stream()
            .map(this::toItemResponse)
            .toList()
    );
  }

  @Transactional(readOnly = true)
  public OrderRefundEligibilityResponse getRefundEligibility(Long userId, String orderNumber) {
    OrderView order = findOwnedOrder(userId, orderNumber);
    boolean hasReturnExchangeRequest = hasReturnExchangeRequest(order.getId());
    RefundEligibility eligibility = evaluateRefundEligibility(order, hasReturnExchangeRequest);
    return new OrderRefundEligibilityResponse(eligibility.eligible(), eligibility.message());
  }

  // ---- package-private: OrderRefundService에서도 사용 ----

  RefundEligibility evaluateRefundEligibility(OrderView order, boolean hasReturnExchangeRequest) {
    if (hasReturnExchangeRequest) {
      return new RefundEligibility(false, CLAIM_ALREADY_REQUESTED_MESSAGE);
    }
    OrderApprovalStatus status = OrderApprovalStatus.from(order.getApprovalStatus());
    if (status.isTerminal()) {
      return new RefundEligibility(false, "이미 종료된 주문이라 환불할 수 없어요.");
    }
    if (status == OrderApprovalStatus.DELIVERED) {
      return new RefundEligibility(false, "배송 완료된 주문은 반품/교환을 이용해 주세요.");
    }
    if (status.isShippingOrLater()) {
      return new RefundEligibility(false, "배송이 시작된 주문은 환불할 수 없어요.");
    }
    if (order.getPaymentStatus() != PaymentStatus.PAID) {
      return new RefundEligibility(false, "결제 완료된 주문만 환불할 수 있어요.");
    }

    LocalDateTime approvedAt = paymentRepository.findApprovedAtByOrderId(order.getId())
        .orElseThrow(() -> new BusinessException("결제 승인 시점을 확인하지 못했어요. 잠시 후 다시 시도해 주세요.", HttpStatus.INTERNAL_SERVER_ERROR));
    LocalDateTime refundDeadline = approvedAt.plusDays(REFUND_AVAILABLE_DAYS);
    if (refundDeadline.isBefore(LocalDateTime.now())) {
      return new RefundEligibility(false, "결제 후 3일이 지나 환불할 수 없어요.");
    }
    return new RefundEligibility(true, "환불이 가능해요.");
  }

  ReturnExchangeEligibility evaluateReturnExchangeEligibility(
      OrderView order,
      boolean hasReturnExchangeRequest
  ) {
    if (hasReturnExchangeRequest) {
      return new ReturnExchangeEligibility(false, CLAIM_ALREADY_REQUESTED_MESSAGE);
    }
    OrderApprovalStatus status = OrderApprovalStatus.from(order.getApprovalStatus());
    if (status == OrderApprovalStatus.CANCELLED || status == OrderApprovalStatus.REJECTED) {
      return new ReturnExchangeEligibility(false, "종료된 주문은 반품/교환할 수 없어요.");
    }
    if (status != OrderApprovalStatus.DELIVERED) {
      return new ReturnExchangeEligibility(false, "배송 완료 후에 반품/교환을 요청할 수 있어요.");
    }

    LocalDateTime baseDate = resolveReturnExchangeBaseDate(order);
    LocalDateTime deadline = baseDate.plusDays(RETURN_EXCHANGE_AVAILABLE_DAYS);
    if (deadline.isBefore(LocalDateTime.now())) {
      return new ReturnExchangeEligibility(false, "배송 완료 후 7일이 지나 반품/교환할 수 없어요.");
    }
    return new ReturnExchangeEligibility(true, "반품/교환이 가능해요.");
  }

  OrderView findOwnedOrder(Long userId, String orderNumber) {
    return orderRepository.findOrderByNumberAndUserId(orderNumber, userId)
        .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없어요. 주문 번호를 다시 확인해 주세요."));
  }

  boolean hasReturnExchangeRequest(Long orderId) {
    return orderRepository.countOrderReturnExchanges(orderId) > 0;
  }

  String resolveOrderName(List<OrderProductEntity> orderProducts) {
    if (orderProducts.isEmpty()) {
      return "FitMarket 주문";
    }
    String firstName = orderProducts.get(0).getProductName();
    if (orderProducts.size() == 1) {
      return firstName;
    }
    return firstName + " 외 " + (orderProducts.size() - 1) + "건";
  }

  // ---- records ----

  record RefundEligibility(boolean eligible, String message) {
  }

  record ReturnExchangeEligibility(boolean eligible, String message) {
  }

  // ---- private helpers ----

  private OrderReturnExchangeStatusResponse toReturnExchangeStatusResponse(OrderReturnExchangeEntity entity) {
    return new OrderReturnExchangeStatusResponse(
        entity.getType(),
        entity.getStatus(),
        entity.getRequestedAt(),
        entity.getProcessedAt()
    );
  }

  private Map<Long, List<OrderProductEntity>> findOrderProductsGrouped(List<OrderView> orders) {
    List<Long> orderIds = orders.stream()
        .map(OrderView::getId)
        .toList();
    if (orderIds.isEmpty()) {
      return Collections.emptyMap();
    }
    return orderRepository.findOrderProductsByOrderIds(orderIds).stream()
        .collect(Collectors.groupingBy(OrderProductEntity::getOrderId));
  }

  private OrderSummaryResponse toSummary(OrderView order, List<OrderProductEntity> products) {
    String orderName = resolveOrderName(products);
    return new OrderSummaryResponse(
        order.getOrderNumber(),
        orderName,
        order.getOrderMode(),
        order.getApprovalStatus(),
        order.getPaymentStatus(),
        order.getTotalAmount(),
        order.getMerchandiseAmount(),
        order.getShippingFee(),
        order.getDiscountAmount(),
        products.size(),
        order.getOrderDate()
    );
  }

  private OrderItemResponse toItemResponse(OrderProductEntity product) {
    return new OrderItemResponse(
        product.getProductId(),
        product.getProductName(),
        product.getQuantity(),
        product.getUnitPrice(),
        product.getTotalPrice()
    );
  }

  private LocalDateTime resolveReturnExchangeBaseDate(OrderView order) {
    if (order.getDueDate() != null) {
      return order.getDueDate();
    }
    if (order.getShipDate() != null) {
      return order.getShipDate();
    }
    return order.getOrderDate();
  }

  private OrderAddressSnapshot parseSnapshot(String snapshotJson) {
    try {
      return objectMapper.readValue(snapshotJson, OrderAddressSnapshot.class);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("배송지 정보를 불러오지 못했어요. 잠시 후 다시 시도해 주세요.", e);
    }
  }
}
