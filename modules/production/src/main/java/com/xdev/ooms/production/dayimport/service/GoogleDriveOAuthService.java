package com.xdev.ooms.production.dayimport.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xdev.ooms.production.dayimport.entity.TenantGoogleDriveCredential;
import com.xdev.ooms.production.dayimport.repository.TenantGoogleDriveCredentialRepository;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import com.xdev.ooms.sharedkernel.settings.service.AppSettingEncryptionService;
import com.xdev.ooms.sharedkernel.utils.OOSMLogger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
public class GoogleDriveOAuthService {

    public static final String PROVIDER = "GOOGLE_DRIVE";
    public static final String AAD_KEY_PREFIX = "tenant-gdrive-refresh:";
    public static final String SCOPES = "https://www.googleapis.com/auth/drive https://www.googleapis.com/auth/userinfo.email";

    private final TenantGoogleDriveCredentialRepository credentialRepository;
    private final AppSettingEncryptionService encryptionService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${oosm.import.gdrive.oauth.client-id:}")
    private String clientId;

    @Value("${oosm.import.gdrive.oauth.client-secret:}")
    private String clientSecret;

    @Value("${oosm.import.gdrive.oauth.redirect-uri:}")
    private String redirectUri;

    @Value("${oosm.import.gdrive.oauth.frontend-redirect:}")
    private String frontendRedirect;

    @Value("${oosm.import.gdrive.oauth.state-secret:}")
    private String stateSecret;

    public GoogleDriveOAuthService(
            TenantGoogleDriveCredentialRepository credentialRepository,
            AppSettingEncryptionService encryptionService,
            ObjectMapper objectMapper) {
        this.credentialRepository = credentialRepository;
        this.encryptionService = encryptionService;
        this.objectMapper = objectMapper;
    }

    public boolean isOAuthConfigured() {
        return StringUtils.hasText(clientId)
                && StringUtils.hasText(clientSecret)
                && StringUtils.hasText(redirectUri);
    }

    public String buildAuthorizeUrl(UUID tenantId) {
        OOSMLogger.logMethodEntry(getClass(), "buildAuthorizeUrl", tenantId);
        if (!isOAuthConfigured()) {
            OOSMLogger.warn(getClass(), "[buildAuthorizeUrl] OAuth not configured");
            throw new IllegalStateException("Google Drive OAuth is not configured (client-id/secret/redirect-uri)");
        }
        if (tenantId == null) {
            OOSMLogger.warn(getClass(), "[buildAuthorizeUrl] tenant missing");
            throw new IllegalStateException("Tenant is required to connect Google Drive");
        }
        String state = signState(tenantId);
        String url = "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + enc(clientId)
                + "&redirect_uri=" + enc(redirectUri)
                + "&response_type=code"
                + "&scope=" + enc(SCOPES)
                + "&access_type=offline"
                + "&prompt=consent"
                + "&include_granted_scopes=true"
                + "&state=" + enc(state);
        OOSMLogger.logBusinessEvent(getClass(), "GDRIVE_OAUTH_URL_BUILT", "tenantId=" + tenantId);
        return url;
    }

    public String handleCallback(String code, String state) throws Exception {
        long start = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(getClass(), "handleCallback");
        try {
            UUID tenantId = verifyState(state);
            TokenResponse tokens = exchangeCode(code);
            if (!StringUtils.hasText(tokens.refreshToken())) {
                OOSMLogger.warn(getClass(), "[handleCallback] missing refresh token tenant={}", tenantId);
                throw new IllegalStateException("Google did not return a refresh token. Revoke app access and reconnect with consent.");
            }
            String email = fetchEmail(tokens.accessToken());
            persistCredential(tenantId, tokens.refreshToken(), tokens.scope(), email);
            OOSMLogger.logBusinessEvent(getClass(), "GDRIVE_OAUTH_CONNECTED",
                    "tenantId=" + tenantId + " email=" + email);
            OOSMLogger.logPerformance(getClass(), "handleCallback", start, System.currentTimeMillis());
            return buildFrontendRedirect(true, email, null);
        } catch (Exception e) {
            OOSMLogger.logException(getClass(), "handleCallback failed", e);
            throw e;
        }
    }

    public String buildFrontendRedirect(boolean success, String email, String error) {
        String base = StringUtils.hasText(frontendRedirect)
                ? frontendRedirect
                : "/reception/import";
        UriComponentsBuilder b = UriComponentsBuilder.fromUriString(base)
                .queryParam("gdrive", success ? "connected" : "error");
        if (StringUtils.hasText(email)) {
            b.queryParam("email", email);
        }
        if (StringUtils.hasText(error)) {
            b.queryParam("message", error);
        }
        return b.build().toUriString();
    }

