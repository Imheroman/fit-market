package com.ssafy.fitmarket_be.product.dto;

import com.ssafy.fitmarket_be.product.domain.Product;

/**
 * 상품 응답 DTO.
 * Controller에서 클라이언트에게 반환하는 상품 정보.
 * 프론트엔드 요구사항에 맞춰 평탄화된 구조 사용.
 */
public record ProductResponse(
    Long id,
    String name,
    Long categoryId,
    String categoryName,
    Long price,
    String imageUrl,
    double rating,
    int reviewCount,
    // 영양 정보 (평탄화)
    int calories,
    int protein,
    int carbs,
    int fat
) {
    /**
     * Domain 객체를 DTO로 변환합니다.
     */
    public static ProductResponse from(Product product) {
        return new ProductResponse(
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