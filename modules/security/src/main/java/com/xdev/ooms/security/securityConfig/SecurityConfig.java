package com.xdev.ooms.security.securityConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.xdev.ooms.security.user.dto.OSMUserOUTDTO;
import com.xdev.ooms.security.user.entity.OSMUser;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private static final ObjectMapper JWT_CLAIM_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final ModelMapper modelMapper;
    @Value("${spring.security.oauth2.resource-server.jwt.jwk-set-uri}")
    private String jwkSetUri;
    @Value("${app.security.jwt.key-path:./data/osm-jwt-key}")
    private String jwtKeyPath;

    public SecurityConfig(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
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
    public JWKSource<SecurityContext> jwkSource() throws Exception {
        KeyPair keyPair = loadOrCreateKeyPair(Path.of(jwtKeyPath));
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        String keyId = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(MessageDigest.getInstance("SHA-256").digest(publicKey.getEncoded()));
        JWK jwk = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(keyId)
                .build();

        return new ImmutableJWKSet<>(new JWKSet(jwk));
    }

    private KeyPair loadOrCreateKeyPair(Path keyPath) throws Exception {
        Path privateKeyPath = keyPath.resolveSibling(keyPath.getFileName() + ".pk8");
        Path publicKeyPath = keyPath.resolveSibling(keyPath.getFileName() + ".pub");
        if (Files.exists(privateKeyPath) && Files.exists(publicKeyPath)) {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(
                    new PKCS8EncodedKeySpec(Base64.getDecoder().decode(Files.readString(privateKeyPath).trim())));
            RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(
                    new X509EncodedKeySpec(Base64.getDecoder().decode(Files.readString(publicKeyPath).trim())));
            return new KeyPair(publicKey, privateKey);
        }

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        Path parent = privateKeyPath.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        writeKeyAtomically(privateKeyPath,
                Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
        writeKeyAtomically(publicKeyPath,
                Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
        return keyPair;
    }

    private void writeKeyAtomically(Path target, String encodedKey) throws Exception {
        Path temp = Files.createTempFile(target.toAbsolutePath().getParent(), target.getFileName().toString(), ".tmp");
        Files.writeString(temp, encodedKey);
        try {
            Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (java.nio.file.AtomicMoveNotSupportedException e) {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    // Define OAuth2TokenGenerator bean
    @Bean
    public OAuth2TokenGenerator<?> tokenGenerator(JWKSource<com.nimbusds.jose.proc.SecurityContext> jwkSource,
                                                  OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer) throws Exception {
        JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder());
        jwtGenerator.setJwtCustomizer(jwtCustomizer);

        DelegatingOAuth2TokenGenerator refreshTokenGenerator = new DelegatingOAuth2TokenGenerator(
                new OAuth2RefreshTokenGenerator());

        return new DelegatingOAuth2TokenGenerator(jwtGenerator, refreshTokenGenerator);
    }

    @Bean
    JwtEncoder jwtEncoder() throws Exception {
        return new NimbusJwtEncoder(jwkSource());
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
                .build();

    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
        return context -> {
            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                Authentication principal = context.getPrincipal();
                if (principal.getPrincipal() instanceof OSMUser user) {
                    OSMUserOUTDTO dto = modelMapper.map(user, OSMUserOUTDTO.class);
                    dto.getRole().setPermissions(null);
                    dto.setPhotoData(null);
                    dto.setPhotoContentType(null);
                    context.getClaims()
                            .claim("osmUser",
                                    JWT_CLAIM_MAPPER.convertValue(dto, Map.class)
                            )
                            //.claim("permissions", user.getAuthorities())
                            .claim("role", user.getRole().getRoleName())
                            .claim("authorities", user.getAuthorities().stream()
                                    .map(GrantedAuthority::getAuthority)
                                    .toList());

                }
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
