package com.ssafy.fitmarket_be.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.ErrorResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch._types.ErrorCause;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggest;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggestOption;
import co.elastic.clients.elasticsearch.core.search.Suggestion;
import com.ssafy.fitmarket_be.common.util.HangulUtils;
import com.ssafy.fitmarket_be.product.document.ProductDocument;
import com.ssafy.fitmarket_be.product.domain.ProductDocumentFixture;
import com.ssafy.fitmarket_be.search.dto.ProductSuggestionResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchAutocompleteService")
class SearchAutocompleteServiceTest {

    @Mock
    private ElasticsearchClient esClient;

    @InjectMocks
    private SearchAutocompleteService searchAutocompleteService;

    // ===== HangulUtils 단위 테스트 =====

    @Nested
    @DisplayName("HangulUtils.decompose")
    class DecomposeTest {

        @Test
        @DisplayName("한글 문자열을 자모 단위로 분해한다")
        void decompose_한글자모분해() {
            // given
            String input = "닭가슴살";

            // when
            String result = HangulUtils.decompose(input);

            // then — 닭: ㄷㅏㄺ → ㄷㅏㄹㄱ (종성 ㄺ은 단일 코드), 가: ㄱㅏ, 슴: ㅅㅡㅁ, 살: ㅅㅏㄹ
            assertThat(result).isNotEmpty();
            // 초성 ㄷ으로 시작
            assertThat(result.charAt(0)).isEqualTo('ㄷ');
            // 원본보다 길어야 함 (자모 분해되므로)
            assertThat(result.length()).isGreaterThan(input.length());
        }

        @Test
        @DisplayName("영문/숫자 혼합 입력은 비한글 문자를 그대로 통과시킨다")
        void decompose_영문숫자혼합_그대로통과() {
            // given
            String input = "protein123";

            // when
            String result = HangulUtils.decompose(input);

            // then
            assertThat(result).isEqualTo("protein123");
        }

        @Test
        @DisplayName("null 입력 시 빈 문자열을 반환한다")
        void decompose_null입력_빈문자열반환() {
            // when
            String result = HangulUtils.decompose(null);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("HangulUtils.extractChosung")
    class ExtractChosungTest {

        @Test
        @DisplayName("한글 문자열에서 초성만 추출한다")
        void extractChosung_초성추출() {
            // given
            String input = "닭가슴살";

            // when
            String result = HangulUtils.extractChosung(input);

            // then
            assertThat(result).isEqualTo("ㄷㄱㅅㅅ");
        }

        @Test
        @DisplayName("비한글 문자는 그대로 통과시킨다")
        void extractChosung_비한글문자_그대로통과() {
            // given
            String input = "ABC닭123";

            // when
            String result = HangulUtils.extractChosung(input);

            // then
            assertThat(result).isEqualTo("ABCㄷ123");
        }

        @Test
        @DisplayName("null 입력 시 빈 문자열을 반환한다")
        void extractChosung_null입력_빈문자열반환() {
            // when
            String result = HangulUtils.extractChosung(null);

            // then
            assertThat(result).isEmpty();
        }
    }

    // ===== getSuggestions 테스트 =====

    @Nested
    @DisplayName("getSuggestions")
    class GetSuggestionsTest {

        @Test
        @DisplayName("정상 조회 시 ES Completion Suggest 결과를 ProductSuggestionResponse 리스트로 반환한다")
        @SuppressWarnings("unchecked")
        void getSuggestions_정상조회_결과반환() throws IOException {
            // given
            ProductDocument doc1 = ProductDocumentFixture.create(1L, "닭가슴살 샐러드", 4.5f);
            ProductDocument doc2 = ProductDocumentFixture.create(2L, "닭가슴살 스테이크", 4.2f);

            CompletionSuggestOption<ProductDocument> option1 = mock(CompletionSuggestOption.class);
            given(option1.id()).willReturn("1");
            given(option1.source()).willReturn(doc1);

            CompletionSuggestOption<ProductDocument> option2 = mock(CompletionSuggestOption.class);
            given(option2.id()).willReturn("2");
            given(option2.source()).willReturn(doc2);

            CompletionSuggest<ProductDocument> completionSuggest = mock(CompletionSuggest.class);
            given(completionSuggest.options()).willReturn(List.of(option1, option2));

            Suggestion<ProductDocument> suggestion = mock(Suggestion.class);
            given(suggestion.completion()).willReturn(completionSuggest);

            Map<String, List<Suggestion<ProductDocument>>> suggestMap = Map.of(
                "product-suggest", List.of(suggestion)
            );

            SearchResponse<ProductDocument> searchResponse = mock(SearchResponse.class);
            given(searchResponse.suggest()).willReturn(suggestMap);

            given(esClient.search(any(Function.class), eq(ProductDocument.class)))
                .willReturn(searchResponse);

            // when
            List<ProductSuggestionResponse> result = searchAutocompleteService.getSuggestions("닭가슴살", 8);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).id()).isEqualTo(1L);
            assertThat(result.get(0).name()).isEqualTo("닭가슴살 샐러드");
            assertThat(result.get(0).categoryName()).isEqualTo("단백질");
            assertThat(result.get(0).imageUrl()).isEqualTo("https://img.test/1");
            assertThat(result.get(1).id()).isEqualTo(2L);
            assertThat(result.get(1).name()).isEqualTo("닭가슴살 스테이크");

            verify(esClient).search(any(Function.class), eq(ProductDocument.class));
        }

