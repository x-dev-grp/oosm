package com.xdev.ooms.hr.payroll.dto;

import com.xdev.ooms.hr.common.enums.PayrollPeriodStatus;
import com.xdev.ooms.hr.payslip.dto.PayslipDto;
import com.xdev.ooms.hr.payroll.entity.PayrollPeriod;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.time.LocalDate;
import java.util.List;

public class PayrollPeriodDto extends BaseDto<PayrollPeriod> {
    private Integer year;
    private Integer month;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private PayrollPeriodStatus status;
    private List<PayslipDto> payslips;

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

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDate periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDate periodEnd) {
        this.periodEnd = periodEnd;
    }

    public PayrollPeriodStatus getStatus() {
        return status;
    }

    public void setStatus(PayrollPeriodStatus status) {
        this.status = status;
    }

    public List<PayslipDto> getPayslips() {
        return payslips;
    }

    public void setPayslips(List<PayslipDto> payslips) {
        this.payslips = payslips;
    }
}
