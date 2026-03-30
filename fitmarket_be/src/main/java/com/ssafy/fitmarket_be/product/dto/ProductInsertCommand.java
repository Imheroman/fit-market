package com.ssafy.fitmarket_be.product.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class ProductInsertCommand {
    @Setter  // MyBatis useGeneratedKeys가 id를 세팅하기 위해 필요
    private Long id;
    private final Long userId;
    private final Long categoryId;
    private final String name;
    private final String description;
    private final Long price;
    private final Integer weightG;
    private final Integer stock;
    private final String imageUrl;
    private final Long foodId;
}
