package com.ssafy.fitmarket_be.product.dto.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ES 검색 응답 역직렬화 전용 경량 DTO.
 * ProductDocument를 직접 사용하면 LocalDateTime/Completion 등
 * ES Java Client의 Jackson 매퍼가 처리하지 못하는 타입 때문에 역직렬화 실패.
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchHitSource {
    private Long id;
    private String name;
    private String description;
    private Long price;
    private Integer stock;
    private Float rating;
    private Integer reviewCount;
    private String imageUrl;
    private Long categoryId;
    private String categoryName;
    private SearchNutritionHit nutrition;
}
