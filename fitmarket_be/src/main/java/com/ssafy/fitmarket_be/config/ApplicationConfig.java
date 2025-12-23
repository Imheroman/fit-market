package com.ssafy.fitmarket_be.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Mapper들을 Scan하기 위한 Config Class
 */
@Configuration
@MapperScan(basePackages = {
    "com.ssafy.fitmarket_be.**.repository",
    "com.ssafy.fitmarket_be.**.infrastructure.mybatis"
})
public class ApplicationConfig {}
