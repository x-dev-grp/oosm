package com.xdev.ooms.security.notification.dto;

import com.xdev.ooms.security.notification.entity.UserNotification;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class UserNotificationDto extends BaseDto<UserNotification> {

    private String ruleCode;
    private String module;
    private String entity;
    private UUID entityId;
    private String title;
    private String recap;
    private String priority;
    private String actorDisplayName;
    private String webRoute;
    private Map<String, String> payload;
    private boolean read;
    private LocalDateTime readAt;

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRecap() {
        return recap;
    }

    public void setRecap(String recap) {
        this.recap = recap;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getActorDisplayName() {
        return actorDisplayName;
    }

    public void setActorDisplayName(String actorDisplayName) {
        this.actorDisplayName = actorDisplayName;
    }

    public String getWebRoute() {
        return webRoute;
    }

    public void setWebRoute(String webRoute) {
        this.webRoute = webRoute;
    }

    public Map<String, String> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, String> payload) {
        this.payload = payload;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
}
