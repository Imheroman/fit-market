package com.ssafy.fitmarket_be.product.dto;

import com.ssafy.fitmarket_be.product.domain.Product;

/**
 * 상품 상세 조회 응답 DTO.
 * 상세 조회 시 모든 정보를 포함합니다.
 * TODO: description, stock 필드 추가 예정
 */
public record ProductDetailResponse(
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
    // 영양 정보 (상세)
    int calories,
    int protein,
    int carbs,
    int fat
) {
    /**
     * Domain 객체를 상세 DTO로 변환합니다.
     */
    public static ProductDetailResponse from(Product product) {
        return new ProductDetailResponse(
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
            product.getNutrition().getFat()
        );
    }
}
