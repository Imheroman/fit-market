package com.ssafy.fitmarket_be.category.dto;

import com.ssafy.fitmarket_be.category.domain.ProductCategory;

/**
 * 카테고리 응답 DTO.
 * 카테고리 정보와 해당 카테고리의 상품 개수를 포함합니다.
 */
public record CategoryResponse(
    Long id,
    String name,
    Long productCount
) {
    /**
     * Domain 객체를 DTO로 변환합니다.
     */
    public static CategoryResponse from(ProductCategory category) {
        return new CategoryResponse(
            category.getId(),
            category.getName(),
            category.getProductCount()
        );
    }
}