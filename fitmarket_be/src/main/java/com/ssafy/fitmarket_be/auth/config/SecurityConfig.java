package com.ssafy.fitmarket_be.auth.config;

import com.ssafy.fitmarket_be.auth.filter.CustomAuthenticationFilter;
import com.ssafy.fitmarket_be.auth.filter.CustomLogoutFilter;
import com.ssafy.fitmarket_be.auth.jwt.JwtUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private final JwtUtil jwtUtil;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
    return configuration.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)  // csrf disable (session 안 쓰므로 불필요)
        // ✅ CORS 활성화
         .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .formLogin(AbstractHttpConfigurer::disable)  // form login disable ( 커스텀 필터 쓰므로 불필요)
        .httpBasic(AbstractHttpConfigurer::disable)  // http basic authentication disable
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(new CustomAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(new CustomLogoutFilter(), LogoutFilter.class);

    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/login", "/logout", "/public/**", "/users/signup")
            .permitAll()
            .anyRequest().authenticated()
        );

    return http.build();
  }

  // ✅ Security에서 사용할 CORS 설정 제공
  @Bean
  public CorsConfigurationSource corsConfigurationSource() { // Changed
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:5173")); // 프론트 주소
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "OPTIONS")); // OPTIONS 포함 // Changed
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
