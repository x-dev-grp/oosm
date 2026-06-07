package com.xdev.ooms.sharedkernel.notifications.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private List<String> userIds;
    private String title;
    private String message;
    private Map<String, String> data;   // pour navigation mobile
}