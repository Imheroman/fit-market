package com.ssafy.fitmarket_be.product.sync;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.ssafy.fitmarket_be.product.document.ProductDocument;
import com.ssafy.fitmarket_be.product.repository.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;

/**
 * 앱 시작 시 MySQL의 기존 상품 데이터를 ES에 벌크 인덱싱하는 초기화 컴포넌트.
 *
 * <ul>
 *   <li>{@code search.elasticsearch.init-index=false}로 비활성화 가능</li>
 *   <li>{@code @Profile("!test")}로 테스트 환경에서는 자동 비활성화</li>
 *   <li>ES 장애 시에도 앱 기동은 정상 진행 (MySQL Fallback 보장)</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class ProductIndexInitializer implements CommandLineRunner {

    private final ProductSearchBatchSync batchSync;
    private final ElasticsearchClient esClient;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ProductMapper productMapper;

    @Value("${search.elasticsearch.init-index:true}")
    private boolean initIndex;

    @Override
    public void run(String... args) throws Exception {
        if (!initIndex) {
            log.info("ES 초기 인덱싱 비활성화 (search.elasticsearch.init-index=false)");
            return;
        }

        try {
            // ES 연결 확인
            esClient.info();
            log.info("ES 연결 확인 완료, 초기 인덱싱 시작...");

            // 인덱스 삭제 후 재생성 — @Setting + @Mapping JSON으로 정확한 매핑 보장
            recreateIndex();

            // MySQL 활성 상품 수 조회
            long mysqlCount = productMapper.countProducts();
            log.info("MySQL 활성 상품 수: {}건", mysqlCount);

            // 전체 재인덱싱 실행
            batchSync.fullReindex();

            // ES 인덱싱 결과 확인
            long esCount = esClient.count(c -> c.index("products")).count();
            log.info("ES 인덱싱 문서 수: {}건", esCount);

            // MySQL vs ES count 비교
            if (mysqlCount == esCount) {
                log.info("ES 초기 인덱싱 완료 — MySQL({})과 ES({}) 문서 수 일치",
                        mysqlCount, esCount);
            } else {
                log.warn("ES 초기 인덱싱 완료 — 문서 수 불일치! MySQL={}, ES={}",
                        mysqlCount, esCount);
            }
        } catch (Exception e) {
            log.warn("ES 초기 인덱싱 실패 (앱 기동에는 영향 없음): {}", e.getMessage());
            // ES 장애 시에도 앱 기동은 정상 진행 — Fallback으로 MySQL 검색 가능
        }
    }

    private void recreateIndex() {
        IndexOperations indexOps = elasticsearchOperations.indexOps(ProductDocument.class);
        if (indexOps.exists()) {
            indexOps.delete();
            log.info("기존 products 인덱스 삭제");
        }
        indexOps.createWithMapping();
        log.info("products 인덱스 생성 완료 (settings + mappings 적용)");
    }
}
