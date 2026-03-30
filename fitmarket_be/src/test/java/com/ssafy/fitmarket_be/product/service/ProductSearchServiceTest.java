package com.ssafy.fitmarket_be.product.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import com.ssafy.fitmarket_be.global.dto.PageResponse;
import com.ssafy.fitmarket_be.product.domain.Product;
import com.ssafy.fitmarket_be.product.domain.ProductFixture;
import com.ssafy.fitmarket_be.product.dto.ProductListResponse;
import com.ssafy.fitmarket_be.product.dto.search.SearchHitSource;
import com.ssafy.fitmarket_be.product.dto.search.SearchNutritionHit;
import com.ssafy.fitmarket_be.product.repository.ProductMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductSearchService")
class ProductSearchServiceTest {

    @Mock
    private ElasticsearchClient esClient;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductSearchService productSearchService;

    // ----- Helper: SearchHitSource 생성 -----

    private SearchHitSource createHitSource(
            Long id, String name, Float rating, Long categoryId, String categoryName) {
        SearchHitSource source = new SearchHitSource();
        ReflectionTestUtils.setField(source, "id", id);
        ReflectionTestUtils.setField(source, "name", name);
        ReflectionTestUtils.setField(source, "description", "테스트 설명 " + name);
        ReflectionTestUtils.setField(source, "price", 10000L);
        ReflectionTestUtils.setField(source, "stock", 100);
        ReflectionTestUtils.setField(source, "rating", rating);
        ReflectionTestUtils.setField(source, "reviewCount", 10);
        ReflectionTestUtils.setField(source, "imageUrl", "https://img.test/" + id);
        ReflectionTestUtils.setField(source, "categoryId", categoryId);
        ReflectionTestUtils.setField(source, "categoryName", categoryName);

        SearchNutritionHit nutrition = new SearchNutritionHit();
        ReflectionTestUtils.setField(nutrition, "calories", 250f);
        ReflectionTestUtils.setField(nutrition, "protein", 30f);
        ReflectionTestUtils.setField(nutrition, "carbs", 10f);
        ReflectionTestUtils.setField(nutrition, "fat", 5f);
        ReflectionTestUtils.setField(source, "nutrition", nutrition);

        return source;
    }

    // ===== 케이스 1: ES 정상 검색 - 하이라이트 포함 결과 반환 =====

    @Test
    @DisplayName("search: 정상 ES 검색 시 하이라이트 포함 결과를 반환한다")
    @SuppressWarnings("unchecked")
    void search_정상ES검색_하이라이트포함결과반환() throws IOException {
        // given
        SearchHitSource src1 = createHitSource(1L, "닭가슴살 샐러드", 4.5f, 1L, "단백질");
        SearchHitSource src2 = createHitSource(2L, "닭가슴살 스테이크", 4.2f, 1L, "단백질");

        SearchResponse<SearchHitSource> searchResponse = mock(SearchResponse.class);
        HitsMetadata<SearchHitSource> hitsMetadata = mock(HitsMetadata.class);
        Hit<SearchHitSource> hit1 = mock(Hit.class);
        Hit<SearchHitSource> hit2 = mock(Hit.class);
        TotalHits totalHits = new TotalHits.Builder().value(2).relation(TotalHitsRelation.Eq).build();

        given(esClient.search(any(Function.class), eq(SearchHitSource.class))).willReturn(searchResponse);
        given(searchResponse.hits()).willReturn(hitsMetadata);
        given(hitsMetadata.hits()).willReturn(List.of(hit1, hit2));
        given(hitsMetadata.total()).willReturn(totalHits);

        given(hit1.source()).willReturn(src1);
        given(hit1.highlight()).willReturn(Map.of(
                "name", List.of("<em>닭가슴살</em> 샐러드")
        ));
        given(hit2.source()).willReturn(src2);
        given(hit2.highlight()).willReturn(Map.of(
                "name", List.of("<em>닭가슴살</em> 스테이크")
        ));

        // when
        PageResponse<ProductListResponse> result = productSearchService.search("닭가슴살", null, 1, 20);

        // then
        assertThat(result.content()).hasSize(2);
        assertThat(result.content().get(0).highlightedName()).contains("<em>");
        assertThat(result.content().get(1).highlightedName()).contains("<em>");
        assertThat(result.totalElements()).isEqualTo(2);

        // MySQL fallback이 실행되지 않았음을 검증
        verify(productMapper, never()).selectProductsWithFilters(any(), any(), anyInt(), anyInt());
    }

