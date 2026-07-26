package com.xdev.ooms.hr.loan.dto;

import com.xdev.ooms.hr.common.enums.EmployeeLoanStatus;
import com.xdev.ooms.hr.employee.dto.EmployeeDto;
import com.xdev.ooms.hr.loan.entity.EmployeeLoan;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class EmployeeLoanDto extends BaseDto<EmployeeLoan> {
    private EmployeeDto employee;
    private BigDecimal principalAmount;
    private BigDecimal monthlyInstallment;
    private BigDecimal remainingBalance;
    private LocalDate startDate;
    private EmployeeLoanStatus status;
    private List<LoanInstallmentDto> installments;

    public EmployeeDto getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeDto employee) {
        this.employee = employee;
    }

    public BigDecimal getPrincipalAmount() {
        return principalAmount;
    }

    public void setPrincipalAmount(BigDecimal principalAmount) {
        this.principalAmount = principalAmount;
    }

    public BigDecimal getMonthlyInstallment() {
        return monthlyInstallment;
    }

    public void setMonthlyInstallment(BigDecimal monthlyInstallment) {
        this.monthlyInstallment = monthlyInstallment;
    }

    public BigDecimal getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(BigDecimal remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public EmployeeLoanStatus getStatus() {
        return status;
    }

    public void setStatus(EmployeeLoanStatus status) {
        this.status = status;
    }

    public List<LoanInstallmentDto> getInstallments() {
        return installments;
    }

    public void setInstallments(List<LoanInstallmentDto> installments) {
        this.installments = installments;
    }
}
