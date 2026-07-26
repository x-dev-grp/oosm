package com.xdev.ooms.hr.compliance.dto;

import com.xdev.ooms.hr.compliance.entity.HrComplianceViolation;
import com.xdev.ooms.hr.compliance.enums.ComplianceSeverity;
import com.xdev.ooms.hr.compliance.enums.ComplianceViolationStatus;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.time.LocalDateTime;
import java.util.UUID;

public class HrComplianceViolationDto extends BaseDto<HrComplianceViolation> {

    private String code;
    private ComplianceSeverity severity;
    private String entityType;
    private UUID entityId;
    private UUID employeeId;
    private String title;
    private String description;
    private String whyItMatters;
    private String recommendedAction;
    private String relatedRuleCode;
    private ComplianceViolationStatus status;
    private LocalDateTime detectedAt;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public ComplianceSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(ComplianceSeverity severity) {
        this.severity = severity;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    public UUID getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(UUID employeeId) {
        this.employeeId = employeeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWhyItMatters() {
        return whyItMatters;
    }

    public void setWhyItMatters(String whyItMatters) {
        this.whyItMatters = whyItMatters;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }

    public void setRecommendedAction(String recommendedAction) {
        this.recommendedAction = recommendedAction;
    }

    public String getRelatedRuleCode() {
        return relatedRuleCode;
    }

    public void setRelatedRuleCode(String relatedRuleCode) {
        this.relatedRuleCode = relatedRuleCode;
    }

    public ComplianceViolationStatus getStatus() {
        return status;
    }

    public void setStatus(ComplianceViolationStatus status) {
        this.status = status;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(LocalDateTime detectedAt) {
        this.detectedAt = detectedAt;
    }
}
