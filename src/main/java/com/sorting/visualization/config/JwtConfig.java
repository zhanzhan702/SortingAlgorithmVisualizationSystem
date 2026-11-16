package com.sorting.visualization.config;

import com.sorting.visualization.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 把配置项注入 {@link JwtUtil}。
 * 单独放在配置类里，是为了让 JwtUtil 保持纯 POJO，单元测试不依赖 Spring 容器。
 */
@Configuration
public class JwtConfig {

    @Bean
    public JwtUtil jwtUtil(@Value("${jwt.secret}") String secret,
                           @Value("${jwt.expiration:86400000}") long expirationMillis) {
        return new JwtUtil(secret, expirationMillis);
    }
}
