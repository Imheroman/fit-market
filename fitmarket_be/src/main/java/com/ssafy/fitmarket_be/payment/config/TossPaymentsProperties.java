package com.ssafy.fitmarket_be.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "toss.payments")
public record TossPaymentsProperties(
    String secretKey,
    String baseUrl
) {}