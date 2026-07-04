package com.xdev.ooms.hr.payslip.entity;

import com.xdev.ooms.hr.common.enums.PayslipStatus;
import com.xdev.ooms.hr.employee.entity.Employee;
import com.xdev.ooms.hr.payroll.entity.PayrollPeriod;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "hr_payslip")
public class Payslip extends BaseEntity implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payroll_period_id", nullable = false)
    private PayrollPeriod payrollPeriod;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
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
    @Enumerated(EnumType.STRING)
    private PayslipStatus status;

    public PayrollPeriod getPayrollPeriod() {
        return payrollPeriod;
    }

    public void setPayrollPeriod(PayrollPeriod payrollPeriod) {
        this.payrollPeriod = payrollPeriod;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
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
