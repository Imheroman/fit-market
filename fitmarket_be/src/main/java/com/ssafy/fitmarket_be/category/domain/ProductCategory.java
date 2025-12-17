package com.ssafy.fitmarket_be.category.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 상품 카테고리 도메인 객체.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCategory {
    private Long id;
    private Long parentId;
    private String name;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private LocalDateTime deletedDate;

    // 상품 개수 (조회 시에만 사용)
    private Long productCount;
}