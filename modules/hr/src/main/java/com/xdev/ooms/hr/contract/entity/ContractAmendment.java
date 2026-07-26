package com.xdev.ooms.hr.contract.entity;

import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "hr_contract_amendment")
public class ContractAmendment extends BaseEntity implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    private EmploymentContract contract;

    private String amendmentNumber;

    private LocalDate effectiveDate;

    /** SALARY, POSITION, DEPARTMENT, SCHEDULE, HOURS, GRADE, CATEGORY, OTHER */
    private String changeType;

    @Column(columnDefinition = "TEXT")
    private String previousValueJson;

    @Column(columnDefinition = "TEXT")
    private String newValueJson;

    @Column(columnDefinition = "TEXT")
    private String reason;

    /** DRAFT, APPLIED, CANCELLED */
    private String status;

    public EmploymentContract getContract() {
        return contract;
    }

    public void setContract(EmploymentContract contract) {
        this.contract = contract;
    }

    public String getAmendmentNumber() {
        return amendmentNumber;
    }

    public void setAmendmentNumber(String amendmentNumber) {
        this.amendmentNumber = amendmentNumber;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public String getPreviousValueJson() {
        return previousValueJson;
    }

    public void setPreviousValueJson(String previousValueJson) {
        this.previousValueJson = previousValueJson;
    }

    public String getNewValueJson() {
        return newValueJson;
    }

    public void setNewValueJson(String newValueJson) {
        this.newValueJson = newValueJson;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
