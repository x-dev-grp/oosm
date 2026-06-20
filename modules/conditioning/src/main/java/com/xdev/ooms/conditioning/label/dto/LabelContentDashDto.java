package com.xdev.ooms.conditioning.label.dto;

import com.xdev.ooms.conditioning.label.entity.LabelContent;
import com.xdev.ooms.sharedkernel.Enum.LabelCategory;
import com.xdev.ooms.sharedkernel.Enum.LabelContentStatus;
import com.xdev.ooms.sharedkernel.Enum.LabelLanguage;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LabelContentDashDto extends BaseDto<LabelContent> {
    private LabelContentStatus status;
    private LabelLanguage language;
    private LocalDate packagingDate;
    private LabelCategory labelCategory;
    private String legalDenomination;
    private String netQuantity;
    private String lotNumber;
    private List<String> certifications = new ArrayList<>();
    private LocalDateTime finalizedAt;

    public LabelContentStatus getStatus() {
        return status;
    }

    public LabelLanguage getLanguage() {
        return language;
    }

    public LocalDate getPackagingDate() {
        return packagingDate;
    }

    public LabelCategory getLabelCategory() {
        return labelCategory;
    }

    public String getLegalDenomination() {
        return legalDenomination;
    }

    public String getNetQuantity() {
        return netQuantity;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public List<String> getCertifications() {
        return certifications;
    }

    public LocalDateTime getFinalizedAt() {
        return finalizedAt;
    }

    public void setStatus(LabelContentStatus status) {
        this.status = status;
    }

    public void setLanguage(LabelLanguage language) {
        this.language = language;
    }

    public void setPackagingDate(LocalDate packagingDate) {
        this.packagingDate = packagingDate;
    }

    public void setLabelCategory(LabelCategory labelCategory) {
        this.labelCategory = labelCategory;
    }

    public void setLegalDenomination(String legalDenomination) {
        this.legalDenomination = legalDenomination;
    }

    public void setNetQuantity(String netQuantity) {
        this.netQuantity = netQuantity;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

    public void setCertifications(List<String> certifications) {
        this.certifications = certifications;
    }

    public void setFinalizedAt(LocalDateTime finalizedAt) {
        this.finalizedAt = finalizedAt;
    }
}
