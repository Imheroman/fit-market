package com.ssafy.fitmarket_be.cart.repository;

import com.ssafy.fitmarket_be.entity.ShoppingCartProduct;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 장바구니 영속성 인터페이스.
 */
@Mapper
public interface ShoppingCartRepository {

  /**
   * 활성화된 장바구니 상품 수량을 조회한다.
   * @param userId 사용자 식별자
   * @return 장바구나 상품 수량
   */
  int countCartItems(Long userId);

  /**
   * 활성화된 장바구니 상품을 조회한다.
   *
   * @param userId 사용자 식별자
   * @return 장바구니 상품 목록
   */
  List<ShoppingCartProduct> findActiveByUserId(@Param("userId") Long userId);

  /**
   * 장바구니에서 특정 항목들을 조회한다.
   *
   * @param userId      사용자 식별자
   * @param cartItemIds 조회할 장바구니 아이디 목록
   * @return 장바구니 상품 목록
   */
  List<ShoppingCartProduct> findByIds(
      @Param("userId") Long userId,
      @Param("cartItemIds") List<Long> cartItemIds
  );

  /**
   * 장바구니에 동일 상품이 존재하면 수량을 증가시킨다.
   *
   * @param userId    사용자 식별자
   * @param productId 상품 식별자
   * @param quantity  증가시킬 수량
   * @return 변경된 행 수
   */
  int incrementQuantity(
      @Param("userId") Long userId,
      @Param("productId") Long productId,
      @Param("quantity") int quantity
  );

  /**
   * 장바구니에 상품을 추가한다.
   *
   * @param userId    사용자 식별자
   * @param productId 상품 식별자
   * @param quantity  담을 수량
   * @return 삽입된 행 수
   */
  int insert(
      @Param("userId") Long userId,
      @Param("productId") Long productId,
      @Param("quantity") int quantity
  );

  /**
   * 장바구니 수량을 변경한다.
   *
   * @param cartItemId 장바구니 아이템 식별자
   * @param userId 사용자 식별자
   * @param quantity 변경할 수량
   * @return 변경된 행 수
   */
  int updateQuantity(
      @Param("cartItemId") Long cartItemId,
      @Param("userId") Long userId,
      @Param("quantity") int quantity
  );

  /**
   * 장바구니를 소프트 삭제한다.
   *
   * @param cartItemId 장바구니 아이템 식별자
   * @param userId 사용자 식별자
   * @return 삭제된 행 수
   */
  int softDelete(
      @Param("cartItemId") Long cartItemId,
      @Param("userId") Long userId
  );

  /**
   * 장바구니 다건을 소프트 삭제한다.
   *
   * @param cartItemIds 장바구니 아이템 식별자 목록
   * @param userId      사용자 식별자
   * @return 삭제된 행 수
   */
  int softDeleteByIds(
      @Param("cartItemIds") List<Long> cartItemIds,
      @Param("userId") Long userId
  );
}
