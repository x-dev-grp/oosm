package com.xdev.ooms.conditioning.expedition.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.xdev.ooms.conditioning.expedition.enums.ExpeditionStatus;
import com.xdev.ooms.conditioning.projet.entity.Projet;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
public class Expedition extends BaseEntity {

    @Column(name = "expedition_number", unique = true, nullable = false, length = 80)
    private String expeditionNumber;

    @ManyToOne
    @JoinColumn(name = "projet_id", nullable = false)
    private Projet projet;

    private UUID clientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ExpeditionStatus status = ExpeditionStatus.DRAFT;

    @Column(length = 255)
    private String destination;

    private LocalDate plannedShipDate;

    private LocalDateTime validatedAt;

    private LocalDateTime shippedAt;

    private LocalDateTime closedAt;

    private LocalDateTime cancelledAt;

    @Column(length = 2000)
    private String notes;
    private Integer totalQuantity;
    private BigDecimal totalVolume;

    /* Transport / shipping fields merged from ShippingInfo. */

    @Column(length = 120)
    private String carrierName;

    @Column(length = 120)
    private String driverName;

    @Column(length = 60)
    private String truckNumber;

    @Column(length = 120)
    private String trackingNumber;

    @Column(length = 30)
    private String incoterm;

    @Lob
    @Column(name = "traceability_snapshot_json", columnDefinition = "TEXT")
    private String traceabilitySnapshotJson;

    private LocalDateTime deliveredAt;

    @OneToMany(mappedBy = "expedition", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpeditionArticle> lines = new ArrayList<>();

    public String getExpeditionNumber() {
        return expeditionNumber;
    }

    public Projet getProjet() {
        return projet;
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

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public BigDecimal getTotalVolume() {
        return totalVolume;
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

    public String getTraceabilitySnapshotJson() {
        return traceabilitySnapshotJson;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public List<ExpeditionArticle> getLines() {
        return lines;
    }

    public void setExpeditionNumber(String expeditionNumber) {
        this.expeditionNumber = expeditionNumber;
    }

    public void setProjet(Projet projet) {
        this.projet = projet;
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

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public void setTotalVolume(BigDecimal totalVolume) {
        this.totalVolume = totalVolume;
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

    public void setTraceabilitySnapshotJson(String traceabilitySnapshotJson) {
        this.traceabilitySnapshotJson = traceabilitySnapshotJson;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public void setLines(List<ExpeditionArticle> lines) {
        this.lines = lines;
    }
}
