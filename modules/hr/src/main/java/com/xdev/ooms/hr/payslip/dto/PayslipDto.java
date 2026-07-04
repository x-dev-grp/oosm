package com.xdev.ooms.hr.payslip.dto;

import com.xdev.ooms.hr.common.enums.PayslipStatus;
import com.xdev.ooms.hr.employee.dto.EmployeeDto;
import com.xdev.ooms.hr.payroll.dto.PayrollPeriodDto;
import com.xdev.ooms.hr.payslip.entity.Payslip;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.time.LocalDate;

public class PayslipDto extends BaseDto<Payslip> {
    private EmployeeDto employee;
    private PayrollPeriodDto payrollPeriod;
    private Double grossSalary;
    private Double baseSalary;
    private Double bonuses;
    private Double cnssEmployee;
    private Double cnssEmployer;
    private Double irpp;
    private Double css;
    private Double netSalary;
    private Boolean paid;
    private LocalDate paymentDate;
    private PayslipStatus status;

    public EmployeeDto getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeDto employee) {
        this.employee = employee;
    }

    public PayrollPeriodDto getPayrollPeriod() {
        return payrollPeriod;
    }

    public void setPayrollPeriod(PayrollPeriodDto payrollPeriod) {
        this.payrollPeriod = payrollPeriod;
    }

    public Double getGrossSalary() {
        return grossSalary;
    }

    public void setGrossSalary(Double grossSalary) {
        this.grossSalary = grossSalary;
    }

    public Double getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(Double baseSalary) {
        this.baseSalary = baseSalary;
    }

    public Double getBonuses() {
        return bonuses;
    }

    public void setBonuses(Double bonuses) {
        this.bonuses = bonuses;
    }

    public Double getCnssEmployee() {
        return cnssEmployee;
    }

    public void setCnssEmployee(Double cnssEmployee) {
        this.cnssEmployee = cnssEmployee;
    }

    public Double getCnssEmployer() {
        return cnssEmployer;
    }

    public void setCnssEmployer(Double cnssEmployer) {
        this.cnssEmployer = cnssEmployer;
    }

    public Double getIrpp() {
        return irpp;
    }

    public void setIrpp(Double irpp) {
        this.irpp = irpp;
    }

    public Double getCss() {
        return css;
    }

    public void setCss(Double css) {
        this.css = css;
    }

    public Double getNetSalary() {
        return netSalary;
    }

    public void setNetSalary(Double netSalary) {
        this.netSalary = netSalary;
    }

    public Boolean getPaid() {
        return paid;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public PayslipStatus getStatus() {
        return status;
    }

    public void setStatus(PayslipStatus status) {
        this.status = status;
    }
}
