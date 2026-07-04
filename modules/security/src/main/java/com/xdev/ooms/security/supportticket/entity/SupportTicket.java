package com.xdev.ooms.security.supportticket.entity;

import com.xdev.ooms.security.supportticket.enums.SupportTicketPriority;
import com.xdev.ooms.security.supportticket.enums.SupportTicketStatus;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "support_ticket")
public class SupportTicket extends BaseEntity {

    @Column(nullable = false, length = 160)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SupportTicketStatus status = SupportTicketStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SupportTicketPriority priority = SupportTicketPriority.NORMAL;

    @Column(length = 500)
    private String pageUrl;

    @Column(nullable = false)
    private UUID reporterUserId;

    @Column(nullable = false, length = 80)
    private String reporterUsername;

    @Column(length = 120)
    private String reporterDisplayName;

    @Column(length = 160)
    private String tenantName;

    @Column(columnDefinition = "TEXT")
    private String adminNote;

    private LocalDateTime resolvedAt;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SupportTicketStatus getStatus() {
        return status;
    }

    public void setStatus(SupportTicketStatus status) {
        this.status = status;
    }

    public SupportTicketPriority getPriority() {
        return priority;
    }

    public void setPriority(SupportTicketPriority priority) {
        this.priority = priority;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    public UUID getReporterUserId() {
        return reporterUserId;
    }

    public void setReporterUserId(UUID reporterUserId) {
        this.reporterUserId = reporterUserId;
    }

    public String getReporterUsername() {
        return reporterUsername;
    }

    public void setReporterUsername(String reporterUsername) {
        this.reporterUsername = reporterUsername;
    }

    public String getReporterDisplayName() {
        return reporterDisplayName;
    }

    public void setReporterDisplayName(String reporterDisplayName) {
        this.reporterDisplayName = reporterDisplayName;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getAdminNote() {
        return adminNote;
    }

    public void setAdminNote(String adminNote) {
        this.adminNote = adminNote;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
