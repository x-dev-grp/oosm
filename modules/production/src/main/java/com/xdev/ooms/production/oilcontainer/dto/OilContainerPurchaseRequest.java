package com.xdev.ooms.production.oilcontainer.dto;

import com.xdev.ooms.sharedkernel.Enum.PaymentMethod;

import java.math.BigDecimal;
import java.util.UUID;

public class OilContainerPurchaseRequest {

    private Integer quantity;
    private BigDecimal unitPrice;
    private UUID materielSupplierId;
    private String vendor;
    private PaymentMethod paymentMethod;
    private String notes;

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public UUID getMaterielSupplierId() {
        return materielSupplierId;
    }

    public void setMaterielSupplierId(UUID materielSupplierId) {
        this.materielSupplierId = materielSupplierId;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
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
}