    @Transactional
    public void disconnect(UUID tenantId) {
        OOSMLogger.logMethodEntry(getClass(), "disconnect", tenantId);
        if (tenantId == null) {
            return;
        }
        credentialRepository.findFirstByTenantIdAndIsDeletedFalse(tenantId).ifPresent(cred -> {
            cred.setDeleted(true);
            cred.setEncryptedRefreshToken("deleted");
            credentialRepository.save(cred);
            OOSMLogger.logBusinessEvent(getClass(), "GDRIVE_OAUTH_DISCONNECTED", "tenantId=" + tenantId);
        });
    }

    public Optional<TenantGoogleDriveCredential> findActive(UUID tenantId) {
        if (tenantId == null) {
            return Optional.empty();
        }
        return credentialRepository.findFirstByTenantIdAndIsDeletedFalse(tenantId);
    }

    public boolean isConnected(UUID tenantId) {
        return findActive(tenantId).isPresent();
    }

    public String getAccessTokenForCurrentTenant() throws Exception {
        UUID tenantId = TenantContext.getCurrentTenant();
        TenantGoogleDriveCredential cred = findActive(tenantId)
                .orElseThrow(() -> new IllegalStateException("Google Drive is not connected for this tenant"));
        String refresh = encryptionService.decrypt(aadKey(tenantId), cred.getEncryptedRefreshToken());
        return refreshAccessToken(refresh).accessToken();
    }

    private void persistCredential(UUID tenantId, String refreshToken, String scope, String email) {
        TenantContext.setCurrentTenant(tenantId);
        try {
            TenantGoogleDriveCredential cred = credentialRepository.findFirstByTenantIdAndIsDeletedFalse(tenantId)
                    .or(() -> credentialRepository.findFirstByTenantIdOrderByLastModifiedDateDesc(tenantId))
                    .orElseGet(TenantGoogleDriveCredential::new);
            cred.setTenantId(tenantId);
            cred.setDeleted(false);
            cred.setGoogleAccountEmail(email);
            cred.setScope(scope);
            cred.setEncryptedRefreshToken(encryptionService.encrypt(aadKey(tenantId), refreshToken));
            Instant now = Instant.now();
            if (cred.getConnectedAt() == null) {
                cred.setConnectedAt(now);
            }
            cred.setTokenUpdatedAt(now);
            credentialRepository.save(cred);
        } finally {
            TenantContext.clear();
        }
    }

    private TokenResponse exchangeCode(String code) throws Exception {
        String body = "code=" + enc(code)
                + "&client_id=" + enc(clientId)
                + "&client_secret=" + enc(clientSecret)
                + "&redirect_uri=" + enc(redirectUri)
                + "&grant_type=authorization_code";
        return postToken(body);
    }

    private TokenResponse refreshAccessToken(String refreshToken) throws Exception {
        String body = "refresh_token=" + enc(refreshToken)
                + "&client_id=" + enc(clientId)
                + "&client_secret=" + enc(clientSecret)
                + "&grant_type=refresh_token";
        return postToken(body);
    }

    private TokenResponse postToken(String formBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://oauth2.googleapis.com/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 300) {
            throw new IllegalStateException("Google token exchange failed: HTTP " + response.statusCode() + " " + response.body());
        }
        JsonNode node = objectMapper.readTree(response.body());
        return new TokenResponse(
                text(node, "access_token"),
                text(node, "refresh_token"),
                text(node, "scope"),
                node.path("expires_in").asLong(0)
        );
    }

    private String fetchEmail(String accessToken) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.googleapis.com/oauth2/v2/userinfo"))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 300) {
            return null;
        }
        JsonNode node = objectMapper.readTree(response.body());
        return text(node, "email");
    }

    private String signState(UUID tenantId) {
        String nonce = UUID.randomUUID().toString().replace("-", "");
        String payload = tenantId + "." + nonce + "." + Instant.now().getEpochSecond();
        String sig = hmac(payload);
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString((payload + "." + sig).getBytes(StandardCharsets.UTF_8));
    }

    private UUID verifyState(String state) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(state), StandardCharsets.UTF_8);
            String[] parts = decoded.split("\\.");
            if (parts.length != 4) {
                throw new IllegalArgumentException("Invalid OAuth state");
            }
            String payload = parts[0] + "." + parts[1] + "." + parts[2];
            String expected = hmac(payload);
            if (!expected.equals(parts[3])) {
                throw new IllegalArgumentException("OAuth state signature mismatch");
            }
            long ts = Long.parseLong(parts[2]);
            if (Math.abs(Instant.now().getEpochSecond() - ts) > 900) {
                throw new IllegalArgumentException("OAuth state expired");
            }
            return UUID.fromString(parts[0]);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid OAuth state", e);
        }
    }

    private String hmac(String payload) {
        try {
            String secret = StringUtils.hasText(stateSecret) ? stateSecret : (clientSecret + "|" + clientId);
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to sign OAuth state", e);
        }
    }

    private static String aadKey(UUID tenantId) {
        return AAD_KEY_PREFIX + tenantId;
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? null : v.asText();
    }

    private record TokenResponse(String accessToken, String refreshToken, String scope, long expiresIn) {
    }
}
