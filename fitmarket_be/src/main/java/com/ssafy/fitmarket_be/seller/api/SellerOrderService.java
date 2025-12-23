package com.ssafy.fitmarket_be.seller.api;

import com.ssafy.fitmarket_be.order.domain.OrderSearchPeriod;
import com.ssafy.fitmarket_be.order.dto.OrderDetailResponse;
import com.ssafy.fitmarket_be.order.dto.OrderStatusUpdateRequest;
import com.ssafy.fitmarket_be.order.dto.OrderSummaryResponse;
import java.util.List;

public interface SellerOrderService {

  List<OrderSummaryResponse> getOrders(Long sellerId, OrderSearchPeriod period);

  OrderDetailResponse getOrderDetail(Long sellerId, String orderNumber);

  void updateOrderStatus(Long sellerId, String orderNumber, OrderStatusUpdateRequest request);
}
