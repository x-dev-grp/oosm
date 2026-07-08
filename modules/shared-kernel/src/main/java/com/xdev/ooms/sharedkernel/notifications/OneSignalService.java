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
import java.util.Map;
import java.util.Optional;

@Service
public class OneSignalService implements OneSignalServiceImpl {

    public static final String AUTHORIZATION = "Authorization";
    public static final String BASIC = "Basic ";
    public static final String APP_ID = "app_id";
    public static final String INCLUDE_PLAYER_IDS = "include_player_ids";
    public static final String EN = "en";
    public static final String CONTENTS = "contents";
    public static final String HEADINGS = "headings";
    public static final String DATA = "data";

    private final RestTemplate restTemplate = new RestTemplate();
    private final AppSettingsService appSettingsService;

    public OneSignalService(AppSettingsService appSettingsService) {
        this.appSettingsService = appSettingsService;
    }

    public String sendNotification(NotificationRequest notificationRequest) {
        String appId = appSettingsService.getString("ONESIGNAL_APP_ID", "");
        Optional<String> apiKey = appSettingsService.getSecret("ONESIGNAL_API_KEY");
        String endpoint = appSettingsService.getString(
                "ONESIGNAL_ENDPOINT", "https://onesignal.com/api/v1/notifications");

        if (!StringUtils.hasText(appId) || apiKey.isEmpty()) {
            OOSMLogger.warn(OneSignalService.class, "OneSignal is not configured (missing app ID or API key)");
            return "ONESIGNAL_NOT_CONFIGURED";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(AUTHORIZATION, BASIC + apiKey.get());

        Map<String, Object> body = new HashMap<>();
        body.put(APP_ID, appId);
        body.put(INCLUDE_PLAYER_IDS, notificationRequest.getUserIds());

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
                return "ONESIGNAL_WARNING: 0 recipients (Player ID is invalid or unsubscribed) - " + responseBody;
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
