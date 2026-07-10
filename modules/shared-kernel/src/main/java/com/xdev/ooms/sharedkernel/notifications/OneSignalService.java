package com.xdev.ooms.sharedkernel.notifications;

import com.xdev.ooms.sharedkernel.notifications.dto.NotificationRequest;
import com.xdev.ooms.sharedkernel.notifications.impl.OneSignalServiceImpl;
import com.xdev.ooms.sharedkernel.settings.service.AppSettingsService;
import com.xdev.ooms.sharedkernel.utils.OOSMLogger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OneSignalService implements OneSignalServiceImpl {

    public static final String AUTHORIZATION = "Authorization";
    public static final String KEY_PREFIX = "Key ";
    public static final String APP_ID = "app_id";
    public static final String INCLUDE_SUBSCRIPTION_IDS = "include_subscription_ids";
    public static final String TARGET_CHANNEL = "target_channel";
    public static final String EN = "en";
    public static final String CONTENTS = "contents";
    public static final String HEADINGS = "headings";
    public static final String DATA = "data";
    public static final String DEFAULT_ENDPOINT = "https://api.onesignal.com/notifications";

    private final RestTemplate restTemplate = new RestTemplate();
    private final AppSettingsService appSettingsService;

    public OneSignalService(AppSettingsService appSettingsService) {
        this.appSettingsService = appSettingsService;
    }

    public String sendNotification(NotificationRequest notificationRequest) {
        String appId = appSettingsService.getString("ONESIGNAL_APP_ID", "");
        Optional<String> apiKey = appSettingsService.getSecret("ONESIGNAL_API_KEY");
        String endpoint = appSettingsService.getString("ONESIGNAL_ENDPOINT", DEFAULT_ENDPOINT);
        if (!StringUtils.hasText(endpoint) || endpoint.contains("onesignal.com/api/v1/")) {
            endpoint = DEFAULT_ENDPOINT;
        }

        if (!StringUtils.hasText(appId) || apiKey.isEmpty()) {
            OOSMLogger.warn(OneSignalService.class, "OneSignal is not configured (missing app ID or API key)");
            return "ONESIGNAL_NOT_CONFIGURED";
        }

        List<String> subscriptionIds = notificationRequest.getUserIds();
        if (subscriptionIds == null || subscriptionIds.isEmpty()) {
            return "ONESIGNAL_ERROR: no subscription ids";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Modern App API keys (os_v2_app_*) require "Key", not "Basic".
        headers.set(AUTHORIZATION, KEY_PREFIX + apiKey.get().trim());

        Map<String, Object> body = new HashMap<>();
        body.put(APP_ID, appId);
        body.put(INCLUDE_SUBSCRIPTION_IDS, subscriptionIds);
        body.put(TARGET_CHANNEL, "push");

        Map<String, String> contents = new HashMap<>();
        contents.put(EN, notificationRequest.getMessage());
        body.put(CONTENTS, contents);
        var title = notificationRequest.getTitle();
        var data = notificationRequest.getData();
        if (title != null && !title.isEmpty()) {
            Map<String, String> headings = new HashMap<>();
            headings.put(EN, title);
            body.put(HEADINGS, headings);
        }

        if (data != null && !data.isEmpty()) {
            body.put(DATA, data);
        }

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, request, String.class);
            String responseBody = response.getBody();
            OOSMLogger.debug(OneSignalService.class, "OneSignal response received: {}", responseBody);

            if (responseBody != null && responseBody.contains("\"recipients\":0")) {
                return "ONESIGNAL_WARNING: 0 recipients (subscription ID invalid or unsubscribed) - " + responseBody;
            }
            if (responseBody != null && responseBody.contains("\"errors\"")) {
                return "ONESIGNAL_ERROR_IN_200: " + responseBody;
            }

            return "SUCCESS";
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            OOSMLogger.error(OneSignalService.class, "OneSignal request failed with status {}: {}",
                    e.getStatusCode(), e.getResponseBodyAsString(), e);
            return "ONESIGNAL_ERROR: " + e.getStatusCode() + " - " + e.getResponseBodyAsString();
        } catch (Exception e) {
            OOSMLogger.logException(OneSignalService.class, "Unexpected OneSignal request failure", e);
            return "INTERNAL_ERROR: " + e.getMessage();
        }
    }
}
