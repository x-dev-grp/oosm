package com.xdev.ooms.sharedkernel.communicator.models.shared;

import com.xdev.ooms.sharedkernel.Enum.LabelCategory;
import com.xdev.ooms.sharedkernel.Enum.LabelLanguage;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public class LabelGenerateRequestDto implements Serializable {

    private UUID lotId;
    private UUID traceabilityLotId;
    private UUID packagingId;
    private UUID productId;
    private UUID operatorId;
    private LocalDate packagingDate;
    private LabelLanguage language = LabelLanguage.FR;
    private LabelCategory labelCategory = LabelCategory.UNIT;
    private String qualityGrade;
    private String variety;
    private UUID filtrationOperationId;

    public UUID getLotId() {
        return lotId;
    }

    public void setLotId(UUID lotId) {
        this.lotId = lotId;
    }

    public UUID getTraceabilityLotId() {
        return traceabilityLotId;
    }

    public void setTraceabilityLotId(UUID traceabilityLotId) {
        this.traceabilityLotId = traceabilityLotId;
    }

    public UUID getPackagingId() {
        return packagingId;
    }

    public void setPackagingId(UUID packagingId) {
        this.packagingId = packagingId;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public UUID getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(UUID operatorId) {
        this.operatorId = operatorId;
    }

    public LocalDate getPackagingDate() {
        return packagingDate;
    }

    public void setPackagingDate(LocalDate packagingDate) {
        this.packagingDate = packagingDate;
    }

    public LabelLanguage getLanguage() {
        return language;
    }

    public void setLanguage(LabelLanguage language) {
        this.language = language;
    }

    public LabelCategory getLabelCategory() {
        return labelCategory;
    }

    public void setLabelCategory(LabelCategory labelCategory) {
        this.labelCategory = labelCategory;
    }

    public String getQualityGrade() {
        return qualityGrade;
    }

    public void setQualityGrade(String qualityGrade) {
        this.qualityGrade = qualityGrade;
    }

    public String getVariety() {
        return variety;
    }

    public void setVariety(String variety) {
        this.variety = variety;
    }

    public UUID getFiltrationOperationId() {
        return filtrationOperationId;
    }

    public void setFiltrationOperationId(UUID filtrationOperationId) {
        this.filtrationOperationId = filtrationOperationId;
    }
}
