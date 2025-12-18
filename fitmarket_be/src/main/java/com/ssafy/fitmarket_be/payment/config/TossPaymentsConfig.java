package com.ssafy.fitmarket_be.payment.config;

import io.netty.resolver.DefaultAddressResolverGroup;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@EnableConfigurationProperties(TossPaymentsProperties.class)
@Configuration
@Slf4j
public class TossPaymentsConfig {

  @Bean
  public WebClient tossWebClient(TossPaymentsProperties properties) {
    String basicAuth = Base64.getEncoder()
        .encodeToString((properties.secretKey() + ":").getBytes(StandardCharsets.UTF_8));

    return WebClient.builder()
        .baseUrl(properties.baseUrl())
        .clientConnector(new ReactorClientHttpConnector(
            // netty가 아닌 기존 Java의 요청을 그대로 따름 ?
            HttpClient.create().resolver(DefaultAddressResolverGroup.INSTANCE)))
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basicAuth)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }
}