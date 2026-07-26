package com.xdev.ooms.hr.agent.dto;

import com.xdev.ooms.hr.agent.enums.AgentRiskLevel;

public class AgentResponse {

    private boolean success;
    private String intent;
    private String toolCalled;
    private AgentRiskLevel riskLevel;
    private boolean confirmationRequired;
    private String message;
    private Object data;

    public static AgentResponse ok(String intent, String tool, AgentRiskLevel risk, String message, Object data) {
        AgentResponse r = new AgentResponse();
        r.success = true;
        r.intent = intent;
        r.toolCalled = tool;
        r.riskLevel = risk;
        r.confirmationRequired = false;
        r.message = message;
        r.data = data;
        return r;
    }

    public static AgentResponse needsConfirmation(String intent, String tool, AgentRiskLevel risk, String message) {
        AgentResponse r = new AgentResponse();
        r.success = false;
        r.intent = intent;
        r.toolCalled = tool;
        r.riskLevel = risk;
        r.confirmationRequired = true;
        r.message = message;
        return r;
    }

    public static AgentResponse error(String message) {
        AgentResponse r = new AgentResponse();
        r.success = false;
        r.message = message;
        return r;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public String getToolCalled() {
        return toolCalled;
    }

    public void setToolCalled(String toolCalled) {
        this.toolCalled = toolCalled;
    }

    public AgentRiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(AgentRiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public boolean isConfirmationRequired() {
        return confirmationRequired;
    }

    public void setConfirmationRequired(boolean confirmationRequired) {
        this.confirmationRequired = confirmationRequired;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
