package com.ssafy.fitmarket_be.product.sync;

import com.ssafy.fitmarket_be.product.document.ProductDocument;
import com.ssafy.fitmarket_be.product.repository.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductSearchBatchSync {

    private final ProductSearchSyncHandler syncHandler;
    private final ProductMapper productMapper;
    private final ElasticsearchTemplate elasticsearchTemplate;
    private final StringRedisTemplate redisTemplate;

    private static final String LAST_SYNC_KEY = "es:sync:last-sync-time";
    private static final String SYNC_FAIL_KEY = ProductSearchSyncHandler.SYNC_FAIL_KEY;
    private static final int FULL_REINDEX_BATCH_SIZE = 500;

    /**
     * 5분 주기 배치 보정.
     * Step 1: Redis에 기록된 실패 ID 우선 재인덱싱.
     * Step 2: lastSyncTime 이후 변경된 상품 벌크 인덱싱.
     */
    @Scheduled(fixedRate = 300_000)
    public void syncModifiedProducts() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastSyncTime = getLastSyncTime();

        // Step 1: 실패 ID 우선 보정
        retryFailedIds();

        // Step 2: 변경분 보정
        List<ProductSyncData> modified = productMapper.selectModifiedAfter(lastSyncTime);
        if (!modified.isEmpty()) {
            int successCount = 0;
            for (ProductSyncData data : modified) {
                try {
                    if (data.deletedDate() != null) {
                        syncHandler.deleteFromIndex(data.id());
                    } else {
                        syncHandler.indexProduct(data.id());
                    }
                    successCount++;
                } catch (Exception e) {
                    log.error("배치 보정 실패 (productId={}): {}", data.id(), e.getMessage());
                }
            }
            log.info("배치 보정 완료: {}/{}건 동기화", successCount, modified.size());
        }

        redisTemplate.opsForValue().set(LAST_SYNC_KEY, now.toString());
    }

    /**
     * 실패 ID 재시도.
     */
    private void retryFailedIds() {
        Set<String> failedIds = redisTemplate.opsForSet().members(SYNC_FAIL_KEY);
        if (failedIds == null || failedIds.isEmpty()) {
            return;
        }

        int successCount = 0;
        List<String> stillFailed = new ArrayList<>();

        for (String id : failedIds) {
            try {
                syncHandler.indexProduct(Long.parseLong(id));
                successCount++;
            } catch (Exception e) {
                log.error("실패 보정 재실패 (productId={}): {}", id, e.getMessage());
                stillFailed.add(id);
            }
        }

        // 성공한 것은 제거, 여전히 실패한 것만 유지
        redisTemplate.delete(SYNC_FAIL_KEY);
        if (!stillFailed.isEmpty()) {
            redisTemplate.opsForSet().add(SYNC_FAIL_KEY, stillFailed.toArray(new String[0]));
        }

        log.info("실패 보정: {}/{}건 재동기화 성공", successCount, failedIds.size());
    }

    /**
     * 전체 재인덱싱 -- 수동 트리거용.
     * 전체 상품을 페이지 단위(500건)로 조회하여 벌크 인덱싱한다.
     */
    public void fullReindex() {
        log.info("전체 재인덱싱 시작");
        int offset = 0;
        int totalIndexed = 0;

        List<ProductSyncData> batch;
        do {
            batch = productMapper.selectAllActiveForSync(FULL_REINDEX_BATCH_SIZE, offset);
            if (!batch.isEmpty()) {
                List<ProductDocument> documents = batch.stream()
                        .map(syncHandler::toDocument)
                        .toList();
                elasticsearchTemplate.save(documents);
                totalIndexed += documents.size();
            }
            offset += FULL_REINDEX_BATCH_SIZE;
        } while (batch.size() == FULL_REINDEX_BATCH_SIZE);

        // 동기화 시점 갱신
        redisTemplate.opsForValue().set(LAST_SYNC_KEY, LocalDateTime.now().toString());
        log.info("전체 재인덱싱 완료: {}건 인덱싱", totalIndexed);
    }

    private LocalDateTime getLastSyncTime() {
        String saved = redisTemplate.opsForValue().get(LAST_SYNC_KEY);
        return saved != null ? LocalDateTime.parse(saved) : LocalDateTime.now().minusMinutes(10);
    }
}
