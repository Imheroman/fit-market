package com.ssafy.fitmarket_be.seller.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.fitmarket_be.order.domain.OrderAddressSnapshot;
import com.ssafy.fitmarket_be.order.domain.OrderApprovalStatus;
import com.ssafy.fitmarket_be.order.domain.OrderProductEntity;
import com.ssafy.fitmarket_be.order.domain.OrderSearchPeriod;
import com.ssafy.fitmarket_be.order.domain.OrderView;
import com.ssafy.fitmarket_be.order.dto.OrderDetailResponse;
import com.ssafy.fitmarket_be.order.dto.OrderItemResponse;
import com.ssafy.fitmarket_be.order.dto.OrderStatusUpdateRequest;
import com.ssafy.fitmarket_be.order.dto.OrderSummaryResponse;
import com.ssafy.fitmarket_be.seller.api.SellerOrderService;
import com.ssafy.fitmarket_be.seller.infrastructure.mybatis.SellerOrderMapper;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class SellerOrderServiceImpl implements SellerOrderService {

  private final SellerOrderMapper sellerOrderMapper;
  private final ObjectMapper objectMapper;

  @Override
  @Transactional(readOnly = true)
  public List<OrderSummaryResponse> getOrders(Long sellerId, OrderSearchPeriod period) {
    LocalDateTime startDate = period.resolveStartDate(LocalDateTime.now());
    List<OrderView> orders = sellerOrderMapper.findOrdersBySellerIdAndStartDate(sellerId, startDate);
    if (orders.isEmpty()) {
      return List.of();
    }

    Map<Long, List<OrderProductEntity>> productsByOrderId =
        findOrderProductsGrouped(orders, sellerId);

    return orders.stream()
        .map(order -> toSummary(order, productsByOrderId.getOrDefault(order.getId(), List.of())))
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public OrderDetailResponse getOrderDetail(Long sellerId, String orderNumber) {
    OrderView order = findOwnedOrder(sellerId, orderNumber);
    List<OrderProductEntity> items = sellerOrderMapper.findOrderProductsByOrderIdsAndSellerId(
        List.of(order.getId()),
        sellerId
    );

    String orderName = resolveOrderName(items);
    long merchandiseAmount = calculateMerchandiseAmount(items);
    long totalAmount = merchandiseAmount;

    return new OrderDetailResponse(
        order.getOrderNumber(),
        order.getOrderMode(),
        order.getApprovalStatus(),
        order.getPaymentStatus(),
        orderName,
        totalAmount,
        merchandiseAmount,
        0L,
        0L,
        false,
        false,
        false,
        null,
        order.getOrderDate(),
        order.getComment(),
        parseSnapshot(order.getAddressSnapshot()),
        items.stream()
            .map(this::toItemResponse)
            .toList()
    );
  }

  @Override
  @Transactional
  public void updateOrderStatus(Long sellerId, String orderNumber, OrderStatusUpdateRequest request) {
    OrderView order = findOwnedOrder(sellerId, orderNumber);
    OrderApprovalStatus newStatus = OrderApprovalStatus.from(request.approvalStatus());
    OrderApprovalStatus currentStatus = OrderApprovalStatus.from(order.getApprovalStatus());
    if (currentStatus == newStatus) {
      return;
    }
    if (!isSellerUpdatable(newStatus)) {
      throw new IllegalArgumentException("판매자는 배송/거절 상태만 변경할 수 있어요.");
    }
    if (!isAllowedTransition(currentStatus, newStatus)) {
      throw new IllegalStateException("승인 완료에서 배송 시작/거절이 가능하고, 배송 중에만 배송 완료로 변경할 수 있어요.");
    }
    int updated = sellerOrderMapper.updateApprovalStatus(order.getId(), newStatus.dbValue());
    if (updated <= 0) {
      throw new IllegalStateException("주문 상태를 변경하지 못했어요. 잠시 후 다시 시도해 주세요.");
    }
  }

  private Map<Long, List<OrderProductEntity>> findOrderProductsGrouped(
      List<OrderView> orders,
      Long sellerId
  ) {
    List<Long> orderIds = orders.stream()
        .map(OrderView::getId)
        .toList();
    if (orderIds.isEmpty()) {
      return Collections.emptyMap();
    }
    return sellerOrderMapper.findOrderProductsByOrderIdsAndSellerId(orderIds, sellerId).stream()
        .collect(Collectors.groupingBy(OrderProductEntity::getOrderId));
  }

  private long calculateMerchandiseAmount(List<OrderProductEntity> orderProducts) {
    return orderProducts.stream()
        .map(OrderProductEntity::getTotalPrice)
        .filter(Objects::nonNull)
        .mapToLong(Long::longValue)
        .sum();
  }

  private String resolveOrderName(List<OrderProductEntity> orderProducts) {
    if (orderProducts.isEmpty()) {
      return "FitMarket 주문";
    }
    String firstName = orderProducts.get(0).getProductName();
    if (orderProducts.size() == 1) {
      return firstName;
    }
    return firstName + " 외 " + (orderProducts.size() - 1) + "건";
  }

  private OrderView findOwnedOrder(Long sellerId, String orderNumber) {
    return sellerOrderMapper.findOrderByNumberAndSellerId(orderNumber, sellerId)
        .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없어요. 주문 번호를 다시 확인해 주세요."));
  }

  private OrderAddressSnapshot parseSnapshot(String snapshotJson) {
    try {
      return objectMapper.readValue(snapshotJson, OrderAddressSnapshot.class);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("배송지 정보를 불러오지 못했어요. 잠시 후 다시 시도해 주세요.", e);
    }
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

  private OrderSummaryResponse toSummary(OrderView order, List<OrderProductEntity> products) {
    String orderName = resolveOrderName(products);
    long merchandiseAmount = calculateMerchandiseAmount(products);
    long totalAmount = merchandiseAmount;
    return new OrderSummaryResponse(
        order.getOrderNumber(),
        orderName,
        order.getOrderMode(),
        order.getApprovalStatus(),
        order.getPaymentStatus(),
        totalAmount,
        merchandiseAmount,
        0L,
        0L,
        products.size(),
        order.getOrderDate()
    );
  }

  private boolean isSellerUpdatable(OrderApprovalStatus status) {
    return status == OrderApprovalStatus.SHIPPING
        || status == OrderApprovalStatus.DELIVERED
        || status == OrderApprovalStatus.REJECTED;
  }

  private boolean isAllowedTransition(OrderApprovalStatus current, OrderApprovalStatus next) {
    if (next == OrderApprovalStatus.SHIPPING) {
      return current == OrderApprovalStatus.APPROVED;
    }
    if (next == OrderApprovalStatus.DELIVERED) {
      return current == OrderApprovalStatus.SHIPPING;
    }
    if (next == OrderApprovalStatus.REJECTED) {
      return current == OrderApprovalStatus.APPROVED;
    }
    return false;
  }
}
