package com.xdev.ooms.production.equipment.entity;

import com.xdev.ooms.production.equipment.enums.EquipmentServiceMissionStatus;
import com.xdev.ooms.sharedkernel.Enum.PaymentMethod;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "equipment_service_mission")
public class EquipmentServiceMission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipment_id", nullable = false)
    private MillEquipment equipment;

    @Column(nullable = false)
    private String clientName;

    private String clientPhone;
    private String workLocation;
    private String description;
    private String operatorName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentServiceMissionStatus status = EquipmentServiceMissionStatus.PLANNED;

    private LocalDateTime scheduledStart;
    private LocalDateTime scheduledEnd;
    private LocalDateTime completedAt;

    private Double billableHours = 0d;
    private Double hourlyRate = 0d;
    private Double totalAmount = 0d;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private Double paidAmount = 0d;
    private Double unpaidAmount = 0d;

    private String invoiceReference;

    @Column(length = 2000)
    private String notes;

    public MillEquipment getEquipment() {
        return equipment;
    }

    public void setEquipment(MillEquipment equipment) {
        this.equipment = equipment;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientPhone() {
        return clientPhone;
    }

    public void setClientPhone(String clientPhone) {
        this.clientPhone = clientPhone;
    }

    public String getWorkLocation() {
        return workLocation;
    }

    public void setWorkLocation(String workLocation) {
        this.workLocation = workLocation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public EquipmentServiceMissionStatus getStatus() {
        return status;
    }

    public void setStatus(EquipmentServiceMissionStatus status) {
        this.status = status;
    }

    public LocalDateTime getScheduledStart() {
        return scheduledStart;
    }

    public void setScheduledStart(LocalDateTime scheduledStart) {
        this.scheduledStart = scheduledStart;
    }

    public LocalDateTime getScheduledEnd() {
        return scheduledEnd;
    }

    public void setScheduledEnd(LocalDateTime scheduledEnd) {
        this.scheduledEnd = scheduledEnd;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Double getBillableHours() {
        return billableHours;
    }

    public void setBillableHours(Double billableHours) {
        this.billableHours = billableHours == null ? 0d : billableHours;
    }

    public Double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(Double hourlyRate) {
        this.hourlyRate = hourlyRate == null ? 0d : hourlyRate;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount == null ? 0d : totalAmount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(Double paidAmount) {
        this.paidAmount = paidAmount == null ? 0d : paidAmount;
    }

    public Double getUnpaidAmount() {
        return unpaidAmount;
    }

    public void setUnpaidAmount(Double unpaidAmount) {
        this.unpaidAmount = unpaidAmount == null ? 0d : unpaidAmount;
    }

    public String getInvoiceReference() {
        return invoiceReference;
    }

    public void setInvoiceReference(String invoiceReference) {
        this.invoiceReference = invoiceReference;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
