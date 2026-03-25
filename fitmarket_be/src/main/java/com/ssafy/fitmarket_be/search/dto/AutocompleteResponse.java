package com.ssafy.fitmarket_be.search.dto;

import java.util.List;

/**
 * 자동완성 응답 DTO.
 * Controller에서 ApiResponse&lt;AutocompleteResponse&gt;로 래핑하여 반환한다.
 *
 * @param products 상품명 자동완성 결과
 */
public record AutocompleteResponse(
    List<ProductSuggestionResponse> products
) {}
