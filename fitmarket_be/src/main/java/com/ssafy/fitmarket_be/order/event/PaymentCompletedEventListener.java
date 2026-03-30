package com.ssafy.fitmarket_be.order.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.fitmarket_be.cart.repository.ShoppingCartRepository;
import com.ssafy.fitmarket_be.order.domain.OrderApprovalStatus;
import com.ssafy.fitmarket_be.order.domain.OrderMode;
import com.ssafy.fitmarket_be.order.domain.OrderProductEntity;
import com.ssafy.fitmarket_be.order.repository.OrderRepository;
import com.ssafy.fitmarket_be.payment.domain.PaymentStatus;
import com.ssafy.fitmarket_be.payment.event.PaymentCompletedEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.StringUtils;

/**
 * 결제 완료 이벤트를 수신하여 주문 상태 갱신, 주문 상품 복원, 장바구니 정리를 수행한다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentCompletedEventListener {

  private final OrderRepository orderRepository;
  private final ShoppingCartRepository shoppingCartRepository;
  private final ObjectMapper objectMapper;

  @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
  public void handlePaymentCompleted(PaymentCompletedEvent event) {
    ensureOrderProducts(event);

    int paymentUpdated = orderRepository.updatePaymentStatus(event.orderId(), PaymentStatus.PAID);
    if (paymentUpdated <= 0) {
      throw new IllegalStateException("주문 결제 상태를 갱신하지 못했어요.");
    }
    int approvalUpdated = orderRepository.updateApprovalStatus(
        event.orderId(), OrderApprovalStatus.APPROVED.dbValue());
    if (approvalUpdated <= 0) {
      throw new IllegalStateException("주문 승인 상태를 갱신하지 못했어요.");
    }

    if (event.orderMode() == OrderMode.CART) {
      deletePurchasedCartItems(event);
    }
  }

  private void ensureOrderProducts(PaymentCompletedEvent event) {
    int existingProducts = orderRepository.countOrderProducts(event.orderId());
    if (existingProducts > 0) {
      return;
    }
    if (!StringUtils.hasText(event.itemsSnapshot())) {
      throw new IllegalStateException("주문 상품 정보를 불러오지 못했어요. 잠시 후 다시 시도해 주세요.");
    }
    List<OrderProductEntity> products = parseOrderProducts(event.itemsSnapshot());
    int insertedProducts = orderRepository.insertOrderProducts(event.orderId(), products);
    if (insertedProducts != products.size()) {
      throw new IllegalStateException("주문 상품 정보를 저장하지 못했어요. 잠시 후 다시 시도해 주세요.");
    }
    orderRepository.clearItemsSnapshot(event.orderId());
  }

  private List<OrderProductEntity> parseOrderProducts(String snapshot) {
    try {
      return objectMapper.readValue(snapshot, new TypeReference<>() {});
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("주문 상품 정보를 불러오지 못했어요. 잠시 후 다시 시도해 주세요.", e);
    }
  }

  private void deletePurchasedCartItems(PaymentCompletedEvent event) {
    List<Long> cartItemIds = orderRepository.findCartItemIdsByOrderId(event.orderId());
    if (cartItemIds.isEmpty()) {
      return;
    }
    int deleted = shoppingCartRepository.softDeleteByIds(cartItemIds, event.userId());
    if (deleted != cartItemIds.size()) {
      log.info("cart items already removed or partially removed. orderId={}, deleted={}, expected={}",
          event.orderId(), deleted, cartItemIds.size());
    }
  }
}
