package com.ssafy.fitmarket_be.product.integration;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

@Testcontainers
public abstract class ElasticsearchTestConfig {

    static final String ES_DOCKERFILE =
            "FROM docker.elastic.co/elasticsearch/elasticsearch:8.15.0\n" +
            "RUN bin/elasticsearch-plugin install --batch analysis-nori\n";

    @Container
    static final GenericContainer<?> ES_CONTAINER = new GenericContainer<>(
            new ImageFromDockerfile("fitmarket-es-test")
                    .withFileFromString("Dockerfile", ES_DOCKERFILE)
    )
    .withExposedPorts(9200)
    .withEnv("discovery.type", "single-node")
    .withEnv("xpack.security.enabled", "false")
    .withEnv("ES_JAVA_OPTS", "-Xms256m -Xmx256m")
    .withStartupTimeout(Duration.ofMinutes(5));

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.uris",
                () -> "http://" + ES_CONTAINER.getHost() + ":" + ES_CONTAINER.getMappedPort(9200));
    }
}
