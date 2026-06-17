package com.xdev.ooms.finance.financialtransaction.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class SupplierFinancialSummaryDto {

    private UUID supplierId;
    private long transactionCount;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private BigDecimal totalPaidAmount = BigDecimal.ZERO;
    private BigDecimal totalUnpaidAmount = BigDecimal.ZERO;
    private BigDecimal inboundAmount = BigDecimal.ZERO;
    private BigDecimal outboundAmount = BigDecimal.ZERO;
    private long inboundCount;
    private long outboundCount;

    public UUID getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(UUID supplierId) {
        this.supplierId = supplierId;
    }

    public long getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(long transactionCount) {
        this.transactionCount = transactionCount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getTotalPaidAmount() {
        return totalPaidAmount;
    }

    public void setTotalPaidAmount(BigDecimal totalPaidAmount) {
        this.totalPaidAmount = totalPaidAmount;
    }

    public BigDecimal getTotalUnpaidAmount() {
        return totalUnpaidAmount;
    }

    public void setTotalUnpaidAmount(BigDecimal totalUnpaidAmount) {
        this.totalUnpaidAmount = totalUnpaidAmount;
    }

    public BigDecimal getInboundAmount() {
        return inboundAmount;
    }

    public void setInboundAmount(BigDecimal inboundAmount) {
        this.inboundAmount = inboundAmount;
    }

    public BigDecimal getOutboundAmount() {
        return outboundAmount;
    }

    public void setOutboundAmount(BigDecimal outboundAmount) {
        this.outboundAmount = outboundAmount;
    }

    public long getInboundCount() {
        return inboundCount;
    }

    public void setInboundCount(long inboundCount) {
        this.inboundCount = inboundCount;
    }

    public long getOutboundCount() {
        return outboundCount;
    }

    public void setOutboundCount(long outboundCount) {
        this.outboundCount = outboundCount;
    }
}
