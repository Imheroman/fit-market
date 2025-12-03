package com.ssafy.fitmarket_be.config;

import com.ssafy.fitmarket_be.user.repository.UserRepository;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Mapper들을 Scan하기 위한 Config Class
 */
@Configuration
@MapperScan(basePackages = "com.ssafy.fitmarket_be.**.repository")
 public class ApplicationConfig {}
