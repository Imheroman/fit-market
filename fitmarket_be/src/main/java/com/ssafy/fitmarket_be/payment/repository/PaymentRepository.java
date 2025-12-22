package com.ssafy.fitmarket_be.payment.repository;

import com.ssafy.fitmarket_be.payment.domain.Payment;
import com.ssafy.fitmarket_be.payment.domain.PaymentStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 결제 영속성을 담당하는 MyBatis 매퍼.
 */
@Mapper
public interface PaymentRepository {

  /**
   * 결제 정보를 upsert 한다.
   *
   * @param payment 결제 엔티티
   * @return 변경된 행 수
   */
  int upsert(Payment payment);

  /**
   * 주문 기준 결제 상태를 갱신한다.
   *
   * @param orderId 주문 식별자
   * @param status  결제 상태
   * @return 변경된 행 수
   */
  int updateStatusByOrderId(
      @Param("orderId") Long orderId,
      @Param("status") PaymentStatus status
  );

  /**
   * 주문의 결제 승인 시각을 조회한다.
   *
   * @param orderId 주문 식별자
   * @return 결제 승인 시각
   */
  Optional<LocalDateTime> findApprovedAtByOrderId(@Param("orderId") Long orderId);
}
