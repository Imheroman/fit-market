package com.ssafy.fitmarket_be.search.controller;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Conflicts;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ssafy.fitmarket_be.common.util.HangulUtils;
import com.ssafy.fitmarket_be.config.RedisContainerInitializer;
import com.ssafy.fitmarket_be.product.integration.ElasticsearchTestConfig;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SearchAutocompleteController 통합 테스트.
 *
 * <p>Testcontainers ES 인스턴스 + 전체 Spring 컨텍스트를 사용하여
 * 자동완성 API 엔드포인트를 검증한다.
 *
 * <p>인덱스 매핑에 top-level {@code suggest} completion 필드를 포함하며,
 * 각 문서에는 원본 이름·자모 분해·초성을 suggest input으로 설정한다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(initializers = RedisContainerInitializer.class)
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude=",
        "search.elasticsearch.enabled=true",
        "search.elasticsearch.init-index=false"
})
@DisplayName("SearchAutocompleteController 통합 테스트")
class SearchAutocompleteControllerTest extends ElasticsearchTestConfig {

    @Autowired
    MockMvc mockMvc;

    private static ElasticsearchClient esClient;
    private static RestClient restClient;

    @BeforeAll
    static void initClient() {
        restClient = RestClient.builder(
                new HttpHost(ES_CONTAINER.getHost(), ES_CONTAINER.getMappedPort(9200))
        ).build();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper(mapper));
        esClient = new ElasticsearchClient(transport);
    }

    @AfterAll
    static void closeClient() throws IOException {
        if (restClient != null) restClient.close();
    }

    @BeforeEach
    void setUp() throws IOException {
        boolean exists = esClient.indices().exists(e -> e.index("products")).value();
        if (exists) {
            // Spring Data ES가 자동 생성한 인덱스를 재사용 — 문서만 초기화
            esClient.deleteByQuery(d -> d.index("products").conflicts(Conflicts.Proceed).query(q -> q.matchAll(m -> m)));
        } else {
            try (InputStream settings = getClass().getResourceAsStream("/elasticsearch/product-settings.json");
                 InputStream mappings = getClass().getResourceAsStream("/elasticsearch/product-mappings.json")) {
                try {
                    esClient.indices().create(c -> c
                            .index("products")
                            .withJson(buildCreateIndexBody(settings, mappings))
                    );
                } catch (ElasticsearchException e) {
                    // 배치 동기화와의 경쟁 조건 — 이미 생성된 인덱스의 문서만 초기화
                    esClient.deleteByQuery(d -> d.index("products").conflicts(Conflicts.Proceed).query(q -> q.matchAll(m -> m)));
                }
            }
        }

        indexTestDocuments();
    }

    // -- Test Cases --

    @Test
    @DisplayName("GET /search/autocomplete — 정상 조회 시 200과 올바른 응답 구조를 반환한다")
    void autocomplete_정상조회_200응답() throws Exception {
        mockMvc.perform(get("/search/autocomplete")
                        .param("q", "닭가"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.products").isArray())
                .andExpect(jsonPath("$.error").isEmpty());
    }

    @Test
    @DisplayName("GET /search/autocomplete — q 파라미터 누락 시 400 Bad Request를 반환한다")
    void autocomplete_q파라미터누락_400반환() throws Exception {
        mockMvc.perform(get("/search/autocomplete"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /search/autocomplete — 인증 토큰 없이도 200으로 접근 가능하다")
    void autocomplete_비로그인접근_200허용() throws Exception {
        mockMvc.perform(get("/search/autocomplete")
                        .param("q", "프로틴"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.products").isArray());
    }

    @Test
    @DisplayName("GET /search/autocomplete — limit 파라미터로 최대 반환 개수를 제한한다")
    void autocomplete_limit파라미터_최대개수제한() throws Exception {
        mockMvc.perform(get("/search/autocomplete")
                        .param("q", "프로틴")
                        .param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.products").isArray())
                .andExpect(jsonPath("$.data.products.length()").value(
                        org.hamcrest.Matchers.lessThanOrEqualTo(3)));
    }

    @Test
    @DisplayName("GET /search/autocomplete — q가 공백 문자열이면 400 Bad Request를 반환한다")
    void autocomplete_q공백문자열_400반환() throws Exception {
        mockMvc.perform(get("/search/autocomplete")
                        .param("q", "   "))
                .andExpect(status().isBadRequest());
    }

    // -- Helpers --

    /**
     * settings.json + mappings.json을 하나의 create-index 요청 body로 합친다.
     * product-mappings.json에 top-level suggest completion 필드가 이미 포함되어 있다.
     */
    @SuppressWarnings("unchecked")
    private InputStream buildCreateIndexBody(InputStream settings, InputStream mappings) {
        try {
            ObjectMapper om = new ObjectMapper();
            Map<String, Object> s = om.readValue(settings, Map.class);
            Map<String, Object> m = om.readValue(mappings, Map.class);

            Map<String, Object> body = Map.of("settings", s, "mappings", m);
            byte[] bytes = om.writeValueAsBytes(body);
            return new ByteArrayInputStream(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Completion Suggester 테스트를 위한 문서를 인덱싱한다.
     * ProductSearchSyncHandler.buildSuggestInput 로직과 동일하게
     * name, 자모 분해, 초성을 suggest input으로 설정한다.
     */
    private void indexTestDocuments() throws IOException {
        indexDocument(1L, "닭가슴살 프로틴바", "고단백 닭가슴살 간식", 15000L, "단백질", 1L);
        indexDocument(2L, "프로틴 쉐이크 바닐라", "고단백 쉐이크", 12000L, "단백질", 1L);
        indexDocument(3L, "프로틴 쉐이크 초콜릿", "초콜릿맛 프로틴", 13000L, "단백질", 1L);
        indexDocument(4L, "프로틴 파우더", "순수 단백질 파우더", 20000L, "보충제", 2L);
        indexDocument(5L, "저탄고지 샐러드", "저탄수화물 고지방 샐러드", 8000L, "식단", 3L);

        esClient.indices().refresh(r -> r.index("products"));
    }

    /**
     * suggest completion 필드를 포함하여 단일 문서를 인덱싱한다.
     * ProductSearchSyncHandler.buildSuggestInput과 동일한 방식으로
     * 원본 이름 / 자모 분해 / 초성을 input으로 설정한다.
     */
    private void indexDocument(Long id, String name, String description,
                               Long price, String categoryName, Long categoryId) throws IOException {

        List<String> suggestInput = List.of(
                name,
                HangulUtils.decompose(name),
                HangulUtils.extractChosung(name)
        );

        Map<String, Object> doc = new HashMap<>();
        doc.put("id", id);
        doc.put("name", name);
        doc.put("description", description);
        doc.put("price", price);
        doc.put("stock", 100);
        doc.put("rating", 4.5);
        doc.put("reviewCount", 10);
        doc.put("imageUrl", "https://img.test/" + id);
        doc.put("categoryId", categoryId);
        doc.put("categoryName", categoryName);
        doc.put("foodName", name);
        doc.put("sellerId", 1L);
        doc.put("createdDate", LocalDateTime.of(2026, 3, 20, 10, 0).toString());
        doc.put("updatedDate", LocalDateTime.of(2026, 3, 20, 10, 0).toString());
        doc.put("suggest", Map.of("input", suggestInput));

        esClient.index(i -> i.index("products").id(String.valueOf(id)).document(doc));
    }
}
