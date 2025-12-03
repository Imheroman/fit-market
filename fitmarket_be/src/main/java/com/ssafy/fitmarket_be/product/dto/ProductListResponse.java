package com.ssafy.fitmarket_be.product.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ProductListResponse(
    List<ProductItem> items,
    Pagination pagination
) {
    public record ProductItem(
        Long id,
        String name,
        String description,
        Long price,
        Integer stock,
        String imageUrl,
        Long categoryId,
        String categoryName,
        // 영양 정보 (중첩 객체 제거, 평평하게)
        String calories,
        String protein,
        String carbs,
        String fat,
        LocalDateTime createdDate
    ) {}

    public record Pagination(
        Integer currentPage,
        Integer totalPages,
        Long totalElements,
        Integer size,
        Boolean hasNext,
        Boolean hasPrevious
    ) {}
}
