package com.ssafy.fitmarket_be.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.ssafy.fitmarket_be")
@EnableRetry
@EnableScheduling
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo("localhost:9200")
                .withConnectTimeout(5_000)
                .withSocketTimeout(30_000)
                .build();
    }
}
