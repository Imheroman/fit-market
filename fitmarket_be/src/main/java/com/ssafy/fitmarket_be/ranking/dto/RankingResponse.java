package com.ssafy.fitmarket_be.ranking.dto;

import com.ssafy.fitmarket_be.product.domain.Product;

public record RankingResponse(
    int rank,
    Long productId,
    String productName,
    long price,
    String imageUrl,
    double score
) {
    public static RankingResponse of(int rank, Product product, Double score) {
        return new RankingResponse(
            rank,
            product.getId(),
            product.getName(),
            product.getPrice(),
            product.getImageUrl(),
            score != null ? score : 0.0
        );
    }
}
