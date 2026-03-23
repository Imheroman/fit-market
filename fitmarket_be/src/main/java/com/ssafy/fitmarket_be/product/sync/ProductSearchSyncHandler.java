package com.ssafy.fitmarket_be.product.sync;

import com.ssafy.fitmarket_be.product.document.NutritionInfo;
import com.ssafy.fitmarket_be.product.document.ProductDocument;
import com.ssafy.fitmarket_be.product.event.ProductEvent;
import com.ssafy.fitmarket_be.product.repository.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductSearchSyncHandler {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final ProductMapper productMapper;
    private final StringRedisTemplate redisTemplate;

    static final String SYNC_FAIL_KEY = "es:sync:failed-ids";

    /**
     * 증분 동기화 -- 트랜잭션 커밋 후 ES 반영.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("esAsyncExecutor")
    @Retryable(
        retryFor = Exception.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void onProductChanged(ProductEvent event) {
        switch (event) {
            case ProductEvent.Created e -> indexProduct(e.productId());
            case ProductEvent.Updated e -> indexProduct(e.productId());
            case ProductEvent.Deleted e -> deleteFromIndex(e.productId());
        }
        log.info("ES 동기화 완료: {} (productId={})", event.getClass().getSimpleName(), event.productId());
    }

    /**
     * 3회 재시도 모두 실패한 경우의 최종 Fallback.
     */
    @Recover
    public void onProductChangedRecover(Exception e, ProductEvent event) {
        log.error("ES 동기화 최종 실패 (3회 재시도 후): productId={}", event.productId(), e);
        redisTemplate.opsForSet().add(SYNC_FAIL_KEY, String.valueOf(event.productId()));
    }

    /**
     * 재사용 가능한 인덱싱 메서드 -- 배치 보정, 초기 인덱싱에서도 호출.
     */
    public void indexProduct(Long productId) {
        ProductSyncData data = productMapper.selectProductForSync(productId);
        if (data == null || data.deletedDate() != null) {
            deleteFromIndex(productId);
            return;
        }

        ProductDocument doc = toDocument(data);
        elasticsearchTemplate.save(doc);
    }

    /**
     * ES에서 상품 문서 삭제.
     */
    public void deleteFromIndex(Long productId) {
        elasticsearchTemplate.delete(String.valueOf(productId), ProductDocument.class);
    }

    /**
     * ProductSyncData를 ProductDocument로 변환.
     */
    ProductDocument toDocument(ProductSyncData data) {
        return ProductDocument.builder()
                .id(data.id())
                .name(data.name())
                .description(data.description())
                .price(data.price())
                .stock(data.stock())
                .rating(data.rating() != null ? data.rating().floatValue() : 0f)
                .reviewCount(data.reviewCount())
                .imageUrl(data.imageUrl())
                .categoryId(data.categoryId())
                .categoryName(data.categoryName())
                .foodName(data.foodName())
                .nutrition(NutritionInfo.builder()
                        .calories(data.calories() != null ? data.calories().floatValue() : 0f)
                        .protein(data.protein() != null ? data.protein().floatValue() : 0f)
                        .carbs(data.carbs() != null ? data.carbs().floatValue() : 0f)
                        .fat(data.fat() != null ? data.fat().floatValue() : 0f)
                        .build())
                .sellerId(data.sellerId())
                .createdDate(data.createdDate())
                .updatedDate(data.modifiedDate())
                .build();
    }
}
