package com.ssafy.fitmarket_be.product.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ssafy.fitmarket_be.global.dto.PageResponse;
import com.ssafy.fitmarket_be.product.dto.ProductListResponse;
import com.ssafy.fitmarket_be.product.repository.ProductMapper;
import com.ssafy.fitmarket_be.product.domain.Product;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ProductSearchService {

    private final ElasticsearchClient esClient;
    private final ProductMapper productMapper;

    public ProductSearchService(
            @Autowired(required = false) ElasticsearchClient esClient,
            ProductMapper productMapper) {
        this.esClient = esClient;
        this.productMapper = productMapper;
    }

    /**
     * ES 기반 검색 (기본) - 실패 시 MySQL Fallback.
     *
     * @param keyword    검색 키워드
     * @param categoryId 카테고리 필터 (optional)
     * @param page       페이지 번호 (1-based)
     * @param size       페이지 크기
     * @return 검색 결과 (PageResponse)
     */
    public PageResponse<ProductListResponse> search(String keyword, Long categoryId,
                                                    int page, int size) {
        if (esClient != null) {
            try {
                return searchFromElasticsearch(keyword, categoryId, page, size);
            } catch (Exception e) {
                log.warn("ES 검색 실패, MySQL Fallback: {}", e.getMessage());
            }
        }
        return searchFromMySQL(keyword, categoryId, page, size);
    }

    /**
     * Elasticsearch multi_match + function_score 검색.
     * 하이라이팅: name, description, categoryName 필드에 em 태그 적용.
     */
    private PageResponse<ProductListResponse> searchFromElasticsearch(
            String keyword, Long categoryId, int page, int size) throws IOException {

        int safePage = (page < 1) ? 1 : page;
        int safeSize = (size < 1) ? 20 : size;
        int from = (safePage - 1) * safeSize;

        SearchResponse<SearchHitSource> response = esClient.search(s -> s
                .index("products")
                .query(q -> q
                        .functionScore(fs -> fs
                                .query(innerQ -> innerQ
                                        .bool(b -> {
                                            b.must(m -> m
                                                    .multiMatch(mm -> mm
                                                            .query(keyword)
                                                            .fields("name^3", "categoryName^2",
                                                                    "description")
                                                            .type(TextQueryType.BestFields)
                                                            .analyzer("korean_analyzer")
                                                    )
                                            );
                                            if (categoryId != null) {
                                                b.filter(f -> f
                                                        .term(t -> t
                                                                .field("categoryId")
                                                                .value(categoryId)
                                                        )
                                                );
                                            }
                                            return b;
                                        })
                                )
                                .functions(fn -> fn
                                        .fieldValueFactor(fvf -> fvf
                                                .field("rating")
                                                .factor(1.2)
                                                .modifier(co.elastic.clients.elasticsearch._types.query_dsl.FieldValueFactorModifier.Log1p)
                                                .missing(3.0)
                                        )
                                )
                                .functions(fn -> fn
                                        .fieldValueFactor(fvf -> fvf
                                                .field("reviewCount")
                                                .factor(1.1)
                                                .modifier(co.elastic.clients.elasticsearch._types.query_dsl.FieldValueFactorModifier.Log1p)
                                                .missing(0.0)
                                        )
                                )
                                .scoreMode(FunctionScoreMode.Sum)
                                .boostMode(FunctionBoostMode.Multiply)
                        )
                )
                .highlight(h -> h
                        .fields("name", f -> f.preTags("<em>").postTags("</em>"))
                        .fields("description", f -> f.preTags("<em>").postTags("</em>"))
                        .fields("categoryName", f -> f.preTags("<em>").postTags("</em>"))
                )
                .from(from)
                .size(safeSize),
                SearchHitSource.class
        );

        // 결과 변환
        List<ProductListResponse> content = new ArrayList<>();
        for (Hit<SearchHitSource> hit : response.hits().hits()) {
            SearchHitSource doc = hit.source();
            if (doc == null) continue;

            Map<String, List<String>> highlights = hit.highlight();
            content.add(toListResponse(doc, highlights));
        }

        // 총 개수
        TotalHits totalHits = response.hits().total();
        long totalElements = (totalHits != null) ? totalHits.value() : content.size();
        int totalPages = (int) Math.ceil((double) totalElements / safeSize);
        boolean hasNext = safePage < totalPages;
        boolean hasPrevious = safePage > 1;

        return new PageResponse<>(
                content,
                safePage,
                safeSize,
                totalElements,
                totalPages,
                hasNext,
                hasPrevious
        );
    }

    /**
     * MySQL Fallback 검색.
     * ES 장애 시 기존 ProductMapper LIKE 검색으로 자동 전환.
     */
    private PageResponse<ProductListResponse> searchFromMySQL(
            String keyword, Long categoryId, int page, int size) {

        log.info("MySQL Fallback 검색 실행: keyword={}, categoryId={}", keyword, categoryId);

        int safePage = (page < 1) ? 1 : page;
        int safeSize = (size < 1) ? 20 : size;
        int offset = (safePage - 1) * safeSize;

        String normalizedKeyword = (keyword != null && !keyword.trim().isEmpty())
                ? keyword.trim() : null;

        List<Product> products = productMapper.selectProductsWithFilters(
                categoryId, normalizedKeyword, safeSize, offset);
        List<ProductListResponse> content = products.stream()
                .map(ProductListResponse::from)
                .toList();

        long totalElements = productMapper.countProductsWithFilters(categoryId, normalizedKeyword);
        int totalPages = (int) Math.ceil((double) totalElements / safeSize);
        boolean hasNext = safePage < totalPages;
        boolean hasPrevious = safePage > 1;

        return new PageResponse<>(
                content,
                safePage,
                safeSize,
                totalElements,
                totalPages,
                hasNext,
                hasPrevious
        );
    }

    /**
     * SearchHitSource + 하이라이트 정보를 ProductListResponse로 변환.
     */
    private ProductListResponse toListResponse(SearchHitSource doc,
                                               Map<String, List<String>> highlights) {
        String highlightedName = (highlights != null && highlights.containsKey("name"))
                ? String.join(" ", highlights.get("name"))
                : null;
        String highlightedDescription = (highlights != null && highlights.containsKey("description"))
                ? String.join(" ", highlights.get("description"))
                : null;

        SearchNutritionHit nutrition = doc.getNutrition();
        int calories = (nutrition != null && nutrition.getCalories() != null)
                ? nutrition.getCalories().intValue() : 0;
        int protein = (nutrition != null && nutrition.getProtein() != null)
                ? nutrition.getProtein().intValue() : 0;
        int carbs = (nutrition != null && nutrition.getCarbs() != null)
                ? nutrition.getCarbs().intValue() : 0;
        int fat = (nutrition != null && nutrition.getFat() != null)
                ? nutrition.getFat().intValue() : 0;

        return new ProductListResponse(
                doc.getId(),
                doc.getName(),
                doc.getDescription(),
                doc.getCategoryId(),
                doc.getCategoryName(),
                doc.getPrice(),
                doc.getStock() != null ? doc.getStock() : 0,
                doc.getImageUrl(),
                doc.getRating() != null ? Math.round(doc.getRating() * 10.0) / 10.0 : 0.0,
                doc.getReviewCount() != null ? doc.getReviewCount() : 0,
                calories,
                protein,
                carbs,
                fat,
                highlightedName,
                highlightedDescription
        );
    }

    /**
     * ES 검색 응답 역직렬화 전용 경량 DTO.
     * ProductDocument를 직접 사용하면 LocalDateTime·Completion 등
     * ES Java Client의 Jackson 매퍼가 처리하지 못하는 타입 때문에 역직렬화 실패.
     */
    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class SearchHitSource {
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

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class SearchNutritionHit {
        private Float calories;
        private Float protein;
        private Float carbs;
        private Float fat;
    }
}
