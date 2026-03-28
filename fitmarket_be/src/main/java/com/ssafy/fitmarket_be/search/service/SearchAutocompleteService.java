package com.ssafy.fitmarket_be.search.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ssafy.fitmarket_be.common.util.HangulUtils;
import com.ssafy.fitmarket_be.search.dto.ProductSuggestionResponse;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class SearchAutocompleteService {

    private final ElasticsearchClient esClient;

    public SearchAutocompleteService(
            @Autowired(required = false) ElasticsearchClient esClient) {
        this.esClient = esClient;
    }

    /**
     * ES Completion Suggester를 사용하여 prefix에 매칭되는 상품 목록을 조회한다.
     *
     * @param prefix 검색 prefix (최소 1자)
     * @param limit  최대 반환 개수 (기본 8)
     */
    public List<ProductSuggestionResponse> getSuggestions(String prefix, int limit) {
        if (esClient == null) {
            log.warn("ES 비활성화 상태: 자동완성 빈 리스트 반환");
            return List.of();
        }

        String normalized = prefix.trim().toLowerCase();
        if (normalized.isEmpty()) return List.of();

        String decomposed = HangulUtils.decompose(normalized);

        try {
            SearchResponse<SuggestHitSource> response = esClient.search(s -> s
                .index("products")
                .suggest(sg -> sg
                    .suggesters("product-suggest", fs -> fs
                        .prefix(decomposed)
                        .completion(c -> c
                            .field("suggest")
                            .skipDuplicates(true)
                            .size(limit)
                        )
                    )
                ),
                SuggestHitSource.class
            );

            return response.suggest().get("product-suggest").stream()
                .flatMap(s -> s.completion().options().stream())
                .map(option -> new ProductSuggestionResponse(
                    Long.parseLong(option.id()),
                    option.source().name,
                    option.source().categoryName,
                    option.source().imageUrl
                ))
                .toList();
        } catch (ElasticsearchException | IOException e) {
            log.warn("ES autocomplete 쿼리 실패, 빈 결과 반환: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Completion Suggest 응답 역직렬화 전용 경량 DTO.
     * ProductDocument를 직접 사용하면 LocalDateTime·Completion 등
     * ES Java Client의 Jackson 매퍼가 처리하지 못하는 타입 때문에 역직렬화 실패.
     */
    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class SuggestHitSource {
        private String name;
        private String categoryName;
        private String imageUrl;
    }
}
