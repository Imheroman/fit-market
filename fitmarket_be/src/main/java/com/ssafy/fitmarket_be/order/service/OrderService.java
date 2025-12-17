package com.ssafy.fitmarket_be.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.fitmarket_be.address.repository.AddressRepository;
import com.ssafy.fitmarket_be.cart.repository.ShoppingCartRepository;
import com.ssafy.fitmarket_be.entity.Address;
import com.ssafy.fitmarket_be.entity.Order;
import com.ssafy.fitmarket_be.entity.ShoppingCartProduct;
import com.ssafy.fitmarket_be.order.domain.OrderAddress;
import com.ssafy.fitmarket_be.order.domain.OrderAddressSnapshot;
import com.ssafy.fitmarket_be.order.domain.OrderApprovalStatus;
import com.ssafy.fitmarket_be.order.domain.OrderMode;
import com.ssafy.fitmarket_be.order.domain.OrderProductEntity;
import com.ssafy.fitmarket_be.order.domain.OrderView;
import com.ssafy.fitmarket_be.order.dto.OrderAddressUpdateRequest;
import com.ssafy.fitmarket_be.order.dto.OrderCreateRequest;
import com.ssafy.fitmarket_be.order.dto.OrderCreateResponse;
import com.ssafy.fitmarket_be.order.dto.OrderDetailResponse;
import com.ssafy.fitmarket_be.order.dto.OrderItemResponse;
import com.ssafy.fitmarket_be.order.dto.OrderRefundRequest;
import com.ssafy.fitmarket_be.order.dto.OrderStatusUpdateRequest;
import com.ssafy.fitmarket_be.order.dto.OrderSummaryResponse;
import com.ssafy.fitmarket_be.order.repository.OrderRepository;
import com.ssafy.fitmarket_be.payment.domain.PaymentStatus;
import com.ssafy.fitmarket_be.payment.repository.PaymentRepository;
import com.ssafy.fitmarket_be.product.domain.Product;
import com.ssafy.fitmarket_be.product.repository.ProductMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 주문 생성/조회/수정 유스케이스를 담당한다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

  private static final int MAX_QUANTITY_PER_PRODUCT = 100;

  private final OrderRepository orderRepository;
  private final ShoppingCartRepository shoppingCartRepository;
  private final ProductMapper productMapper;
  private final AddressRepository addressRepository;
  private final PaymentRepository paymentRepository;
  private final ObjectMapper objectMapper;

  /**
   * 장바구니 혹은 바로구매 요청을 받아 주문을 생성한다.
   *
   * @param userId  주문자 식별자
   * @param request 주문 생성 요청
   * @return 주문 번호 및 금액 정보
   * <p>결제 위젯에 전달할 주문 번호를 프런트에서 생성했다면 {@code orderNumber} 필드로 함께 전달한다.</p>
   */
  @Transactional
  public OrderCreateResponse createOrder(Long userId, OrderCreateRequest request) {
    return createOrderInternal(userId, request, null);
  }

  /**
   * 선결제 후 주문 생성 플로우를 위해, 프런트에서 전달한 주문 번호를 그대로 사용해 주문을 생성한다.
   *
   * @param userId      주문자 식별자
   * @param orderNumber 결제 위젯에 전달한 주문 번호
   * @param request     주문 생성 요청
   * @return 주문 번호 및 금액 정보
   */
  @Transactional
  public OrderCreateResponse createOrderWithOrderNumber(
      Long userId,
      String orderNumber,
      OrderCreateRequest request
  ) {
    return createOrderInternal(userId, request, orderNumber);
  }

  private OrderCreateResponse createOrderInternal(
      Long userId,
      OrderCreateRequest request,
      String orderNumberOverride
  ) {
    OrderMode mode = request.resolvedMode();
    Address address = addressRepository.findByIdAndUserId(request.addressId(), userId)
        .orElseThrow(() -> new IllegalArgumentException("배송지 정보를 불러오지 못했어요. 다시 선택해 주세요."));

    List<OrderProductEntity> orderProducts = mode.isCart()
        ? buildFromCart(userId, request.cartItemIds())
        : List.of(buildDirectItem(request.productId(), request.quantity()));

    long merchandiseAmount = calculateMerchandiseAmount(orderProducts);
    long shippingFee = Objects.requireNonNullElse(request.shippingFee(), 0L);
    long discountAmount = Objects.requireNonNullElse(request.discountAmount(), 0L);
    long totalAmount = merchandiseAmount + shippingFee - discountAmount;

    if (totalAmount <= 0) {
      throw new IllegalArgumentException("결제 금액이 0원 이하예요. 할인/배송비 설정을 다시 확인해 주세요.");
    }

    String orderNumber = resolveOrderNumber(orderNumberOverride, request.orderNumber());
    String itemsSnapshot = writeItemsSnapshot(orderProducts);
    Order order = Order.builder()
        .orderNumber(orderNumber)
        .orderMode(mode)
        .orderApprovalStatusId(null)
        .addressId(address.getId())
        .addressSnapshot(toSnapshotJson(address))
        .userId(userId)
        .orderDate(LocalDateTime.now())
        .merchandiseAmount(merchandiseAmount)
        .shippingFee(shippingFee)
        .discountAmount(discountAmount)
        .totalAmount(totalAmount)
        .paymentStatus(PaymentStatus.PENDING)
        .comment(request.comment())
        .itemsSnapshot(itemsSnapshot)
        .build();

    try {
      int inserted = orderRepository.insertOrder(order);
      if (inserted <= 0 || order.getId() == null) {
        throw new IllegalStateException("주문을 생성하지 못했어요. 잠시 후 다시 시도해 주세요.");
      }
    } catch (DuplicateKeyException e) {
      throw new IllegalStateException("이미 처리 중인 주문 번호예요. 잠시 후 다시 시도해 주세요.", e);
    }

    saveOrderProducts(order.getId(), orderProducts);
    saveOrderAddressHistory(order.getId(), address);

    int updatedApproval = orderRepository.updateApprovalStatus(order.getId(),
        OrderApprovalStatus.PENDING_APPROVAL.dbValue());
    if (updatedApproval <= 0) {
      throw new IllegalStateException("주문 상태를 저장하지 못했어요. 잠시 후 다시 시도해 주세요.");
    }
    log.info("created order {} for user {} with {} items", orderNumber, userId,
        orderProducts.size());

    return new OrderCreateResponse(
        orderNumber,
        resolveOrderName(orderProducts),
        totalAmount,
        merchandiseAmount,
        shippingFee,
        discountAmount,
        mode
    );
  }

  /**
   * 사용자 주문 목록을 반환한다.
   *
   * @param userId 사용자 식별자
   * @return 주문 요약 목록
   */
  @Transactional(readOnly = true)
  public List<OrderSummaryResponse> getOrders(Long userId) {
    List<OrderView> orders = orderRepository.findOrdersByUserId(userId);
    if (orders.isEmpty()) {
      return List.of();
    }
    Map<Long, List<OrderProductEntity>> productsByOrderId = findOrderProductsGrouped(orders);

    return orders.stream()
        .map(order -> toSummary(order, productsByOrderId.getOrDefault(order.getId(), List.of())))
        .toList();
  }

  /**
   * 주문 상세 정보를 반환한다.
   *
   * @param userId      사용자 식별자
   * @param orderNumber 주문 번호
   * @return 주문 상세
   */
  @Transactional(readOnly = true)
  public OrderDetailResponse getOrderDetail(Long userId, String orderNumber) {
    OrderView order = findOwnedOrder(userId, orderNumber);
    Map<Long, List<OrderProductEntity>> productsByOrderId =
        findOrderProductsGrouped(List.of(order));
    List<OrderProductEntity> products = productsByOrderId.getOrDefault(order.getId(), List.of());
    String orderName = resolveOrderName(products);

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
        order.getOrderDate(),
        order.getComment(),
        parseSnapshot(order.getAddressSnapshot()),
        products.stream()
            .map(this::toItemResponse)
            .toList()
    );
  }

  /**
   * 주문 배송지를 변경한다.
   *
   * @param userId      사용자 식별자
   * @param orderNumber 주문 번호
   * @param request     배송지 변경 요청
   */
  @Transactional
  public void updateOrderAddress(Long userId, String orderNumber, OrderAddressUpdateRequest request) {
    OrderView order = findOwnedOrder(userId, orderNumber);
    OrderApprovalStatus status = OrderApprovalStatus.from(order.getApprovalStatus());
    if (status.isShippingOrLater()) {
      throw new IllegalStateException("배송이 시작된 주문은 배송지를 바꿀 수 없어요.");
    }

    Address address = addressRepository.findByIdAndUserId(request.addressId(), userId)
        .orElseThrow(() -> new IllegalArgumentException("배송지 정보를 찾을 수 없어요. 다시 선택해 주세요."));
    int updated = orderRepository.updateOrderAddress(
        order.getId(),
        userId,
        address.getId(),
        toSnapshotJson(address)
    );
    if (updated <= 0) {
      throw new IllegalStateException("배송지 정보를 수정하지 못했어요. 잠시 후 다시 시도해 주세요.");
    }
    refreshOrderAddressHistory(order.getId(), address);
  }

  /**
   * 주문 상태를 변경한다.
   *
   * @param userId      사용자 식별자
   * @param orderNumber 주문 번호
   * @param request     상태 변경 요청
   */
  @Transactional
  public void updateApprovalStatus(Long userId, String orderNumber, OrderStatusUpdateRequest request) {
    OrderView order = findOwnedOrder(userId, orderNumber);
    OrderApprovalStatus newStatus = OrderApprovalStatus.from(request.approvalStatus());
    OrderApprovalStatus currentStatus = OrderApprovalStatus.from(order.getApprovalStatus());
    if (currentStatus == newStatus) {
      return;
    }
    int updated = orderRepository.updateApprovalStatus(order.getId(), newStatus.dbValue());
    if (updated <= 0) {
      throw new IllegalStateException("주문 상태를 변경하지 못했어요. 잠시 후 다시 시도해 주세요.");
    }
  }

  /**
   * 결제 완료된 주문을 환불 처리한다.
   *
   * @param userId      사용자 식별자
   * @param orderNumber 주문 번호
   * @param request     환불 요청
   */
  @Transactional
  public void refundOrder(Long userId, String orderNumber, OrderRefundRequest request) {
    OrderView order = findOwnedOrder(userId, orderNumber);
    OrderApprovalStatus status = OrderApprovalStatus.from(order.getApprovalStatus());
    if (status.isTerminal()) {
      throw new IllegalStateException("이미 종료된 주문이에요. 환불할 수 없어요.");
    }
    if (order.getPaymentStatus() != PaymentStatus.PAID) {
      throw new IllegalStateException("결제 완료된 주문만 환불할 수 있어요.");
    }

    int updatedOrder = orderRepository.updatePaymentStatus(order.getId(), PaymentStatus.REFUNDED);
    if (updatedOrder <= 0) {
      throw new IllegalStateException("주문 결제 상태를 변경하지 못했어요.");
    }
    int updatedPayment = paymentRepository.updateStatusByOrderId(order.getId(), PaymentStatus.REFUNDED);
    if (updatedPayment == 0) {
      log.info("payment row not found while refunding order {}", orderNumber);
    }
    orderRepository.updateApprovalStatus(order.getId(), OrderApprovalStatus.CANCELLED.dbValue());

    log.info("refund requested for order {} by user {} reason={}", orderNumber, userId,
        Objects.toString(request.reason(), "N/A"));
  }

  /**
   * 주문과 주문 상품 스냅샷을 소프트 삭제한다.
   *
   * @param userId      사용자 식별자
   * @param orderNumber 주문 번호
   */
  @Transactional
  public void deleteOrder(Long userId, String orderNumber) {
    OrderView order = findOwnedOrder(userId, orderNumber);
    int deletedOrder = orderRepository.softDeleteOrder(order.getId(), userId);
    if (deletedOrder <= 0) {
      throw new IllegalStateException("주문을 삭제하지 못했어요. 잠시 후 다시 시도해 주세요.");
    }
    orderRepository.softDeleteOrderProducts(order.getId());
  }

  private void saveOrderProducts(Long orderId, List<OrderProductEntity> orderProducts) {
    int insertedProducts = orderRepository.insertOrderProducts(orderId, orderProducts);
    if (insertedProducts != orderProducts.size()) {
      throw new IllegalStateException("주문 상품 정보를 저장하지 못했어요. 잠시 후 다시 시도해 주세요.");
    }
  }

  private void saveOrderAddressHistory(Long orderId, Address address) {
    OrderAddress orderAddress = OrderAddress.builder()
        .orderId(orderId)
        .recipient(address.getRecipient())
        .phone(address.getPhone())
        .postalCode(address.getPostalCode())
        .addressLine(address.getAddressLine())
        .addressLineDetail(address.getAddressLineDetail())
        .memo(address.getMemo())
        .current(true)
        .build();
    int inserted = orderRepository.insertOrderAddress(orderId, orderAddress);
    if (inserted <= 0) {
      throw new IllegalStateException("배송지 정보를 저장하지 못했어요. 다시 시도해 주세요.");
    }
  }

  private void refreshOrderAddressHistory(Long orderId, Address address) {
    orderRepository.deactivateOrderAddresses(orderId);
    saveOrderAddressHistory(orderId, address);
  }

  private List<OrderProductEntity> buildFromCart(Long userId, List<Long> cartItemIds) {
    if (cartItemIds == null || cartItemIds.isEmpty()) {
      throw new IllegalArgumentException("장바구니에서 선택한 상품이 없어요.");
    }
    List<ShoppingCartProduct> cartProducts = shoppingCartRepository.findByIds(userId, cartItemIds);
    if (cartProducts.size() != cartItemIds.size()) {
      throw new IllegalArgumentException("선택한 장바구니 상품을 모두 찾을 수 없어요. 다시 선택해 주세요.");
    }

    List<OrderProductEntity> orderProducts = new ArrayList<>();
    for (ShoppingCartProduct cartProduct : cartProducts) {
      validateQuantityLimit(cartProduct.getQuantity());
      long totalPrice = cartProduct.getPrice() * cartProduct.getQuantity();
      orderProducts.add(OrderProductEntity.builder()
          .productId(cartProduct.getProductId())
          .cartItemId(cartProduct.getId())
          .productName(cartProduct.getProductName())
          .quantity(cartProduct.getQuantity())
          .unitPrice(cartProduct.getPrice())
          .totalPrice(totalPrice)
          .build());
    }
    return orderProducts;
  }

  private OrderProductEntity buildDirectItem(Long productId, Integer quantity) {
    Product product = productMapper.selectProductById(productId);
    if (product == null) {
      throw new IllegalArgumentException("상품 정보를 찾을 수 없어요. 다시 시도해 주세요.");
    }
    if (product.getStock() < quantity) {
      throw new IllegalArgumentException("재고가 부족해요. 수량을 다시 선택해 주세요.");
    }
    validateQuantityLimit(quantity);

    long totalPrice = product.getPrice() * quantity;
    return OrderProductEntity.builder()
        .productId(product.getId())
        .productName(product.getName())
        .quantity(quantity)
        .unitPrice(product.getPrice())
        .totalPrice(totalPrice)
        .build();
  }

  private long calculateMerchandiseAmount(List<OrderProductEntity> orderProducts) {
    return orderProducts.stream()
        .mapToLong(OrderProductEntity::getTotalPrice)
        .sum();
  }

  private String toSnapshotJson(Address address) {
    try {
      return objectMapper.writeValueAsString(OrderAddressSnapshot.from(address));
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("배송지 정보를 저장하지 못했어요. 다시 시도해 주세요.", e);
    }
  }

  private String writeItemsSnapshot(List<OrderProductEntity> orderProducts) {
    try {
      return objectMapper.writeValueAsString(orderProducts);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("주문 상품 정보를 준비하지 못했어요. 잠시 후 다시 시도해 주세요.", e);
    }
  }

  private String resolveOrderNumber(String orderNumberOverride, String requestOrderNumber) {
    if (StringUtils.hasText(orderNumberOverride)) {
      return normalizeOrderNumber(orderNumberOverride);
    }
    if (StringUtils.hasText(requestOrderNumber)) {
      return normalizeOrderNumber(requestOrderNumber);
    }
    return UUID.randomUUID().toString();
  }

  private String normalizeOrderNumber(String orderNumber) {
    String normalized = orderNumber.trim();
    if (!StringUtils.hasText(normalized)) {
      throw new IllegalArgumentException("주문 번호가 비어 있어요. 다시 시도해 주세요.");
    }
    if (normalized.length() > 40) {
      throw new IllegalArgumentException("주문 번호가 너무 길어요. 다시 시도해 주세요.");
    }
    return normalized;
  }

  private OrderAddressSnapshot parseSnapshot(String snapshotJson) {
    try {
      return objectMapper.readValue(snapshotJson, OrderAddressSnapshot.class);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("배송지 정보를 불러오지 못했어요. 잠시 후 다시 시도해 주세요.", e);
    }
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

  private OrderView findOwnedOrder(Long userId, String orderNumber) {
    return orderRepository.findOrderByNumberAndUserId(orderNumber, userId)
        .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없어요. 주문 번호를 다시 확인해 주세요."));
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

  private void validateQuantityLimit(int quantity) {
    if (quantity > MAX_QUANTITY_PER_PRODUCT) {
      throw new IllegalArgumentException("한 상품은 한 번에 최대 100개까지 주문할 수 있어요.");
    }
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
}
