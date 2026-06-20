package com.xdev.ooms.production.filtration.dto;



import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO retourné par l'endpoint analytics/filtration de osm-prod.
 * Structure identique à FiltrationReportDto dans osm-cond pour compatibilité Feign.
 */
public class FiltrationAnalyticsDto {
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
