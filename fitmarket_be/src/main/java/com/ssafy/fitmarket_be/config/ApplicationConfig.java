package com.ssafy.fitmarket_be.config;

import com.ssafy.fitmarket_be.user.repository.UserRepository;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Mapper들을 Scan하기 위한 Config Class
 */
@Configuration
@MapperScan(basePackageClasses = {UserRepository.class}) // 단일 클래스가 아니라 그 클래스가 소속한 하위 패키지 스캔 !! (따라서, 다른 Dao들까지도 Scan 가능 !!)
 public class ApplicationConfig {}
