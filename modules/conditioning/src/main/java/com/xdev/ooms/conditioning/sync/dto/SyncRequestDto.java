package com.xdev.ooms.conditioning.sync.dto;

public class SyncRequestDto {
    private String operationId;
    private String url;
    private String method;
    private String body;

    public String getOperationId() {
        return operationId;
    }

    public String getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    public String getBody() {
        return body;
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

    public void setBody(String body) {
        this.body = body;
    }
}