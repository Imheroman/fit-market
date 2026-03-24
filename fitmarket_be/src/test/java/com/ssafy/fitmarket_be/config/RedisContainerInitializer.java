package com.ssafy.fitmarket_be.config;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * 통합 테스트용 Redis Testcontainer 초기화.
 * 컨테이너는 JVM당 1개만 생성되어 모든 테스트에서 재사용된다.
 */
public class RedisContainerInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final GenericContainer<?> REDIS;

    static {
        REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                .withExposedPorts(6379);
        REDIS.start();
        Runtime.getRuntime().addShutdownHook(new Thread(REDIS::stop));
    }

    @Override
    public void initialize(ConfigurableApplicationContext ctx) {
        TestPropertyValues.of(
                "spring.data.redis.host=" + REDIS.getHost(),
                "spring.data.redis.port=" + REDIS.getMappedPort(6379)
        ).applyTo(ctx.getEnvironment());
    }
}
