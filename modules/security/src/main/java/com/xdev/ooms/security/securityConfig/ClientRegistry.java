package com.xdev.ooms.security.securityConfig;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;
import java.util.UUID;

@Configuration
public class ClientRegistry {
    @Value("${oauth2.client.id}")
    private String clientId;

    public ClientRegistry() {
    }

//    @Bean
//    public RegisteredClientRepository registeredClientRepository() {
//        RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
//                .clientId(clientId)
//                .clientSecret(passwordEncoder.encode(clientSecret))
//                .scope(OidcScopes.OPENID)
//                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
//                .authorizationGrantType(new AuthorizationGrantType("TOKEN"))
//                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
//                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
//                .authorizationGrantType(AuthorizationGrantType.JWT_BEARER)
//                .tokenSettings(TokenSettings.builder()
//                        .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
//                        .accessTokenTimeToLive(Duration.ofMinutes(5))
//                        .refreshTokenTimeToLive(Duration.ofHours(1))
//                        .reuseRefreshTokens(false)
//                        .build())
//                .scope("read")
//                .scope("write")
//                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(false).build())
//                .build();
//        return new InMemoryRegisteredClientRepository(client);
//    }

    @Bean
    @DependsOnDatabaseInitialization
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        JdbcRegisteredClientRepository repository = new JdbcRegisteredClientRepository(jdbcTemplate);
        RegisteredClient existingClient = repository.findByClientId(clientId);

        if (existingClient == null) {
            RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId(clientId)
                    .scope(OidcScopes.OPENID)
                    .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                    .authorizationGrantType(new AuthorizationGrantType("TOKEN"))
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    .tokenSettings(TokenSettings.builder()
                            .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                            .accessTokenTimeToLive(Duration.ofMinutes(5))
                            .refreshTokenTimeToLive(Duration.ofHours(24))
                            .reuseRefreshTokens(false)
                            .build())
                    .scope("read")
                    .scope("write")
                    .clientSettings(ClientSettings.builder().requireAuthorizationConsent(false).build())
                    .build();

            repository.save(client);
        } else {
            jdbcTemplate.update(
                    "UPDATE oauth2_registered_client " +
                            "SET client_secret = NULL, client_secret_expires_at = NULL, " +
                            "client_authentication_methods = ?, authorization_grant_types = ? " +
                            "WHERE client_id = ?",
                    ClientAuthenticationMethod.NONE.getValue(),
                    "TOKEN," + AuthorizationGrantType.REFRESH_TOKEN.getValue(),
                    clientId
            );
        }

        return repository;
    }

}
