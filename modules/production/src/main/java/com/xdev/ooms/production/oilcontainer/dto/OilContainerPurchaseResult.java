package com.xdev.ooms.production.oilcontainer.dto;

import java.math.BigDecimal;

public class OilContainerPurchaseResult {

    private OilContainerDTO container;
    private Integer purchasedQuantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private String invoiceReference;
    private Integer previousStockQuantity;
    private Integer newStockQuantity;

    public OilContainerDTO getContainer() {
        return container;
    }

    public void setContainer(OilContainerDTO container) {
        this.container = container;
    }

    public Integer getPurchasedQuantity() {
        return purchasedQuantity;
    }

    public void setPurchasedQuantity(Integer purchasedQuantity) {
        this.purchasedQuantity = purchasedQuantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getInvoiceReference() {
        return invoiceReference;
    }

    public void setInvoiceReference(String invoiceReference) {
        this.invoiceReference = invoiceReference;
    }

    public Integer getPreviousStockQuantity() {
        return previousStockQuantity;
    }

    public void setPreviousStockQuantity(Integer previousStockQuantity) {
        this.previousStockQuantity = previousStockQuantity;
    }

    public Integer getNewStockQuantity() {
        return newStockQuantity;
    }

    public void setNewStockQuantity(Integer newStockQuantity) {
        this.newStockQuantity = newStockQuantity;
    }
}
