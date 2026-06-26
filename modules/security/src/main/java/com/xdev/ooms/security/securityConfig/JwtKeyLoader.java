package com.xdev.ooms.security.securityConfig;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Component
public class JwtKeyLoader {

    private static final String KEY_ID = "oosm-jwt-secret";

    private final SecretKey secretKey;
    private final byte[] secretBytes;

    public JwtKeyLoader(@Value("${app.security.jwt.secret:}") String jwtSecret) {
        if (!StringUtils.hasText(jwtSecret)) {
            throw new IllegalStateException("Set app.security.jwt.secret or JWT_SECRET.");
        }
        this.secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        this.secretKey = new SecretKeySpec(secretBytes, "HmacSHA256");
    }

    public JWKSource<SecurityContext> jwkSource() {
        JWK jwk = new OctetSequenceKey.Builder(secretBytes)
                .keyID(KEY_ID)
                .algorithm(JWSAlgorithm.HS256)
                .keyUse(KeyUse.SIGNATURE)
                .build();
        return new ImmutableJWKSet<>(new JWKSet(jwk));
    }

    public SecretKey secretKey() {
        return secretKey;
    }
}
