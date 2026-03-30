package com.ssafy.fitmarket_be.product.sync;

import com.ssafy.fitmarket_be.product.document.ProductDocument;
import com.ssafy.fitmarket_be.product.domain.ProductSyncDataFixture;
import com.ssafy.fitmarket_be.product.event.ProductEvent;
import com.ssafy.fitmarket_be.product.repository.ProductMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductSearchSyncHandlerTest {

    @Mock
    ElasticsearchTemplate elasticsearchTemplate;

    @Mock
    ProductMapper productMapper;

    @Mock
    StringRedisTemplate redisTemplate;

    @InjectMocks
    ProductSearchSyncHandler syncHandler;

    @Test
    @DisplayName("onProductChanged - Created 이벤트 시 indexProduct 호출")
    void onProductChanged_Created이벤트_indexProduct호출() {
        // given
        ProductEvent event = new ProductEvent.Created(1L);
        when(productMapper.selectProductForSync(1L))
                .thenReturn(ProductSyncDataFixture.active(1L));
        when(elasticsearchTemplate.save(any(ProductDocument.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        syncHandler.onProductChanged(event);

        // then
        verify(elasticsearchTemplate).save(any(ProductDocument.class));
        verify(elasticsearchTemplate, never()).delete(anyString(), eq(ProductDocument.class));
    }

    @Test
    @DisplayName("onProductChanged - Updated 이벤트 시 indexProduct 호출")
    void onProductChanged_Updated이벤트_indexProduct호출() {
        // given
        ProductEvent event = new ProductEvent.Updated(1L);
        when(productMapper.selectProductForSync(1L))
                .thenReturn(ProductSyncDataFixture.active(1L));
        when(elasticsearchTemplate.save(any(ProductDocument.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        syncHandler.onProductChanged(event);

        // then
        verify(elasticsearchTemplate).save(any(ProductDocument.class));
        verify(elasticsearchTemplate, never()).delete(anyString(), eq(ProductDocument.class));
    }

    @Test
    @DisplayName("onProductChanged - Deleted 이벤트 시 deleteFromIndex 호출")
    void onProductChanged_Deleted이벤트_deleteFromIndex호출() {
        // given
        ProductEvent event = new ProductEvent.Deleted(1L);

        // when
        syncHandler.onProductChanged(event);

        // then
        verify(elasticsearchTemplate).delete("1", ProductDocument.class);
        verify(elasticsearchTemplate, never()).save(any(ProductDocument.class));
        verify(productMapper, never()).selectProductForSync(anyLong());
    }

    @Test
    @DisplayName("indexProduct - soft-deleted 상품이면 deleteFromIndex 호출")
    void indexProduct_softDeleted상품_deleteFromIndex호출() {
        // given
        when(productMapper.selectProductForSync(2L))
                .thenReturn(ProductSyncDataFixture.deleted(2L));

        // when
        syncHandler.indexProduct(2L);

        // then
        verify(elasticsearchTemplate).delete("2", ProductDocument.class);
        verify(elasticsearchTemplate, never()).save(any(ProductDocument.class));
    }

    @Test
    @DisplayName("toDocument - 영양소 null이면 기본값 0f 매핑")
    void toDocument_영양소null_기본값0f매핑() {
        // given
        ProductSyncData data = ProductSyncDataFixture.withNullNutrition(3L);

        // when
        ProductDocument doc = syncHandler.toDocument(data);

        // then
        assertThat(doc.getNutrition().getCalories()).isEqualTo(0f);
        assertThat(doc.getNutrition().getProtein()).isEqualTo(0f);
        assertThat(doc.getNutrition().getCarbs()).isEqualTo(0f);
        assertThat(doc.getNutrition().getFat()).isEqualTo(0f);
    }

    @Test
    @DisplayName("toDocument - 정상 영양소 정확한 Float 변환")
    void toDocument_정상영양소_정확한Float변환() {
        // given
        ProductSyncData data = ProductSyncDataFixture.active(1L);

        // when
        ProductDocument doc = syncHandler.toDocument(data);

        // then
        assertThat(doc.getNutrition().getCalories()).isEqualTo(250f);
        assertThat(doc.getNutrition().getProtein()).isEqualTo(30f);
        assertThat(doc.getNutrition().getCarbs()).isEqualTo(10f);
        assertThat(doc.getNutrition().getFat()).isEqualTo(5f);
        assertThat(doc.getName()).isEqualTo("테스트 상품 1");
        assertThat(doc.getCategoryName()).isEqualTo("단백질");
        assertThat(doc.getSellerId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("productId에 해당하는 상품이 없으면 ES에서 삭제한다")
    void indexProduct_데이터없음_deleteFromIndex호출() {
        // given
        given(productMapper.selectProductForSync(999L)).willReturn(null);

        // when
        syncHandler.indexProduct(999L);

        // then
        verify(elasticsearchTemplate).delete("999", ProductDocument.class);
        verify(elasticsearchTemplate, never()).save(any(ProductDocument.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("3회 재시도 모두 실패 시 Redis에 실패 ID를 저장한다")
    void onProductChangedRecover_최종실패_Redis에ID저장() {
        // given
        ProductEvent event = new ProductEvent.Created(1L);
        Exception ex = new RuntimeException("ES 장애");
        SetOperations<String, String> setOps = mock(SetOperations.class);
        given(redisTemplate.opsForSet()).willReturn(setOps);

        // when
        syncHandler.onProductChangedRecover(ex, event);

        // then
        verify(setOps).add("es:sync:failed-ids", "1");
    }
}
