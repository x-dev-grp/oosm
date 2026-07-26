package com.xdev.ooms.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "springdoc.api-docs.enabled", havingValue = "true")
public class OpenApiConfig {

    private static final String BEARER_JWT = "bearer-jwt";

    @Value("${info.app.version:0.0.0}")
    private String appVersion;

    @Bean
    public OpenAPI oosmOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ZitFlow API")
                        .version(appVersion)
                        .description("""
                                ZitFlow modular monolith REST API.
                                
                                Authentication: obtain a JWT via POST /oauth2/token with grant_type=TOKEN,
                                username, password, and OAuth client credentials, then use Authorize with
                                Bearer <token>. The embedded admin UI at /administration/api-docs attaches
                                the token automatically for Try it out requests.
                                """))
                .components(new Components()
                        .addSecuritySchemes(BEARER_JWT, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT access token from /oauth2/token (grant_type=TOKEN)")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_JWT));
    }

    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("all")
                .pathsToMatch("/api/**")
                .build();
    }

    @Bean
    public GroupedOpenApi securityApi() {
        return GroupedOpenApi.builder()
                .group("security")
                .pathsToMatch("/api/security/**", "/api/notifications/**")
                .build();
    }

    @Bean
    public GroupedOpenApi productionApi() {
        return GroupedOpenApi.builder()
                .group("production")
                .pathsToMatch("/api/production/**")
                .build();
    }

    @Bean
    public GroupedOpenApi inventoryApi() {
        return GroupedOpenApi.builder()
                .group("inventory")
                .pathsToMatch("/api/inventaire/**")
                .build();
    }

    @Bean
    public GroupedOpenApi conditioningApi() {
        return GroupedOpenApi.builder()
                .group("conditioning")
                .pathsToMatch("/api/ordreConditionement/**")
                .build();
    }

    @Bean
    public GroupedOpenApi financeApi() {
        return GroupedOpenApi.builder()
                .group("finance")
                .pathsToMatch("/api/finance/**")
                .build();
    }

    @Bean
    public GroupedOpenApi hrApi() {
        return GroupedOpenApi.builder()
                .group("hr")
                .pathsToMatch("/api/hr/**")
                .build();
    }

    @Bean
    public GroupedOpenApi administrationApi() {
        return GroupedOpenApi.builder()
                .group("administration")
                .pathsToMatch("/api/admin/**")
                .build();
    }

    @Bean
    public GroupedOpenApi documentsApi() {
        return GroupedOpenApi.builder()
                .group("documents")
                .pathsToMatch("/api/documents/**")
                .build();
    }
}
