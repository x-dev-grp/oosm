package com.xdev.ooms.security.securityConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(DynamicCorsConfigurationSource dynamicCorsConfigurationSource) {
        return dynamicCorsConfigurationSource;
    }
}
