package com.xdev.ooms.hr.compliance.entity;

import com.xdev.ooms.hr.compliance.enums.ComplianceSeverity;
import com.xdev.ooms.hr.compliance.enums.ComplianceViolationStatus;
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
@Table(name = "hr_compliance_violation")
public class HrComplianceViolation extends BaseEntity implements Serializable {

    @Column(nullable = false, length = 64)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ComplianceSeverity severity;

    @Column(length = 64)
    private String entityType;

    private UUID entityId;

    private UUID employeeId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String whyItMatters;

    @Column(columnDefinition = "TEXT")
    private String recommendedAction;

    @Column(length = 64)
    private String relatedRuleCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ComplianceViolationStatus status;

    @Column(nullable = false)
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
