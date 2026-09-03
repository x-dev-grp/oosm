package com.xdev.ooms.sharedkernel.notifications;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xdev.ooms.sharedkernel.notifications.dto.NotificationRequest;
import com.xdev.ooms.sharedkernel.settings.service.AppSettingsService;
import com.xdev.ooms.sharedkernel.utils.OOSMLogger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class FcmPushService implements PushNotificationSender {

    public static final String FCM_PROJECT_ID = "FCM_PROJECT_ID";
    public static final String FCM_CLIENT_EMAIL = "FCM_CLIENT_EMAIL";
    public static final String FCM_PRIVATE_KEY = "FCM_PRIVATE_KEY";
    public static final String FCM_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    public static final String TOKEN_URL = "https://oauth2.googleapis.com/token";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;
    private final AppSettingsService appSettingsService;
    private final AtomicReference<CachedToken> cachedToken = new AtomicReference<>();

    public FcmPushService(AppSettingsService appSettingsService, ObjectMapper objectMapper) {
        this.appSettingsService = appSettingsService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String sendNotification(NotificationRequest notificationRequest) {
        String projectId = appSettingsService.getString(FCM_PROJECT_ID, "").trim();
        String clientEmail = appSettingsService.getString(FCM_CLIENT_EMAIL, "").trim();
        Optional<String> privateKeyPem = appSettingsService.getSecret(FCM_PRIVATE_KEY);

        if (!StringUtils.hasText(projectId) || !StringUtils.hasText(clientEmail) || privateKeyPem.isEmpty()) {
            OOSMLogger.warn(FcmPushService.class, "FCM is not configured (missing project ID, client email, or private key)");
            return "FCM_NOT_CONFIGURED";
        }

        List<String> tokens = notificationRequest.getUserIds();
        if (tokens == null || tokens.isEmpty()) {
            return "FCM_ERROR: no device tokens";
        }

        String accessToken;
        try {
            accessToken = getAccessToken(clientEmail, privateKeyPem.get());
        } catch (Exception ex) {
            OOSMLogger.logException(FcmPushService.class, "Failed to obtain FCM access token", ex);
            return "FCM_ERROR: access token - " + ex.getMessage();
        }

        String endpoint = "https://fcm.googleapis.com/v1/projects/" + projectId + "/messages:send";
        int successCount = 0;
        StringBuilder errors = new StringBuilder();

        for (String token : tokens) {
            if (!StringUtils.hasText(token)) {
                continue;
            }
            try {
                String result = sendOne(endpoint, accessToken, token.trim(), notificationRequest);
                if ("SUCCESS".equals(result)) {
                    successCount++;
                } else {
                    if (!errors.isEmpty()) {
                        errors.append(" | ");
                    }
                    errors.append(result);
                }
            } catch (org.springframework.web.client.HttpStatusCodeException e) {
                OOSMLogger.error(FcmPushService.class, "FCM request failed with status {}: {}",
                        e.getStatusCode(), e.getResponseBodyAsString(), e);
                if (!errors.isEmpty()) {
                    errors.append(" | ");
                }
                errors.append("FCM_ERROR: ").append(e.getStatusCode()).append(" - ").append(e.getResponseBodyAsString());
            } catch (Exception e) {
                OOSMLogger.logException(FcmPushService.class, "Unexpected FCM request failure", e);
                if (!errors.isEmpty()) {
                    errors.append(" | ");
                }
                errors.append("INTERNAL_ERROR: ").append(e.getMessage());
            }
        }

        if (successCount > 0 && errors.isEmpty()) {
            return "SUCCESS";
        }
        if (successCount > 0) {
            return "FCM_PARTIAL_SUCCESS: " + successCount + " ok; " + errors;
        }
        return errors.isEmpty() ? "FCM_ERROR: no tokens sent" : errors.toString();
    }

    private String sendOne(String endpoint, String accessToken, String deviceToken, NotificationRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        Map<String, Object> notification = new LinkedHashMap<>();
        if (StringUtils.hasText(request.getTitle())) {
            notification.put("title", request.getTitle());
        }
        if (StringUtils.hasText(request.getMessage())) {
            notification.put("body", request.getMessage());
        }

        Map<String, String> data = new LinkedHashMap<>();
        if (request.getData() != null) {
            request.getData().forEach((k, v) -> {
                if (k != null && v != null) {
                    data.put(k, String.valueOf(v));
                }
            });
        }

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("token", deviceToken);
        if (!notification.isEmpty()) {
            message.put("notification", notification);
        }
        if (!data.isEmpty()) {
            message.put("data", data);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("message", message);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(endpoint, entity, String.class);
        String responseBody = response.getBody();
        OOSMLogger.debug(FcmPushService.class, "FCM response received: {}", responseBody);

        if (response.getStatusCode().is2xxSuccessful()) {
            return "SUCCESS";
        }
        return "FCM_ERROR: " + response.getStatusCode() + " - " + responseBody;
    }

    private String getAccessToken(String clientEmail, String privateKeyPem) throws Exception {
        CachedToken current = cachedToken.get();
        long now = Instant.now().getEpochSecond();
        if (current != null && current.expiresAtEpochSec() > now + 60) {
            return current.value();
        }

        synchronized (this) {
            current = cachedToken.get();
            now = Instant.now().getEpochSecond();
            if (current != null && current.expiresAtEpochSec() > now + 60) {
                return current.value();
            }

            String assertion = buildServiceAccountJwt(clientEmail, privateKeyPem, now);
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");
            form.add("assertion", assertion);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(TOKEN_URL, entity, String.class);
            JsonNode json = objectMapper.readTree(response.getBody());
            String accessToken = json.path("access_token").asText(null);
            int expiresIn = json.path("expires_in").asInt(3600);
            if (!StringUtils.hasText(accessToken)) {
                throw new IllegalStateException("OAuth token response missing access_token: " + response.getBody());
            }
            cachedToken.set(new CachedToken(accessToken, now + expiresIn));
            return accessToken;
        }
    }

    private String buildServiceAccountJwt(String clientEmail, String privateKeyPem, long nowEpochSec) throws Exception {
        String headerJson = "{\"alg\":\"RS256\",\"typ\":\"JWT\"}";
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("iss", clientEmail);
        claims.put("scope", FCM_SCOPE);
        claims.put("aud", TOKEN_URL);
        claims.put("iat", nowEpochSec);
        claims.put("exp", nowEpochSec + 3600);
        String payloadJson = objectMapper.writeValueAsString(claims);

        String header = base64Url(headerJson.getBytes(StandardCharsets.UTF_8));
        String payload = base64Url(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signingInput = header + "." + payload;

        PrivateKey privateKey = parsePkcs8PrivateKey(privateKeyPem);
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(signingInput.getBytes(StandardCharsets.UTF_8));
        String sig = base64Url(signature.sign());
        return signingInput + "." + sig;
    }

    private static PrivateKey parsePkcs8PrivateKey(String pem) throws Exception {
        String normalized = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replace("\\n", "\n")
                .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(normalized);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
    }

    private static String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private record CachedToken(String value, long expiresAtEpochSec) {
    }
}
