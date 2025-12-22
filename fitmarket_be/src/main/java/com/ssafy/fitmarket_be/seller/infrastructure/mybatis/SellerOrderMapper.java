package com.ssafy.fitmarket_be.seller.infrastructure.mybatis;

import com.ssafy.fitmarket_be.order.domain.OrderProductEntity;
import com.ssafy.fitmarket_be.order.domain.OrderView;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SellerOrderMapper {

  List<OrderView> findOrdersBySellerIdAndStartDate(
      @Param("sellerId") Long sellerId,
      @Param("startDate") LocalDateTime startDate
  );

  Optional<OrderView> findOrderByNumberAndSellerId(
      @Param("orderNumber") String orderNumber,
      @Param("sellerId") Long sellerId
  );

  List<OrderProductEntity> findOrderProductsByOrderIdsAndSellerId(
      @Param("orderIds") List<Long> orderIds,
      @Param("sellerId") Long sellerId
  );

  int updateApprovalStatus(
      @Param("orderId") Long orderId,
      @Param("approvalStatusName") String approvalStatusName
  );
}
