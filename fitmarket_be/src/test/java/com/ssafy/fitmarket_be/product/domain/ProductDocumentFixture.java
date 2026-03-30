package com.ssafy.fitmarket_be.product.domain;

import com.ssafy.fitmarket_be.product.document.NutritionInfo;
import com.ssafy.fitmarket_be.product.document.ProductDocument;
import java.time.LocalDateTime;

public class ProductDocumentFixture {

    private ProductDocumentFixture() {}

    /**
     * 기본 ProductDocument 생성.
     */
    public static ProductDocument create(Long id, String name, Float rating) {
        return ProductDocument.builder()
                .id(id)
                .name(name)
                .description("테스트 설명 " + name)
                .price(10000L)
                .stock(100)
                .rating(rating)
                .reviewCount(10)
                .imageUrl("https://img.test/" + id)
                .categoryId(1L)
                .categoryName("단백질")
                .foodName("닭가슴살")
                .nutrition(NutritionInfo.builder()
                        .calories(250f).protein(30f).carbs(10f).fat(5f)
                        .build())
                .sellerId(1L)
                .createdDate(LocalDateTime.of(2026, 3, 20, 10, 0))
                .updatedDate(LocalDateTime.of(2026, 3, 20, 10, 0))
                .build();
    }

    /**
     * 카테고리 지정 생성 (ES 카테고리 필터 테스트용).
     */
    public static ProductDocument withCategory(Long id, String name, Long categoryId, String categoryName) {
        return ProductDocument.builder()
                .id(id)
                .name(name)
                .description("설명 " + name)
                .price(15000L)
                .stock(50)
                .rating(4.0f)
                .reviewCount(5)
                .imageUrl("https://img.test/" + id)
                .categoryId(categoryId)
                .categoryName(categoryName)
                .foodName("프로틴바")
                .nutrition(NutritionInfo.builder()
                        .calories(200f).protein(25f).carbs(15f).fat(8f)
                        .build())
                .sellerId(1L)
                .createdDate(LocalDateTime.of(2026, 3, 20, 10, 0))
                .updatedDate(LocalDateTime.of(2026, 3, 20, 10, 0))
                .build();
    }

    /**
     * description 지정 생성 (multiMatch description 필드 매칭 테스트용).
     */
    public static ProductDocument withDescription(Long id, String name, String description) {
        return ProductDocument.builder()
                .id(id)
                .name(name)
                .description(description)
                .price(10000L)
                .stock(100)
                .rating(4.0f)
                .reviewCount(10)
                .imageUrl("https://img.test/" + id)
                .categoryId(1L)
                .categoryName("단백질")
                .foodName("프로틴바")
                .nutrition(NutritionInfo.builder()
                        .calories(250f).protein(30f).carbs(10f).fat(5f)
                        .build())
                .sellerId(1L)
                .createdDate(LocalDateTime.of(2026, 3, 20, 10, 0))
                .updatedDate(LocalDateTime.of(2026, 3, 20, 10, 0))
                .build();
    }

    /**
     * rating + reviewCount 지정 (function_score 테스트용).
     */
    public static ProductDocument withScore(Long id, String name, Float rating, Integer reviewCount) {
        return ProductDocument.builder()
                .id(id)
                .name(name)
                .description("스코어 테스트 " + name)
                .price(12000L)
                .stock(80)
                .rating(rating)
                .reviewCount(reviewCount)
                .imageUrl("https://img.test/" + id)
                .categoryId(1L)
                .categoryName("단백질")
                .foodName("프로틴")
                .nutrition(NutritionInfo.builder()
                        .calories(180f).protein(20f).carbs(12f).fat(6f)
                        .build())
                .sellerId(1L)
                .createdDate(LocalDateTime.of(2026, 3, 20, 10, 0))
                .updatedDate(LocalDateTime.of(2026, 3, 20, 10, 0))
                .build();
    }
}
