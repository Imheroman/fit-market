package com.ssafy.fitmarket_be.ranking.service;

import com.ssafy.fitmarket_be.product.domain.Product;
import com.ssafy.fitmarket_be.product.repository.ProductMapper;
import com.ssafy.fitmarket_be.ranking.dto.RankingResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductRankingService {
    private final StringRedisTemplate redisTemplate;
    private final ProductMapper productMapper;

    private static final String DAILY_PREFIX = "ranking:product:daily:";
    private static final String WEEKLY_PREFIX = "ranking:product:weekly:";
    private static final String ALL_KEY = "ranking:product:all";

    public void incrementScore(Long productId, double score) {
        String member = String.valueOf(productId);
        String today = LocalDate.now().toString();

        try {
            redisTemplate.opsForZSet().incrementScore(DAILY_PREFIX + today, member, score);
            redisTemplate.opsForZSet().incrementScore(ALL_KEY, member, score);
            redisTemplate.expire(DAILY_PREFIX + today, Duration.ofDays(2));
        } catch (RedisConnectionFailureException e) {
            log.warn("Ranking update failed for product {}", productId, e);
        }
    }

    public List<RankingResponse> getTopProducts(String period, int limit) {
        String key = resolveKey(period);

        try {
            Set<ZSetOperations.TypedTuple<String>> tuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, limit - 1);

            if (tuples == null || tuples.isEmpty()) return List.of();

            AtomicInteger rank = new AtomicInteger(1);
            return tuples.stream()
                .map(t -> {
                    Long productId = Long.parseLong(t.getValue());
                    Product product = productMapper.selectProductById(productId);
                    if (product == null) return null;
                    return RankingResponse.of(rank.getAndIncrement(), product, t.getScore());
                })
                .filter(r -> r != null)
                .toList();
        } catch (RedisConnectionFailureException e) {
            log.warn("Ranking query failed for period={}", period, e);
            return List.of();
        }
    }

    private String resolveKey(String period) {
        return switch (period) {
            case "daily" -> DAILY_PREFIX + LocalDate.now();
            case "weekly" -> {
                LocalDate now = LocalDate.now();
                int year = now.get(WeekFields.ISO.weekBasedYear());
                int week = now.get(WeekFields.ISO.weekOfWeekBasedYear());
                yield WEEKLY_PREFIX + year + "-W" + String.format("%02d", week);
            }
            default -> ALL_KEY;
        };
    }
}