        @Test
        @DisplayName("ES에서 빈 suggest response가 반환되면 빈 리스트를 반환한다")
        @SuppressWarnings("unchecked")
        void getSuggestions_빈결과_빈리스트반환() throws IOException {
            // given
            CompletionSuggest<ProductDocument> completionSuggest = mock(CompletionSuggest.class);
            given(completionSuggest.options()).willReturn(List.of());

            Suggestion<ProductDocument> suggestion = mock(Suggestion.class);
            given(suggestion.completion()).willReturn(completionSuggest);

            Map<String, List<Suggestion<ProductDocument>>> suggestMap = Map.of(
                "product-suggest", List.of(suggestion)
            );

            SearchResponse<ProductDocument> searchResponse = mock(SearchResponse.class);
            given(searchResponse.suggest()).willReturn(suggestMap);

            given(esClient.search(any(Function.class), eq(ProductDocument.class)))
                .willReturn(searchResponse);

            // when
            List<ProductSuggestionResponse> result = searchAutocompleteService.getSuggestions("존재하지않는상품", 8);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("빈 prefix 입력 시 ES 호출 없이 빈 리스트를 반환한다")
        @SuppressWarnings("unchecked")
        void getSuggestions_빈prefix_빈리스트반환() throws IOException {
            // when
            List<ProductSuggestionResponse> result = searchAutocompleteService.getSuggestions("   ", 8);

            // then
            assertThat(result).isEmpty();
            verify(esClient, never()).search(any(Function.class), eq(ProductDocument.class));
        }

        @Test
        @DisplayName("ElasticsearchException 발생 시 빈 리스트를 반환한다")
        @SuppressWarnings("unchecked")
        void getSuggestions_ES예외_빈리스트반환() throws IOException {
            // given
            ErrorResponse errorResponse = ErrorResponse.of(b -> b
                .error(ErrorCause.of(e -> e.type("search_phase_execution_exception").reason("test error")))
                .status(500)
            );
            given(esClient.search(any(Function.class), eq(ProductDocument.class)))
                .willThrow(new ElasticsearchException("test-endpoint", errorResponse));

            // when
            List<ProductSuggestionResponse> result = searchAutocompleteService.getSuggestions("닭가슴살", 8);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("IOException 발생 시 빈 리스트를 반환한다")
        @SuppressWarnings("unchecked")
        void getSuggestions_IOException_빈리스트반환() throws IOException {
            // given
            given(esClient.search(any(Function.class), eq(ProductDocument.class)))
                .willThrow(new IOException("ES connection refused"));

            // when
            List<ProductSuggestionResponse> result = searchAutocompleteService.getSuggestions("닭가슴살", 8);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("esClient가 null이면 ES 비활성화 상태로 빈 리스트를 반환한다")
        void getSuggestions_esClientNull_빈리스트반환() {
            // given
            SearchAutocompleteService serviceWithNullClient = new SearchAutocompleteService(null);

            // when
            List<ProductSuggestionResponse> result = serviceWithNullClient.getSuggestions("닭가슴살", 8);

            // then
            assertThat(result).isEmpty();
        }
    }
}
