package com.xdev.ooms.production.maintenance.dto;

import com.xdev.ooms.production.maintenance.entity.MaintenanceWorkOrder;
import com.xdev.ooms.production.maintenance.enums.MaintenanceAssetType;
import com.xdev.ooms.production.maintenance.enums.MaintenanceType;
import com.xdev.ooms.production.maintenance.enums.MaintenanceWorkOrderStatus;
import com.xdev.ooms.sharedkernel.Enum.PaymentMethod;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.time.LocalDateTime;
import java.util.UUID;

public class MaintenanceWorkOrderDto extends BaseDto<MaintenanceWorkOrder> {

    private MaintenanceAssetType assetType;
    private UUID assetId;
    private String assetName;
    private MaintenanceType maintenanceType;
    private MaintenanceWorkOrderStatus status;
    private LocalDateTime scheduledStart;
    private LocalDateTime scheduledEnd;
    private LocalDateTime completedAt;
    private String technician;
    private String vendor;
    private String description;
    private String partsReplaced;
    private Double partsCost;
    private Double laborCost;
    private Double totalCost;
    private PaymentMethod paymentMethod;
    private String notes;
    private String invoiceReference;

    public MaintenanceAssetType getAssetType() {
        return assetType;
    }

    public void setAssetType(MaintenanceAssetType assetType) {
        this.assetType = assetType;
    }

    public UUID getAssetId() {
        return assetId;
    }

    public void setAssetId(UUID assetId) {
        this.assetId = assetId;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public MaintenanceType getMaintenanceType() {
        return maintenanceType;
    }

    public void setMaintenanceType(MaintenanceType maintenanceType) {
        this.maintenanceType = maintenanceType;
    }

    public MaintenanceWorkOrderStatus getStatus() {
        return status;
    }

    public void setStatus(MaintenanceWorkOrderStatus status) {
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

    public String getTechnician() {
        return technician;
    }

    public void setTechnician(String technician) {
        this.technician = technician;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPartsReplaced() {
        return partsReplaced;
    }

    public void setPartsReplaced(String partsReplaced) {
        this.partsReplaced = partsReplaced;
    }

    public Double getPartsCost() {
        return partsCost;
    }

    public void setPartsCost(Double partsCost) {
        this.partsCost = partsCost;
    }

    public Double getLaborCost() {
        return laborCost;
    }

    public void setLaborCost(Double laborCost) {
        this.laborCost = laborCost;
    }

    public Double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(Double totalCost) {
        this.totalCost = totalCost;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getInvoiceReference() {
        return invoiceReference;
    }

    public void setInvoiceReference(String invoiceReference) {
        this.invoiceReference = invoiceReference;
    }
}
