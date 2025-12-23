package com.ssafy.fitmarket_be.order.repository;

import com.ssafy.fitmarket_be.entity.Order;
import com.ssafy.fitmarket_be.order.domain.OrderPaymentContext;
import com.ssafy.fitmarket_be.order.domain.OrderProductEntity;
import com.ssafy.fitmarket_be.order.domain.OrderReturnExchangeEntity;
import com.ssafy.fitmarket_be.order.domain.OrderView;
import com.ssafy.fitmarket_be.order.domain.OrderAddress;
import com.ssafy.fitmarket_be.payment.domain.PaymentStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 주문 관련 MyBatis 매퍼 인터페이스.
 */
@Mapper
public interface OrderRepository {

  /**
   * 사용자 주문 목록 아이디를 조회한다.
   *
   * @param userId    사용자 식별자
   * @param startDate 조회 시작 시점
   * @return 주문 뷰 목록
   */
  List<Long> findOrdersPkByUserIdAndStartDate(
      @Param("userId") Long userId,
      @Param("startDate") LocalDateTime startDate
  );

  /**
   * 주문을 생성한다.
   *
   * @param order 주문 엔티티
   * @return 생성된 행 수
   */
  int insertOrder(Order order);

  /**
   * 주문 상품 스냅샷을 일괄 저장한다.
   *
   * @param orderId 주문 식별자
   * @param items   주문 상품 목록
   */
  int insertOrderProducts(
      @Param("orderId") Long orderId,
      @Param("items") List<OrderProductEntity> items
  );

  /**
   * 주문 배송지 이력을 저장한다.
   *
   * @param orderId 주문 식별자
   * @param address 주문 시점 배송지
   * @return 생성된 행 수
   */
  int insertOrderAddress(
      @Param("orderId") Long orderId,
      @Param("address") OrderAddress address
  );

  /**
   * 주문의 기존 배송지 이력을 만료 상태로 전환한다.
   *
   * @param orderId 주문 식별자
   * @return 변경된 행 수
   */
  int deactivateOrderAddresses(@Param("orderId") Long orderId);

  /**
   * 사용자 주문 목록을 조회한다.
   *
   * @param userId    사용자 식별자
   * @param startDate 조회 시작 시점
   * @return 주문 뷰 목록
   */
  List<OrderView> findOrdersByUserIdAndStartDate(
      @Param("userId") Long userId,
      @Param("startDate") LocalDateTime startDate
  );

  /**
   * 주문 번호로 단일 주문을 조회한다.
   *
   * @param orderNumber 주문 번호
   * @param userId      사용자 식별자
   * @return 주문 뷰
   */
  Optional<OrderView> findOrderByNumberAndUserId(
      @Param("orderNumber") String orderNumber,
      @Param("userId") Long userId
  );

  /**
   * 주문 상품 목록을 조회한다.
   *
   * @param orderIds 주문 식별자 목록
   * @return 주문 상품 스냅샷 목록
   */
  List<OrderProductEntity> findOrderProductsByOrderIds(@Param("orderIds") List<Long> orderIds);

  /**
   * 결제 승인에 필요한 주문 정보를 조회한다.
   *
   * @param orderNumber 주문 번호
   * @return 결제 컨텍스트
   */
  Optional<OrderPaymentContext> findPaymentContextByOrderNumber(@Param("orderNumber") String orderNumber);

  /**
   * 주문에 매핑된 상품 스냅샷 건수를 조회한다.
   *
   * @param orderId 주문 식별자
   * @return 주문 상품 개수
   */
  int countOrderProducts(@Param("orderId") Long orderId);

  /**
   * 주문에 포함된 장바구니 아이템 목록을 조회한다.
   *
   * @param orderId 주문 식별자
   * @return cart_item_id 목록
   */
  List<Long> findCartItemIdsByOrderId(@Param("orderId") Long orderId);

  /**
   * 주문 결제 상태를 갱신한다.
   *
   * @param orderId       주문 식별자
   * @param paymentStatus 결제 상태
   * @return 변경된 행 수
   */
  int updatePaymentStatus(
      @Param("orderId") Long orderId,
      @Param("paymentStatus") PaymentStatus paymentStatus
  );

  /**
   * 주문 승인 상태를 갱신한다.
   *
   * @param orderId             주문 식별자
   * @param approvalStatusName  주문 승인 상태 이름
   * @return 변경된 행 수
   */
  int updateApprovalStatus(
      @Param("orderId") Long orderId,
      @Param("approvalStatusName") String approvalStatusName
  );

  /**
   * 주문 배송지 정보를 수정한다.
   *
   * @param orderId         주문 식별자
   * @param userId          사용자 식별자
   * @param addressId       배송지 식별자
   * @param addressSnapshot 배송지 스냅샷 JSON
   * @return 변경된 행 수
   */
  int updateOrderAddress(
      @Param("orderId") Long orderId,
      @Param("userId") Long userId,
      @Param("addressId") Long addressId,
      @Param("addressSnapshot") String addressSnapshot
  );

  /**
   * 주문을 소프트 삭제한다.
   *
   * @param orderId 주문 식별자
   * @param userId  사용자 식별자
   * @return 삭제된 행 수
   */
  int softDeleteOrder(
      @Param("orderId") Long orderId,
      @Param("userId") Long userId
  );

  /**
   * 주문에 속한 상품 스냅샷을 소프트 삭제한다.
   *
   * @param orderId 주문 식별자
   * @return 삭제된 행 수
   */
  int softDeleteOrderProducts(@Param("orderId") Long orderId);

  /**
   * 주문 상품 스냅샷 JSON을 제거한다.
   *
   * @param orderId 주문 식별자
   * @return 변경된 행 수
   */
  int clearItemsSnapshot(@Param("orderId") Long orderId);

  /**
   * 주문 반품/교환/환불 요청을 저장한다.
   *
   * @param request 요청 엔티티
   * @return 생성된 행 수
   */
  int insertOrderReturnExchange(@Param("request") OrderReturnExchangeEntity request);

  /**
   * 주문에 반품/교환/환불 요청이 존재하는지 확인한다.
   *
   * @param orderId 주문 식별자
   * @return 요청 건수
   */
  int countOrderReturnExchanges(@Param("orderId") Long orderId);

  /**
   * 주문 반품/교환/환불 요청 단건을 조회한다.
   *
   * @param orderId 주문 식별자
   * @return 반품/교환/환불 요청
   */
  Optional<OrderReturnExchangeEntity> findOrderReturnExchangeByOrderId(@Param("orderId") Long orderId);
}
