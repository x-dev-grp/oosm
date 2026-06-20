package com.xdev.ooms.conditioning.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FiltrationReportDto {
    private String operationId;
    private LocalDateTime operationDate;
    private BigDecimal inputVolume;
    private BigDecimal outputVolume;
    private BigDecimal lossVolume;
    private BigDecimal efficiencyRate;

    public String getOperationId() {
        return operationId;
    }

    public LocalDateTime getOperationDate() {
        return operationDate;
    }

    public BigDecimal getInputVolume() {
        return inputVolume;
    }

    public BigDecimal getOutputVolume() {
        return outputVolume;
    }

    public BigDecimal getLossVolume() {
        return lossVolume;
    }

    public BigDecimal getEfficiencyRate() {
        return efficiencyRate;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public void setOperationDate(LocalDateTime operationDate) {
        this.operationDate = operationDate;
    }

    public void setInputVolume(BigDecimal inputVolume) {
        this.inputVolume = inputVolume;
    }

    public void setOutputVolume(BigDecimal outputVolume) {
        this.outputVolume = outputVolume;
    }

    public void setLossVolume(BigDecimal lossVolume) {
        this.lossVolume = lossVolume;
    }

    public void setEfficiencyRate(BigDecimal efficiencyRate) {
        this.efficiencyRate = efficiencyRate;
    }
}
