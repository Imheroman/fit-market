package com.ssafy.fitmarket_be.product.sync;

import com.ssafy.fitmarket_be.product.document.ProductDocument;
import com.ssafy.fitmarket_be.product.domain.ProductSyncDataFixture;
import com.ssafy.fitmarket_be.product.repository.ProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductSearchBatchSync")
class ProductSearchBatchSyncTest {

    @Mock ProductSearchSyncHandler syncHandler;
    @Mock ProductMapper productMapper;
    @Mock ElasticsearchTemplate elasticsearchTemplate;
    @Mock StringRedisTemplate redisTemplate;
    @Mock ValueOperations<String, String> valueOps;
    @Mock SetOperations<String, String> setOps;
    @InjectMocks ProductSearchBatchSync batchSync;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOps);
    }

    @Test
    @DisplayName("syncModifiedProducts - 실패 ID 재시도 성공 후 Redis에서 제거")
    void syncModifiedProducts_실패ID재시도_성공후Redis제거() {
        // given
        given(setOps.members("es:sync:failed-ids")).willReturn(Set.of("1", "2"));
        // indexProduct 성공 (예외 안 던짐)
        given(valueOps.get("es:sync:last-sync-time")).willReturn(null);
        given(productMapper.selectModifiedAfter(any())).willReturn(Collections.emptyList());

        // when
        batchSync.syncModifiedProducts();

        // then
        verify(syncHandler).indexProduct(1L);
        verify(syncHandler).indexProduct(2L);
        verify(redisTemplate).delete("es:sync:failed-ids");
    }

    @Test
    @DisplayName("syncModifiedProducts - 변경분 동기화 시 deleted 구분 처리")
    void syncModifiedProducts_변경분동기화_deleted구분처리() {
        // given
        given(setOps.members("es:sync:failed-ids")).willReturn(Collections.emptySet());
        given(valueOps.get("es:sync:last-sync-time")).willReturn(
                java.time.LocalDateTime.now().minusMinutes(10).toString()
        );

        List<ProductSyncData> modified = List.of(
                ProductSyncDataFixture.active(10L),
                ProductSyncDataFixture.active(11L),
                ProductSyncDataFixture.deleted(12L)
        );
        given(productMapper.selectModifiedAfter(any())).willReturn(modified);

        // when
        batchSync.syncModifiedProducts();

        // then
        verify(syncHandler).indexProduct(10L);
        verify(syncHandler).indexProduct(11L);
        verify(syncHandler).deleteFromIndex(12L);
        verify(valueOps).set(eq("es:sync:last-sync-time"), anyString());
    }

    @Test
    @DisplayName("syncModifiedProducts - 변경 없으면 동기화 미실행")
    void syncModifiedProducts_변경없음_동기화미실행() {
        // given
        given(setOps.members("es:sync:failed-ids")).willReturn(Collections.emptySet());
        given(valueOps.get("es:sync:last-sync-time")).willReturn(
                java.time.LocalDateTime.now().minusMinutes(5).toString()
        );
        given(productMapper.selectModifiedAfter(any())).willReturn(Collections.emptyList());

        // when
        batchSync.syncModifiedProducts();

        // then
        verify(syncHandler, never()).indexProduct(anyLong());
        verify(syncHandler, never()).deleteFromIndex(anyLong());
    }

    @Test
    @DisplayName("fullReindex - 페이지 단위 벌크 인덱싱 전체 완료")
    void fullReindex_페이지단위벌크인덱싱_전체완료() {
        // given
        List<ProductSyncData> firstBatch = IntStream.rangeClosed(1, 500)
                .mapToObj(i -> ProductSyncDataFixture.active((long) i))
                .toList();
        List<ProductSyncData> secondBatch = IntStream.rangeClosed(501, 700)
                .mapToObj(i -> ProductSyncDataFixture.active((long) i))
                .toList();

        given(productMapper.selectAllActiveForSync(500, 0)).willReturn(firstBatch);
        given(productMapper.selectAllActiveForSync(500, 500)).willReturn(secondBatch);

        given(syncHandler.toDocument(any(ProductSyncData.class)))
                .willAnswer(inv -> ProductDocument.builder()
                        .id(inv.getArgument(0, ProductSyncData.class).id())
                        .build());

        given(elasticsearchTemplate.save(anyIterable()))
                .willAnswer(inv -> inv.getArgument(0));

        // when
        batchSync.fullReindex();

        // then
        verify(elasticsearchTemplate, times(2)).save(anyIterable());
        verify(valueOps).set(eq("es:sync:last-sync-time"), anyString());

        // 루프 종료 검증: selectAllActiveForSync가 정확히 2번만 호출됨
        verify(productMapper, times(2)).selectAllActiveForSync(eq(500), anyInt());
        verifyNoMoreInteractions(productMapper);  // 세 번째 호출 없음
    }

    @Test
    @DisplayName("실패 ID 재시도 시 일부만 성공하면 실패 ID만 Redis에 다시 저장한다")
    void syncModifiedProducts_실패ID재시도_일부실패_Redis재저장() {
        // given: 실패 ID [1, 2], id=1 성공, id=2 예외
        given(setOps.members("es:sync:failed-ids")).willReturn(Set.of("1", "2"));
        doNothing().when(syncHandler).indexProduct(1L);
        doThrow(new RuntimeException("ES 장애")).when(syncHandler).indexProduct(2L);

        // 변경분 없음 설정
        given(valueOps.get("es:sync:last-sync-time")).willReturn(null);
        given(productMapper.selectModifiedAfter(any())).willReturn(Collections.emptyList());

        // when
        batchSync.syncModifiedProducts();

        // then: [2]만 다시 저장
        verify(redisTemplate).delete("es:sync:failed-ids");
        verify(setOps).add("es:sync:failed-ids", "2");
    }

    @Test
    @DisplayName("변경분 동기화 중 특정 상품이 실패해도 나머지는 계속 처리된다")
    void syncModifiedProducts_변경분동기화_개별실패_나머지계속처리() {
        // given: 실패 ID 없음
        given(setOps.members("es:sync:failed-ids")).willReturn(Collections.emptySet());
        given(valueOps.get("es:sync:last-sync-time")).willReturn(null);

        // active 2건 중 id=20 성공, id=21 예외
        List<ProductSyncData> modified = List.of(
                ProductSyncDataFixture.active(20L),
                ProductSyncDataFixture.active(21L)
        );
        given(productMapper.selectModifiedAfter(any())).willReturn(modified);
        doNothing().when(syncHandler).indexProduct(20L);
        doThrow(new RuntimeException("ES 장애")).when(syncHandler).indexProduct(21L);

        // when
        batchSync.syncModifiedProducts();

        // then: 두 건 모두 indexProduct 호출 시도됨 (개별 실패가 루프를 중단시키지 않음)
        verify(syncHandler).indexProduct(20L);
        verify(syncHandler).indexProduct(21L);
        // lastSyncTime 갱신 확인 (실패 여부와 무관하게 갱신됨)
        verify(valueOps).set(eq("es:sync:last-sync-time"), anyString());
    }
}