    // ===== 케이스 2: ES 장애 시 MySQL Fallback 반환 =====

    @Test
    @DisplayName("search: ES 장애 시 MySQL Fallback으로 결과를 반환한다")
    @SuppressWarnings("unchecked")
    void search_ES장애시_MySQLFallback반환() throws IOException {
        // given
        given(esClient.search(any(Function.class), eq(SearchHitSource.class)))
                .willThrow(new IOException("ES connection refused"));

        Product product = ProductFixture.create(1L, 10);
        given(productMapper.selectProductsWithFilters(isNull(), eq("닭가슴살"), eq(20), eq(0)))
                .willReturn(List.of(product));
        given(productMapper.countProductsWithFilters(isNull(), eq("닭가슴살")))
                .willReturn(1L);

        // when
        PageResponse<ProductListResponse> result = productSearchService.search("닭가슴살", null, 1, 20);

        // then
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).highlightedName()).isNull();
        assertThat(result.content().get(0).highlightedDescription()).isNull();
        assertThat(result.totalElements()).isEqualTo(1);
    }

    // ===== 케이스 3: categoryId 필터 - ES 쿼리 포함 검증 =====

    @Test
    @DisplayName("search: categoryId 필터가 있으면 ES 쿼리에 포함된다")
    @SuppressWarnings("unchecked")
    void search_categoryId필터_ES쿼리포함검증() throws IOException {
        // given
        SearchHitSource src = createHitSource(1L, "프로틴바", 4.0f, 3L, "간식");

        SearchResponse<SearchHitSource> searchResponse = mock(SearchResponse.class);
        HitsMetadata<SearchHitSource> hitsMetadata = mock(HitsMetadata.class);
        Hit<SearchHitSource> hit = mock(Hit.class);
        TotalHits totalHits = new TotalHits.Builder().value(1).relation(TotalHitsRelation.Eq).build();

        given(esClient.search(any(Function.class), eq(SearchHitSource.class))).willReturn(searchResponse);
        given(searchResponse.hits()).willReturn(hitsMetadata);
        given(hitsMetadata.hits()).willReturn(List.of(hit));
        given(hitsMetadata.total()).willReturn(totalHits);
        given(hit.source()).willReturn(src);
        given(hit.highlight()).willReturn(Map.of());

        // when
        PageResponse<ProductListResponse> result = productSearchService.search("프로틴바", 3L, 1, 20);

        // then
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).categoryId()).isEqualTo(3L);

        verify(esClient).search(any(Function.class), eq(SearchHitSource.class));
    }

    // ===== 케이스 4: 빈 결과 - 빈 리스트 반환 =====

    @Test
    @DisplayName("search: 검색 결과가 없으면 빈 리스트를 반환한다")
    @SuppressWarnings("unchecked")
    void search_빈결과_빈리스트반환() throws IOException {
        // given
        SearchResponse<SearchHitSource> searchResponse = mock(SearchResponse.class);
        HitsMetadata<SearchHitSource> hitsMetadata = mock(HitsMetadata.class);
        TotalHits totalHits = new TotalHits.Builder().value(0).relation(TotalHitsRelation.Eq).build();

        given(esClient.search(any(Function.class), eq(SearchHitSource.class))).willReturn(searchResponse);
        given(searchResponse.hits()).willReturn(hitsMetadata);
        given(hitsMetadata.hits()).willReturn(List.of());
        given(hitsMetadata.total()).willReturn(totalHits);

        // when
        PageResponse<ProductListResponse> result = productSearchService.search("존재하지않는키워드", null, 1, 20);

        // then
        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isEqualTo(0);
        assertThat(result.totalPages()).isEqualTo(0);
    }

    // ===== 케이스 5: hit.source() == null 건너뛰기 =====

    @Test
    @DisplayName("ES 결과에 source가 null인 hit이 포함되면 해당 hit을 건너뛴다")
    @SuppressWarnings("unchecked")
    void search_sourceNull_건너뛰기() throws IOException {
        // given: 2개 hit 중 1개 source==null
        SearchHitSource src1 = createHitSource(1L, "닭가슴살 샐러드", 4.5f, 1L, "단백질");

        SearchResponse<SearchHitSource> searchResponse = mock(SearchResponse.class);
        HitsMetadata<SearchHitSource> hitsMetadata = mock(HitsMetadata.class);
        Hit<SearchHitSource> hit1 = mock(Hit.class);
        Hit<SearchHitSource> hitNull = mock(Hit.class);
        TotalHits totalHits = new TotalHits.Builder().value(2).relation(TotalHitsRelation.Eq).build();

        given(esClient.search(any(Function.class), eq(SearchHitSource.class))).willReturn(searchResponse);
        given(searchResponse.hits()).willReturn(hitsMetadata);
        given(hitsMetadata.hits()).willReturn(List.of(hit1, hitNull));
        given(hitsMetadata.total()).willReturn(totalHits);

        given(hit1.source()).willReturn(src1);
        given(hit1.highlight()).willReturn(Map.of("name", List.of("<em>닭가슴살</em> 샐러드")));
        given(hitNull.source()).willReturn(null);

        // when
        PageResponse<ProductListResponse> result = productSearchService.search("닭가슴살", null, 1, 20);

        // then: source가 null인 hit은 건너뛰어 결과 1개만 반환
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).name()).isEqualTo("닭가슴살 샐러드");
        assertThat(result.totalElements()).isEqualTo(2);

        verify(productMapper, never()).selectProductsWithFilters(any(), any(), anyInt(), anyInt());
    }

    // ===== 케이스 6: totalHits == null → content 크기를 totalElements로 사용 =====

    @Test
    @DisplayName("ES 결과의 totalHits가 null이면 content 크기를 totalElements로 사용한다")
    @SuppressWarnings("unchecked")
    void search_totalHitsNull_contentSize사용() throws IOException {
        // given: hitsMetadata.total() → null, hits 2개
        SearchHitSource src1 = createHitSource(1L, "프로틴바 A", 4.0f, 1L, "단백질");
        SearchHitSource src2 = createHitSource(2L, "프로틴바 B", 3.8f, 1L, "단백질");

        SearchResponse<SearchHitSource> searchResponse = mock(SearchResponse.class);
        HitsMetadata<SearchHitSource> hitsMetadata = mock(HitsMetadata.class);
        Hit<SearchHitSource> hit1 = mock(Hit.class);
        Hit<SearchHitSource> hit2 = mock(Hit.class);

        given(esClient.search(any(Function.class), eq(SearchHitSource.class))).willReturn(searchResponse);
        given(searchResponse.hits()).willReturn(hitsMetadata);
        given(hitsMetadata.hits()).willReturn(List.of(hit1, hit2));
        given(hitsMetadata.total()).willReturn(null);

        given(hit1.source()).willReturn(src1);
        given(hit1.highlight()).willReturn(Map.of());
        given(hit2.source()).willReturn(src2);
        given(hit2.highlight()).willReturn(Map.of());

        // when
        PageResponse<ProductListResponse> result = productSearchService.search("프로틴바", null, 1, 20);

        // then: totalHits가 null이므로 content.size() == 2가 totalElements로 사용됨
        assertThat(result.content()).hasSize(2);
        assertThat(result.totalElements()).isEqualTo(2);

        verify(productMapper, never()).selectProductsWithFilters(any(), any(), anyInt(), anyInt());
    }

    // ===== 케이스 7: 페이지 기본값 보정 - 안전한 기본값 적용 =====

    @Test
    @DisplayName("search: page=0, size=0이면 page=1, size=20으로 보정된다")
    @SuppressWarnings("unchecked")
    void search_페이지기본값보정_안전한기본값적용() throws IOException {
        // given
        SearchResponse<SearchHitSource> searchResponse = mock(SearchResponse.class);
        HitsMetadata<SearchHitSource> hitsMetadata = mock(HitsMetadata.class);
        TotalHits totalHits = new TotalHits.Builder().value(0).relation(TotalHitsRelation.Eq).build();

        given(esClient.search(any(Function.class), eq(SearchHitSource.class))).willReturn(searchResponse);
        given(searchResponse.hits()).willReturn(hitsMetadata);
        given(hitsMetadata.hits()).willReturn(List.of());
        given(hitsMetadata.total()).willReturn(totalHits);

        // when
        PageResponse<ProductListResponse> result = productSearchService.search("테스트", null, 0, 0);

        // then
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.size()).isEqualTo(20);
    }
}
