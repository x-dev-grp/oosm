package com.xdev.ooms.production.genealogy.dto;



import java.util.Map;
import java.util.UUID;

public class IntakeStepDto {
    /** OLIVE_RECEPTION | OIL_RECEPTION | STORAGE_INTAKE */
    private String type;
    private UUID deliveryId;
    private UUID transactionId;
    private String deliveryNumber;
    private String lotNumber;
    private String lotOliveNumber;
    private String deliveryType;
    private String supplierName;
    private String deliveryDate;
    private Double quantityKg;
    private UUID storageUnitId;
    private String storageUnitName;
    private Map<String, String> qualityControls;
    private Map<String, Object> extra;

    public String getType() {
        return type;
    }

    public UUID getDeliveryId() {
        return deliveryId;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public String getDeliveryNumber() {
        return deliveryNumber;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public String getLotOliveNumber() {
        return lotOliveNumber;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public Double getQuantityKg() {
        return quantityKg;
    }

    public UUID getStorageUnitId() {
        return storageUnitId;
    }

    public String getStorageUnitName() {
        return storageUnitName;
    }

    public Map<String, String> getQualityControls() {
        return qualityControls;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDeliveryId(UUID deliveryId) {
        this.deliveryId = deliveryId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public void setDeliveryNumber(String deliveryNumber) {
        this.deliveryNumber = deliveryNumber;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

    public void setLotOliveNumber(String lotOliveNumber) {
        this.lotOliveNumber = lotOliveNumber;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public void setDeliveryDate(String deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public void setQuantityKg(Double quantityKg) {
        this.quantityKg = quantityKg;
    }

    public void setStorageUnitId(UUID storageUnitId) {
        this.storageUnitId = storageUnitId;
    }

    public void setStorageUnitName(String storageUnitName) {
        this.storageUnitName = storageUnitName;
    }

    public void setQualityControls(Map<String, String> qualityControls) {
        this.qualityControls = qualityControls;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }
}
