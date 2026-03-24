package com.ssafy.fitmarket_be.product.domain;

import com.ssafy.fitmarket_be.product.sync.ProductSyncData;
import java.time.LocalDateTime;

public class ProductSyncDataFixture {

    private ProductSyncDataFixture() {}

    /**
     * 활성 상품 (deletedDate == null).
     */
    public static ProductSyncData active(Long id) {
        return new ProductSyncData(
            id,
            "테스트 상품 " + id,
            "테스트 설명 " + id,
            10000L,
            100,
            4.5,
            10,
            "https://img.test/product/" + id,
            1L,
            "단백질",
            "닭가슴살",
            250,
            30,
            10,
            5,
            1L,
            LocalDateTime.of(2026, 3, 20, 10, 0),
            LocalDateTime.of(2026, 3, 20, 10, 0),
            null
        );
    }

    /**
     * soft-deleted 상품 (deletedDate != null).
     */
    public static ProductSyncData deleted(Long id) {
        return new ProductSyncData(
            id,
            "삭제된 상품 " + id,
            "삭제 설명",
            10000L,
            0,
            3.0,
            5,
            null,
            1L,
            "단백질",
            "닭가슴살",
            200,
            25,
            8,
            3,
            1L,
            LocalDateTime.of(2026, 3, 18, 10, 0),
            LocalDateTime.of(2026, 3, 20, 10, 0),
            LocalDateTime.of(2026, 3, 21, 10, 0)
        );
    }

    /**
     * 영양소가 null인 상품 (food 매칭 안 된 상태).
     */
    public static ProductSyncData withNullNutrition(Long id) {
        return new ProductSyncData(
            id,
            "영양소없음 " + id,
            "설명",
            5000L,
            50,
            null,
            0,
            null,
            2L,
            "간식",
            null,
            null,
            null,
            null,
            null,
            2L,
            LocalDateTime.of(2026, 3, 22, 10, 0),
            LocalDateTime.of(2026, 3, 22, 10, 0),
            null
        );
    }
}
