package com.ssafy.fitmarket_be.product.dto;

import com.ssafy.fitmarket_be.product.domain.Product;

/**
 * 상품 수정 응답 DTO.
 * 수정된 상품의 정보를 반환합니다.
 */
public record ProductUpdateResponse(
    Long id,
    String name,
    Long categoryId,
    String categoryName,
    Long price,
    String imageUrl,
    double rating,
    int reviewCount,
    // 영양 정보
    int calories,
    int protein,
    int carbs,
    int fat
) {
    /**
     * Domain 객체를 수정 응답 DTO로 변환합니다.
     */
    public static ProductUpdateResponse from(Product product) {
        return new ProductUpdateResponse(
            product.getId(),
            product.getName(),
            product.getCategoryId(),
            product.getCategoryName(),
            product.getPrice(),
            product.getImageUrl(),
            product.getRating(),
            product.getReviewCount(),
            product.getNutrition().getCalories(),
            product.getNutrition().getProtein(),
            product.getNutrition().getCarbs(),
            product.getNutrition().getFat()
        );
    }
}