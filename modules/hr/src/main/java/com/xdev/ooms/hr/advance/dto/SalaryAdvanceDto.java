package com.xdev.ooms.hr.advance.dto;

import com.xdev.ooms.hr.advance.entity.SalaryAdvance;
import com.xdev.ooms.hr.common.enums.SalaryAdvanceStatus;
import com.xdev.ooms.hr.employee.dto.EmployeeDto;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SalaryAdvanceDto extends BaseDto<SalaryAdvance> {
    private EmployeeDto employee;
    private BigDecimal amount;
    private LocalDate requestDate;
    private LocalDate paymentDate;
    private Integer deductionPeriodYear;
    private Integer deductionPeriodMonth;
    private BigDecimal remainingAmount;
    private String reason;
    private SalaryAdvanceStatus status;

    public EmployeeDto getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeDto employee) {
        this.employee = employee;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Integer getDeductionPeriodYear() {
        return deductionPeriodYear;
    }

    public void setDeductionPeriodYear(Integer deductionPeriodYear) {
        this.deductionPeriodYear = deductionPeriodYear;
    }

    public Integer getDeductionPeriodMonth() {
        return deductionPeriodMonth;
    }

    public void setDeductionPeriodMonth(Integer deductionPeriodMonth) {
        this.deductionPeriodMonth = deductionPeriodMonth;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public SalaryAdvanceStatus getStatus() {
        return status;
    }

    public void setStatus(SalaryAdvanceStatus status) {
        this.status = status;
    }
}
