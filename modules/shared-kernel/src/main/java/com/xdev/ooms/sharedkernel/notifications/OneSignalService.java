package com.xdev.ooms.sharedkernel.notifications;

import com.xdev.ooms.sharedkernel.notifications.dto.NotificationRequest;
import com.xdev.ooms.sharedkernel.notifications.impl.OneSignalServiceImpl;
import com.xdev.ooms.sharedkernel.utils.OSMLogger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class OneSignalService  implements OneSignalServiceImpl {

    public static final String AUTHORIZATION = "Authorization";
    public static final String BASIC = "Basic ";
    public static final String APP_ID = "app_id";
    public static final String INCLUDE_PLAYER_IDS = "include_player_ids";
    public static final String EN = "en";
    public static final String CONTENTS = "contents";
    public static final String HEADINGS = "headings";
    public static final String DATA = "data";
    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${onesignal.app-id}")
    private String appId;
    @Value("${onesignal.api-key}")
    private String apiKey;
    @Value("${onesignal.endpoint}")
    private String endpoint;

    public String sendNotification(NotificationRequest notificationRequest) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(AUTHORIZATION, BASIC + apiKey);

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
            OSMLogger.debug(OneSignalService.class, "OneSignal response received: {}", responseBody);

            if (responseBody != null && responseBody.contains("\"recipients\":0")) {
                return "ONESIGNAL_WARNING: 0 recipients (Player ID is invalid or unsubscribed) - " + responseBody;
            }
            if (responseBody != null && responseBody.contains("\"errors\"")) {
                return "ONESIGNAL_ERROR_IN_200: " + responseBody;
            }

            return "SUCCESS";
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            OSMLogger.error(OneSignalService.class, "OneSignal request failed with status {}: {}",
                    e.getStatusCode(), e.getResponseBodyAsString(), e);
            return "ONESIGNAL_ERROR: " + e.getStatusCode() + " - " + e.getResponseBodyAsString();
        } catch (Exception e) {
            OSMLogger.logException(OneSignalService.class, "Unexpected OneSignal request failure", e);
            return "INTERNAL_ERROR: " + e.getMessage();
        }
    }
}
