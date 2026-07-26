package com.xdev.ooms.hr.agent.entity;

import com.xdev.ooms.hr.agent.enums.AgentRiskLevel;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "hr_agent_action_log")
public class HrAgentActionLog extends BaseEntity implements Serializable {

    private UUID userId;

    @Column(columnDefinition = "TEXT")
    private String prompt;

    @Column(length = 128)
    private String intent;

    @Column(length = 128)
    private String toolCalled;

    @Column(columnDefinition = "TEXT")
    private String parameters;

    @Column(columnDefinition = "TEXT")
    private String result;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private AgentRiskLevel riskLevel;

    private Boolean confirmationRequired;
    private Boolean confirmed;
    private UUID approvedBy;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
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

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public AgentRiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(AgentRiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public Boolean getConfirmationRequired() {
        return confirmationRequired;
    }

    public void setConfirmationRequired(Boolean confirmationRequired) {
        this.confirmationRequired = confirmationRequired;
    }

    public Boolean getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
    }

    public UUID getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(UUID approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
