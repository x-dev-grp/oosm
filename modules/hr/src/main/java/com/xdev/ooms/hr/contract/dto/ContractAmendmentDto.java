package com.xdev.ooms.hr.contract.dto;

import com.xdev.ooms.hr.contract.entity.ContractAmendment;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.time.LocalDate;

public class ContractAmendmentDto extends BaseDto<ContractAmendment> {
    private EmploymentContractDto contract;
    private String amendmentNumber;
    private LocalDate effectiveDate;
    private String changeType;
    private String previousValueJson;
    private String newValueJson;
    private String reason;
    private String status;

    public EmploymentContractDto getContract() {
        return contract;
    }

    public void setContract(EmploymentContractDto contract) {
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
