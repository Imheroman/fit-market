package com.ssafy.fitmarket_be.global.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ConditionalOnProperty(name = "search.elasticsearch.enabled", havingValue = "true", matchIfMissing = true)
@EnableElasticsearchRepositories(basePackages = "com.ssafy.fitmarket_be")
@EnableRetry
@EnableScheduling
public class ElasticsearchConfig {
    // Spring Boot auto-configuration이 spring.elasticsearch.* 프로퍼티를 기반으로
    // RestClient → ElasticsearchClient → ElasticsearchOperations 빈을 자동 생성
}
