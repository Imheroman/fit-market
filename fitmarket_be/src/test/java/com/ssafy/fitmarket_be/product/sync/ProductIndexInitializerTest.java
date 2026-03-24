package com.ssafy.fitmarket_be.product.sync;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.CountResponse;
import co.elastic.clients.elasticsearch.core.InfoResponse;
import com.ssafy.fitmarket_be.product.repository.ProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductIndexInitializer")
class ProductIndexInitializerTest {

    @Mock
    private ProductSearchBatchSync batchSync;

    @Mock
    private ElasticsearchClient esClient;

    @Mock
    private ProductMapper productMapper;

    private ProductIndexInitializer initializer;

    @BeforeEach
    void setUp() {
        initializer = new ProductIndexInitializer(batchSync, esClient, productMapper);
    }

    @Test
    @DisplayName("initIndex=true이고 ES 연결 정상이면 fullReindex를 실행한다")
    @SuppressWarnings("unchecked")
    void run_정상초기화_fullReindex호출() throws Exception {
        // given
        ReflectionTestUtils.setField(initializer, "initIndex", true);
        given(esClient.info()).willReturn(mock(InfoResponse.class));
        given(productMapper.countProducts()).willReturn(100L);

        CountResponse countResponse = mock(CountResponse.class);
        given(countResponse.count()).willReturn(100L);
        given(esClient.count(any(Function.class))).willReturn(countResponse);

        // when
        initializer.run();

        // then
        verify(batchSync).fullReindex();
        verify(productMapper).countProducts();
    }

    @Test
    @DisplayName("initIndex=false이면 초기 인덱싱을 건너뛴다")
    void run_initIndexFalse_스킵() throws Exception {
        // given
        ReflectionTestUtils.setField(initializer, "initIndex", false);

        // when
        initializer.run();

        // then
        verify(batchSync, never()).fullReindex();
        verify(esClient, never()).info();
        verify(productMapper, never()).countProducts();
    }

    @Test
    @DisplayName("ES 연결 실패 시 예외를 전파하지 않고 앱이 정상 기동한다")
    void run_ES연결실패_예외전파없이앱기동() throws Exception {
        // given
        ReflectionTestUtils.setField(initializer, "initIndex", true);
        given(esClient.info()).willThrow(new IOException("Connection refused"));

        // when & then
        assertDoesNotThrow(() -> initializer.run());
        verify(batchSync, never()).fullReindex();
    }

    @Test
    @DisplayName("MySQL과 ES 문서 수가 불일치하면 경고 로그를 남기고 정상 완료한다")
    @SuppressWarnings("unchecked")
    void run_countMismatch_경고후정상완료() throws Exception {
        // given
        ReflectionTestUtils.setField(initializer, "initIndex", true);
        given(esClient.info()).willReturn(mock(InfoResponse.class));
        given(productMapper.countProducts()).willReturn(100L);

        CountResponse countResponse = mock(CountResponse.class);
        given(countResponse.count()).willReturn(95L);  // 불일치
        given(esClient.count(any(Function.class))).willReturn(countResponse);

        // when
        initializer.run();

        // then
        verify(batchSync).fullReindex();
        // 경고 로그 발생하나 예외 없이 정상 완료
    }
}
