package com.xdev.ooms.hr.contract.dto;

import com.xdev.ooms.hr.common.enums.CddLegalReason;
import com.xdev.ooms.hr.common.enums.ContractStatus;
import com.xdev.ooms.hr.common.enums.ContractType;
import com.xdev.ooms.hr.common.enums.SalaryType;
import com.xdev.ooms.hr.contract.entity.EmploymentContract;
import com.xdev.ooms.hr.employee.dto.EmployeeDto;
import com.xdev.ooms.hr.poste.dto.PosteDto;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class EmploymentContractDto extends BaseDto<EmploymentContract> {
    private EmployeeDto employee;
    private PosteDto poste;
    private String contractNumber;
    private ContractType contractType;
    private LocalDate startDate;
    private LocalDate endDate;
    /**
     * @deprecated prefer {@link #baseSalary}
     */
    @Deprecated
    private Double salary;
    private BigDecimal baseSalary;
    private CddLegalReason cddLegalReason;
    private String cddReasonDetails;
    private LocalDate probationStart;
    private LocalDate probationEnd;
    private BigDecimal weeklyHours;
    private SalaryType salaryType;
    private String collectiveAgreement;
    private String terminationReason;
    private LocalDate terminationDate;
    private ContractStatus status;

    public EmployeeDto getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeDto employee) {
        this.employee = employee;
    }

    public PosteDto getPoste() {
        return poste;
    }

    public void setPoste(PosteDto poste) {
        this.poste = poste;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    public ContractType getContractType() {
        return contractType;
    }

    public void setContractType(ContractType contractType) {
        this.contractType = contractType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }

    public BigDecimal getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(BigDecimal baseSalary) {
        this.baseSalary = baseSalary;
    }

    public CddLegalReason getCddLegalReason() {
        return cddLegalReason;
    }

    public void setCddLegalReason(CddLegalReason cddLegalReason) {
        this.cddLegalReason = cddLegalReason;
    }

    public String getCddReasonDetails() {
        return cddReasonDetails;
    }

    public void setCddReasonDetails(String cddReasonDetails) {
        this.cddReasonDetails = cddReasonDetails;
    }

    public LocalDate getProbationStart() {
        return probationStart;
    }

    public void setProbationStart(LocalDate probationStart) {
        this.probationStart = probationStart;
    }

    public LocalDate getProbationEnd() {
        return probationEnd;
    }

    public void setProbationEnd(LocalDate probationEnd) {
        this.probationEnd = probationEnd;
    }

    public BigDecimal getWeeklyHours() {
        return weeklyHours;
    }

    public void setWeeklyHours(BigDecimal weeklyHours) {
        this.weeklyHours = weeklyHours;
    }

    public SalaryType getSalaryType() {
        return salaryType;
    }

    public void setSalaryType(SalaryType salaryType) {
        this.salaryType = salaryType;
    }

    public String getCollectiveAgreement() {
        return collectiveAgreement;
    }

    public void setCollectiveAgreement(String collectiveAgreement) {
        this.collectiveAgreement = collectiveAgreement;
    }

    public String getTerminationReason() {
        return terminationReason;
    }

    public void setTerminationReason(String terminationReason) {
        this.terminationReason = terminationReason;
    }

    public LocalDate getTerminationDate() {
        return terminationDate;
    }

    public void setTerminationDate(LocalDate terminationDate) {
        this.terminationDate = terminationDate;
    }

    public ContractStatus getStatus() {
        return status;
    }

    public void setStatus(ContractStatus status) {
        this.status = status;
    }
}
