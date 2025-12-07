package com.ssafy.fitmarket_be.product.dto;

import com.ssafy.fitmarket_be.product.domain.Product;

/**
 * 상품 목록 조회 응답 DTO.
 * 목록 조회 시 필요한 최소한의 정보만 포함합니다.
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
    int calories
) {
    /**
     * Domain 객체를 목록용 DTO로 변환합니다.
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
            product.getNutrition().getCalories()
        );
    }
}
