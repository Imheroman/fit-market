package com.ssafy.fitmarket_be.seller.presentation;

import com.ssafy.fitmarket_be.order.domain.OrderSearchPeriod;
import com.ssafy.fitmarket_be.order.dto.OrderDetailResponse;
import com.ssafy.fitmarket_be.order.dto.OrderStatusUpdateRequest;
import com.ssafy.fitmarket_be.order.dto.OrderSummaryResponse;
import com.ssafy.fitmarket_be.seller.api.SellerOrderService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequiredArgsConstructor
@RequestMapping("/seller/orders")
public class SellerOrderController {

  private final SellerOrderService sellerOrderService;

  @GetMapping
  public ResponseEntity<List<OrderSummaryResponse>> getOrders(
      @AuthenticationPrincipal(expression = "id") Long sellerId,
      @RequestParam(name = "period", required = false) String period
  ) {
    OrderSearchPeriod searchPeriod = OrderSearchPeriod.from(period);
    List<OrderSummaryResponse> responses = sellerOrderService.getOrders(sellerId, searchPeriod);
    return ResponseEntity.status(HttpStatus.OK).body(responses);
  }

  @GetMapping("/{orderNumber}")
  public ResponseEntity<OrderDetailResponse> getOrderDetail(
      @AuthenticationPrincipal(expression = "id") Long sellerId,
      @PathVariable String orderNumber
  ) {
    OrderDetailResponse response = sellerOrderService.getOrderDetail(sellerId, orderNumber);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @PatchMapping("/{orderNumber}/status")
  public ResponseEntity<Void> updateOrderStatus(
      @AuthenticationPrincipal(expression = "id") Long sellerId,
      @PathVariable String orderNumber,
      @Valid @RequestBody OrderStatusUpdateRequest request
  ) {
    sellerOrderService.updateOrderStatus(sellerId, orderNumber, request);
    return ResponseEntity.status(HttpStatus.OK).build();
  }
}
