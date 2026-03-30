package com.ssafy.fitmarket_be.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * MyBatis Mapper들을 Scan하기 위한 Config Class
 */
@Configuration
@MapperScan(basePackages = {
    "com.ssafy.fitmarket_be.**.repository",
    "com.ssafy.fitmarket_be.**.infrastructure.mybatis"
})
public class ApplicationConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
