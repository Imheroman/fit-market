package com.ssafy.fitmarket_be.product.sync;

import java.time.LocalDateTime;

/**
 * ES 동기화용 상품 데이터.
 * Product 도메인에 포함되지 않는 user_id, food_name, created_date, modified_date를 함께 전달한다.
 */
public record ProductSyncData(
    Long id,
    String name,
    String description,
    Long price,
    Integer stock,
    Double rating,
    Integer reviewCount,
    String imageUrl,
    Long categoryId,
    String categoryName,
    String foodName,
    Integer calories,
    Integer protein,
    Integer carbs,
    Integer fat,
    Long sellerId,
    LocalDateTime createdDate,
    LocalDateTime modifiedDate,
    LocalDateTime deletedDate
) {}
