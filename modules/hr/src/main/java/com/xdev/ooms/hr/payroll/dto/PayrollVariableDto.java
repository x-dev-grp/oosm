package com.xdev.ooms.hr.payroll.dto;

import com.xdev.ooms.hr.employee.dto.EmployeeDto;
import com.xdev.ooms.hr.payroll.entity.PayrollVariable;
import com.xdev.ooms.hr.payroll.enums.PayrollVariableStatus;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.math.BigDecimal;

public class PayrollVariableDto extends BaseDto<PayrollVariable> {

    private EmployeeDto employee;
    private Integer year;
    private Integer month;
    private String type;
    private BigDecimal amount;
    private BigDecimal quantity;
    private String reason;
    private PayrollVariableStatus status;

    public EmployeeDto getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeDto employee) {
        this.employee = employee;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public PayrollVariableStatus getStatus() {
        return status;
    }

    public void setStatus(PayrollVariableStatus status) {
        this.status = status;
    }
}
