package com.ssafy.fitmarket_be.product.integration;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.FieldValueFactorModifier;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ssafy.fitmarket_be.product.document.ProductDocument;
import com.ssafy.fitmarket_be.product.domain.ProductDocumentFixture;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Elasticsearch 통합 테스트")
class ElasticsearchIntegrationTest extends ElasticsearchTestConfig {

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
        // 인덱스 삭제 후 재생성 (테스트 격리)
        boolean exists = esClient.indices().exists(e -> e.index("products")).value();
        if (exists) {
            esClient.indices().delete(d -> d.index("products"));
        }

        // settings + mappings 적용
        try (InputStream settings = getClass().getResourceAsStream("/elasticsearch/product-settings.json");
             InputStream mappings = getClass().getResourceAsStream("/elasticsearch/product-mappings.json")) {

            esClient.indices().create(c -> c
                    .index("products")
                    .withJson(mergeSettingsAndMappings(settings, mappings))
            );
        }
    }

    private InputStream mergeSettingsAndMappings(InputStream settings, InputStream mappings) {
        // 간단한 방식: settings와 mappings를 합쳐서 create index body 생성
        try {
            ObjectMapper om = new ObjectMapper();
            Map<String, Object> s = om.readValue(settings, Map.class);
            Map<String, Object> m = om.readValue(mappings, Map.class);
            Map<String, Object> body = Map.of("settings", s, "mappings", m);
            byte[] bytes = om.writeValueAsBytes(body);
            return new java.io.ByteArrayInputStream(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void indexAndRefresh(ProductDocument... docs) throws IOException {
        for (ProductDocument doc : docs) {
            esClient.index(i -> i.index("products").id(String.valueOf(doc.getId())).document(doc));
        }
        esClient.indices().refresh(r -> r.index("products"));
    }

    @Test
    @DisplayName("nori 형태소 분석으로 부분 키워드 매칭이 동작한다")
    void nori_형태소분석_부분키워드매칭() throws IOException {
        // given
        indexAndRefresh(ProductDocumentFixture.create(1L, "닭가슴살 프로틴바", 4.5f));

        // when
        SearchResponse<ProductDocument> response = esClient.search(s -> s
                .index("products")
                .query(q -> q.multiMatch(mm -> mm
                        .query("닭가슴살")
                        .fields("name^3", "categoryName^2", "description", "foodName")
                        .type(TextQueryType.BestFields)
                        .analyzer("korean_analyzer")
                )),
                ProductDocument.class
        );

        // then
        assertThat(response.hits().hits()).isNotEmpty();
        assertThat(response.hits().hits().get(0).source().getName()).contains("닭가슴살");
    }

    @Test
    @DisplayName("multiMatch로 description 필드에서도 매칭된다")
    void multiMatch_복합필드검색_description매칭() throws IOException {
        // given
        indexAndRefresh(ProductDocumentFixture.withDescription(1L, "프로틴바", "고단백 닭가슴살 간식"));

        // when
        SearchResponse<ProductDocument> response = esClient.search(s -> s
                .index("products")
                .query(q -> q.multiMatch(mm -> mm
                        .query("닭가슴살")
                        .fields("name^3", "categoryName^2", "description", "foodName")
                        .type(TextQueryType.BestFields)
                        .analyzer("korean_analyzer")
                )),
                ProductDocument.class
        );

        // then
        assertThat(response.hits().hits()).hasSize(1);
    }

    @Test
    @DisplayName("function_score에서 reviewCount 가중치가 높은 상품이 상위에 노출된다")
    void functionScore_정렬검증_rating과reviewCount가중() throws IOException {
        // given
        // A: textScore * (log1p(4.5)*1.2 + log1p(100)*1.1) ≈ textScore * (2.11 + 5.07) = textScore * 7.18
        // B: textScore * (log1p(4.8)*1.2 + log1p(10)*1.1)  ≈ textScore * (2.17 + 2.64) = textScore * 4.81
        // → A가 상위
        indexAndRefresh(
                ProductDocumentFixture.withScore(1L, "프로틴바 A", 4.5f, 100),
                ProductDocumentFixture.withScore(2L, "프로틴바 B", 4.8f, 10)
        );

        // when
        SearchResponse<ProductDocument> response = esClient.search(s -> s
                .index("products")
                .query(q -> q.functionScore(fs -> fs
                        .query(innerQ -> innerQ.multiMatch(mm -> mm
                                .query("프로틴바")
                                .fields("name^3", "description")
                                .analyzer("korean_analyzer")
                        ))
                        .functions(fn -> fn.fieldValueFactor(fvf -> fvf
                                .field("rating").factor(1.2)
                                .modifier(FieldValueFactorModifier.Log1p).missing(3.0)
                        ))
                        .functions(fn -> fn.fieldValueFactor(fvf -> fvf
                                .field("reviewCount").factor(1.1)
                                .modifier(FieldValueFactorModifier.Log1p).missing(0.0)
                        ))
                        .scoreMode(FunctionScoreMode.Sum)
                        .boostMode(FunctionBoostMode.Multiply)
                )),
                ProductDocument.class
        );

        // then
        assertThat(response.hits().hits()).hasSize(2);
        assertThat(response.hits().hits().get(0).source().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("검색 결과에 em 태그 하이라이트가 포함된다")
    void highlight_검색결과_em태그포함() throws IOException {
        // given
        indexAndRefresh(ProductDocumentFixture.create(1L, "닭가슴살 샐러드", 4.5f));

        // when
        SearchResponse<ProductDocument> response = esClient.search(s -> s
                .index("products")
                .query(q -> q.multiMatch(mm -> mm
                        .query("닭가슴살")
                        .fields("name^3", "description")
                        .analyzer("korean_analyzer")
                ))
                .highlight(h -> h
                        .fields("name", f -> f.preTags("<em>").postTags("</em>"))
                ),
                ProductDocument.class
        );

        // then
        assertThat(response.hits().hits()).isNotEmpty();
        Map<String, List<String>> highlight = response.hits().hits().get(0).highlight();
        assertThat(highlight).containsKey("name");
        assertThat(highlight.get("name").get(0)).contains("<em>");
    }

    @Test
    @DisplayName("벌크 인덱싱 후 페이지네이션이 정확하게 동작한다")
    void bulkIndex_검색_페이지네이션정확() throws IOException {
        // given
        indexAndRefresh(
                ProductDocumentFixture.create(1L, "프로틴바 바닐라", 4.0f),
                ProductDocumentFixture.create(2L, "프로틴바 초콜릿", 4.2f),
                ProductDocumentFixture.create(3L, "프로틴바 딸기", 3.8f),
                ProductDocumentFixture.create(4L, "프로틴바 쉐이크", 4.5f),
                ProductDocumentFixture.create(5L, "프로틴바 파우더", 4.1f)
        );

        // when
        SearchResponse<ProductDocument> response = esClient.search(s -> s
                .index("products")
                .query(q -> q.multiMatch(mm -> mm
                        .query("프로틴바")
                        .fields("name")
                        .analyzer("korean_analyzer")
                ))
                .from(0).size(3),
                ProductDocument.class
        );

        // then
        assertThat(response.hits().hits()).hasSize(3);
        TotalHits totalHits = response.hits().total();
        assertThat(totalHits).isNotNull();
        assertThat(totalHits.value()).isEqualTo(5L);
    }

    @Test
    @DisplayName("categoryId 필터와 키워드 조합으로 해당 카테고리만 반환한다")
    void categoryId필터_키워드조합_해당카테고리만반환() throws IOException {
        // given
        indexAndRefresh(
                ProductDocumentFixture.withCategory(1L, "프로틴바 A", 1L, "단백질"),
                ProductDocumentFixture.withCategory(2L, "프로틴바 B", 1L, "단백질"),
                ProductDocumentFixture.withCategory(3L, "프로틴바 C", 2L, "간식")
        );

        // when
        SearchResponse<ProductDocument> response = esClient.search(s -> s
                .index("products")
                .query(q -> q.bool(b -> b
                        .must(m -> m.multiMatch(mm -> mm
                                .query("프로틴바")
                                .fields("name")
                                .analyzer("korean_analyzer")
                        ))
                        .filter(f -> f.term(t -> t.field("categoryId").value(1L)))
                )),
                ProductDocument.class
        );

        // then
        assertThat(response.hits().hits()).hasSize(2);
        response.hits().hits().forEach(hit ->
                assertThat(hit.source().getCategoryId()).isEqualTo(1L)
        );
    }
}
