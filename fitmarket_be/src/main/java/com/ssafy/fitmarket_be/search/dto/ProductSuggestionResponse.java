package com.ssafy.fitmarket_be.search.dto;

/**
 * 상품 자동완성 응답 DTO.
 *
 * @param id           상품 ID
 * @param name         상품명
 * @param categoryName 카테고리명
 * @param imageUrl     상품 이미지 URL
 */
public record ProductSuggestionResponse(
    Long id,
    String name,
    String categoryName,
    String imageUrl
) {}
