package com.xdev.ooms.production.equipment.dto;

import com.xdev.ooms.production.equipment.entity.EquipmentServiceMission;
import com.xdev.ooms.production.equipment.enums.EquipmentServiceMissionStatus;
import com.xdev.ooms.sharedkernel.Enum.PaymentMethod;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.time.LocalDateTime;

public class EquipmentServiceMissionDto extends BaseDto<EquipmentServiceMission> {

    private MillEquipmentDto equipment;
    private String clientName;
    private String clientPhone;
    private String workLocation;
    private String description;
    private String operatorName;
    private EquipmentServiceMissionStatus status;
    private LocalDateTime scheduledStart;
    private LocalDateTime scheduledEnd;
    private LocalDateTime completedAt;
    private Double billableHours;
    private Double hourlyRate;
    private Double totalAmount;
    private PaymentMethod paymentMethod;
    private Double paidAmount;
    private Double unpaidAmount;
    private String invoiceReference;
    private String notes;

    public MillEquipmentDto getEquipment() {
        return equipment;
    }

    public void setEquipment(MillEquipmentDto equipment) {
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
        this.billableHours = billableHours;
    }

    public Double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(Double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
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
        this.paidAmount = paidAmount;
    }

    public Double getUnpaidAmount() {
        return unpaidAmount;
    }

    public void setUnpaidAmount(Double unpaidAmount) {
        this.unpaidAmount = unpaidAmount;
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
