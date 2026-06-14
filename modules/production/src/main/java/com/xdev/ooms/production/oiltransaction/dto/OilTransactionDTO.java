package com.xdev.ooms.production.oiltransaction.dto;

import com.xdev.ooms.production.storageunit.entity.StorageUnit;
import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;

import com.xdev.ooms.production.storageunit.dto.StorageUnitDto;
import com.xdev.ooms.production.unifieddelivery.dto.UnifiedDeliveryDTO;


import com.xdev.ooms.production.oiltransaction.entity.OilTransaction;
import com.xdev.ooms.sharedkernel.Enum.Olive_Oil_Type;
import com.xdev.ooms.sharedkernel.Enum.TransactionState;
import com.xdev.ooms.sharedkernel.Enum.TransactionType;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.util.UUID;

public class OilTransactionDTO extends BaseDto<OilTransaction> {
    private StorageUnitDto storageUnitDestination;
    private StorageUnitDto storageUnitSource;
    private String qualityGrade;
    private Double quantityKg;
    private Double unitPrice;
    private Double totalPrice;
    private TransactionType transactionType;
    private TransactionState transactionState ;
    private UnifiedDeliveryDTO reception;

    public Olive_Oil_Type getOilType() {
        return oilType;
    }

    public void setOilType(Olive_Oil_Type oilType) {
        this.oilType = oilType;
    }

    private Olive_Oil_Type oilType;
    private UUID oilSaleId;

    public UUID getOilSaleId() {
        return oilSaleId;
    }

    public void setOilSaleId(UUID oilSaleId) {
        this.oilSaleId = oilSaleId;
    }

    public UnifiedDeliveryDTO getReception() {
        return reception;
    }

    public void setReception(UnifiedDeliveryDTO reception) {
        this.reception = reception;
    }

    public TransactionState getTransactionState() {
        return transactionState;
    }

    public void setTransactionState(TransactionState transactionState) {
        this.transactionState = transactionState;
    }


    public StorageUnitDto getStorageUnitDestination() {
        return storageUnitDestination;
    }

    public void setStorageUnitDestination(StorageUnitDto storageUnitDestination) {
        this.storageUnitDestination = storageUnitDestination;
    }

    public StorageUnitDto getStorageUnitSource() {
        return storageUnitSource;
    }

    public void setStorageUnitSource(StorageUnitDto storageUnitSource) {
        this.storageUnitSource = storageUnitSource;
    }

    public String getQualityGrade() {
        return qualityGrade;
    }

    public void setQualityGrade(String qualityGrade) {
        this.qualityGrade = qualityGrade;
    }

    public Double getQuantityKg() {
        return quantityKg;
    }

    public void setQuantityKg(Double quantityKg) {
        this.quantityKg = quantityKg;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }


    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

}
