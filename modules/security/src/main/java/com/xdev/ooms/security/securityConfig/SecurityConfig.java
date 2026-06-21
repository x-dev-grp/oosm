package com.xdev.ooms.security.securityConfig;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.xdev.ooms.security.user.entity.OSMUser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtKeyLoader jwtKeyLoader;

    public SecurityConfig(JwtKeyLoader jwtKeyLoader) {
        this.jwtKeyLoader = jwtKeyLoader;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


//    @Bean
//    public AuthorizationServerSettings authorizationServerSettings() {
//        return AuthorizationServerSettings.builder()
//                .issuer("http://localhost:8081")
//                .build();
//    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        return jwtKeyLoader.jwkSource();
    }

    // Define OAuth2TokenGenerator bean
    @Bean
    public OAuth2TokenGenerator<?> tokenGenerator(JWKSource<SecurityContext> jwkSource,
                                                  OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer) {
        JwtGenerator jwtGenerator = new JwtGenerator(new NimbusJwtEncoder(jwkSource));
        jwtGenerator.setJwtCustomizer(jwtCustomizer);

        DelegatingOAuth2TokenGenerator refreshTokenGenerator = new DelegatingOAuth2TokenGenerator(
                new OAuth2RefreshTokenGenerator());

        return new DelegatingOAuth2TokenGenerator(jwtGenerator, refreshTokenGenerator);
    }

    @Bean
    JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(jwtKeyLoader.secretKey())
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
        return context -> {
            if (!OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                return;
            }
            // Authorization server JwtGenerator defaults to RS256; align with symmetric secret.
            context.getJwsHeader().algorithm(MacAlgorithm.HS256);

            Authentication principal = context.getPrincipal();
            if (principal.getPrincipal() instanceof OSMUser user) {
                Map<String, Object> slimUser = new LinkedHashMap<>();
                slimUser.put("id", user.getId());
                slimUser.put("tenantId", user.getTenantId());
                slimUser.put("username", user.getUsername());
                slimUser.put("firstName", user.getFirstName());
                slimUser.put("lastName", user.getLastName());
                slimUser.put("email", user.getEmail());
                slimUser.put("isNewUser", user.isNewUser());

                context.getClaims()
                        .claim("osmUser", slimUser)
                        .claim("role", user.getRole().getRoleName());
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
