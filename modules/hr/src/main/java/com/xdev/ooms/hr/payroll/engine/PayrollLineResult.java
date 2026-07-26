package com.xdev.ooms.hr.payroll.engine;

import java.math.BigDecimal;

public class PayrollLineResult {

    private String componentCode;
    private String label;
    private PayrollLineType type;
    private BigDecimal amount;
    private boolean taxable;
    private boolean cnssApplicable;

    public PayrollLineResult() {
    }

    public PayrollLineResult(
            String componentCode,
            String label,
            PayrollLineType type,
            BigDecimal amount,
            boolean taxable,
            boolean cnssApplicable
    ) {
        this.componentCode = componentCode;
        this.label = label;
        this.type = type;
        this.amount = amount;
        this.taxable = taxable;
        this.cnssApplicable = cnssApplicable;
    }

    public String getComponentCode() {
        return componentCode;
    }

    public void setComponentCode(String componentCode) {
        this.componentCode = componentCode;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public PayrollLineType getType() {
        return type;
    }

    public void setType(PayrollLineType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public boolean isTaxable() {
        return taxable;
    }

    public void setTaxable(boolean taxable) {
        this.taxable = taxable;
    }

    public boolean isCnssApplicable() {
        return cnssApplicable;
    }

    public void setCnssApplicable(boolean cnssApplicable) {
        this.cnssApplicable = cnssApplicable;
    }
}
