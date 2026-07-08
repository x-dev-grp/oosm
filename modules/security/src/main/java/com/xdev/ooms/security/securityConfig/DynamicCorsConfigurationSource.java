package com.xdev.ooms.security.securityConfig;

import com.xdev.ooms.sharedkernel.settings.service.AppSettingsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class DynamicCorsConfigurationSource implements CorsConfigurationSource {

    private final AppSettingsService appSettingsService;

    public DynamicCorsConfigurationSource(AppSettingsService appSettingsService) {
        this.appSettingsService = appSettingsService;
    }

    @Override
    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
        CorsConfiguration cors = new CorsConfiguration();
        List<String> allowedOriginPatterns = new ArrayList<>(List.of(
                "http://localhost:4200",
                "http://127.0.0.1:4200",
                "http://localhost:*",
                "http://127.0.0.1:*",
                "https://*.up.railway.app",
                "https://*.railway.app",
                "https://*.onrender.com",
                "https://www.x-dev.pro",
                "https://x-dev.pro"
        ));

        String frontendBaseUrl = appSettingsService.getString("FRONTEND_ENTRY_POINT", "http://localhost:4200");
        if (StringUtils.hasText(frontendBaseUrl)) {
            allowedOriginPatterns.add(frontendBaseUrl.trim());
        }

        String configuredOriginPatterns = appSettingsService.getString("APP_CORS_ALLOWED_ORIGIN_PATTERNS", "");
        if (StringUtils.hasText(configuredOriginPatterns)) {
            allowedOriginPatterns.addAll(Arrays.stream(configuredOriginPatterns.split(","))
                    .map(String::trim)
                    .filter(pattern -> !pattern.isEmpty())
                    .toList());
        }

        cors.setAllowedOriginPatterns(allowedOriginPatterns);
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With",
                "x-tenant-id",
                "content-disposition"
        ));
        cors.setExposedHeaders(List.of("Content-Disposition", "Authorization"));
        cors.setAllowCredentials(true);
        cors.setMaxAge(3600L);
        return cors;
    }
}
