package com.ssafy.fitmarket_be.entity;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

/**
 * 장바구니에 담긴 단일 상품과 상품 정보를 표현하는 도메인 모델.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Alias("ShoppingCartProduct")
public class ShoppingCartProduct {

  private Long id;
  private Long userId;
  private Long productId;
  private String productName;
  private Long categoryId;
  private String categoryName;
  private Long price;
  private int quantity;
  private String imageUrl;
  private int calories;
  private int protein;
  private int carbs;
  private int fat;
  private LocalDateTime createdDate;
  private LocalDateTime modifiedDate;
}
