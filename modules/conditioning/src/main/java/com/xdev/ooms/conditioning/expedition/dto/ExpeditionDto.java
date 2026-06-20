package com.xdev.ooms.conditioning.expedition.dto;

import com.xdev.ooms.conditioning.expedition.entity.Expedition;
import com.xdev.ooms.conditioning.expedition.enums.ExpeditionStatus;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ExpeditionDto extends BaseDto<Expedition> {
    private String expeditionNumber;
    private UUID projetId;
    private String projetCode;
    private UUID clientId;

    private ExpeditionStatus status;
    private String destination;
    private LocalDate plannedShipDate;
    private LocalDateTime validatedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime closedAt;
    private LocalDateTime cancelledAt;
    private String notes;

    /* Transport fields merged from shipping. */
    private String carrierName;
    private String driverName;
    private String truckNumber;
    private String trackingNumber;
    private String incoterm;
    private LocalDateTime deliveredAt;
    private String traceabilitySnapshotJson;

    private Integer totalQuantity;
    private BigDecimal totalVolume;

    private String publicCode;
    private String qrImageBase64;

    private List<ExpeditionArticleDto> lines = new ArrayList<>();

    public String getExpeditionNumber() {
        return expeditionNumber;
    }

    public UUID getProjetId() {
        return projetId;
    }

    public String getProjetCode() {
        return projetCode;
    }

    public UUID getClientId() {
        return clientId;
    }

    public ExpeditionStatus getStatus() {
        return status;
    }

    public String getDestination() {
        return destination;
    }

    public LocalDate getPlannedShipDate() {
        return plannedShipDate;
    }

    public LocalDateTime getValidatedAt() {
        return validatedAt;
    }

    public LocalDateTime getShippedAt() {
        return shippedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public String getNotes() {
        return notes;
    }

    public String getCarrierName() {
        return carrierName;
    }

    public String getDriverName() {
        return driverName;
    }

    public String getTruckNumber() {
        return truckNumber;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public String getIncoterm() {
        return incoterm;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public String getTraceabilitySnapshotJson() {
        return traceabilitySnapshotJson;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public BigDecimal getTotalVolume() {
        return totalVolume;
    }

    public String getPublicCode() {
        return publicCode;
    }

    public String getQrImageBase64() {
        return qrImageBase64;
    }

    public List<ExpeditionArticleDto> getLines() {
        return lines;
    }

    public void setExpeditionNumber(String expeditionNumber) {
        this.expeditionNumber = expeditionNumber;
    }

    public void setProjetId(UUID projetId) {
        this.projetId = projetId;
    }

    public void setProjetCode(String projetCode) {
        this.projetCode = projetCode;
    }

    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }

    public void setStatus(ExpeditionStatus status) {
        this.status = status;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setPlannedShipDate(LocalDate plannedShipDate) {
        this.plannedShipDate = plannedShipDate;
    }

    public void setValidatedAt(LocalDateTime validatedAt) {
        this.validatedAt = validatedAt;
    }

    public void setShippedAt(LocalDateTime shippedAt) {
        this.shippedAt = shippedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setCarrierName(String carrierName) {
        this.carrierName = carrierName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public void setTruckNumber(String truckNumber) {
        this.truckNumber = truckNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public void setIncoterm(String incoterm) {
        this.incoterm = incoterm;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public void setTraceabilitySnapshotJson(String traceabilitySnapshotJson) {
        this.traceabilitySnapshotJson = traceabilitySnapshotJson;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public void setTotalVolume(BigDecimal totalVolume) {
        this.totalVolume = totalVolume;
    }

    public void setPublicCode(String publicCode) {
        this.publicCode = publicCode;
    }

    public void setQrImageBase64(String qrImageBase64) {
        this.qrImageBase64 = qrImageBase64;
    }

    public void setLines(List<ExpeditionArticleDto> lines) {
        this.lines = lines;
    }
}
