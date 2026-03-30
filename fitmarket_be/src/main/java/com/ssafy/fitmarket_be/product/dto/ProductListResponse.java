package com.ssafy.fitmarket_be.product.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ssafy.fitmarket_be.product.domain.Product;

/**
 * 상품 목록 조회 응답 DTO.
 * 목록 조회 시 필요한 최소한의 정보만 포함합니다.
 * ES 검색 시 highlightedName, highlightedDescription 필드가 추가됩니다 (null이면 JSON에 미포함).
 */
public record ProductListResponse(
    Long id,
    String name,
    String description,
    Long categoryId,
    String categoryName,
    Long price,
    int stock,
    String imageUrl,
    double rating,
    int reviewCount,
    int calories,
    int protein,
    int carbs,
    int fat,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String highlightedName,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String highlightedDescription
) {
    /**
     * Domain 객체를 목록용 DTO로 변환합니다 (하이라이팅 없음).
     */
    public static ProductListResponse from(Product product) {
        return new ProductListResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getCategoryId(),
            product.getCategoryName(),
            product.getPrice(),
            product.getStock(),
            product.getImageUrl(),
            product.getRating(),
            product.getReviewCount(),
            product.getNutrition().getCalories(),
            product.getNutrition().getProtein(),
            product.getNutrition().getCarbs(),
            product.getNutrition().getFat(),
            null,
            null
        );
    }
}
