package com.xdev.ooms.conditioning.sync.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
public class OfflineOperation extends BaseEntity implements Serializable {

    @Column(nullable = false)
    private String operationId;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String method;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode requestBody;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode responseBody;

    @Column(nullable = false)
    private String status;

    private String errorMessage;

    private LocalDateTime syncedAt;

    public String getOperationId() {
        return operationId;
    }

    public String getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    public JsonNode getRequestBody() {
        return requestBody;
    }

    public JsonNode getResponseBody() {
        return responseBody;
    }

    public String getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getSyncedAt() {
        return syncedAt;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setRequestBody(JsonNode requestBody) {
        this.requestBody = requestBody;
    }

    public void setResponseBody(JsonNode responseBody) {
        this.responseBody = responseBody;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setSyncedAt(LocalDateTime syncedAt) {
        this.syncedAt = syncedAt;
    }
}