package com.xdev.ooms.hr.contract.dto;

import com.xdev.ooms.hr.common.enums.ContractStatus;
import com.xdev.ooms.hr.common.enums.ContractType;
import com.xdev.ooms.hr.contract.entity.EmploymentContract;
import com.xdev.ooms.hr.employee.dto.EmployeeDto;
import com.xdev.ooms.hr.poste.dto.PosteDto;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.time.LocalDate;

public class EmploymentContractDto extends BaseDto<EmploymentContract> {
    private EmployeeDto employee;
    private PosteDto poste;
    private ContractType contractType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double salary;
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

    public ContractStatus getStatus() {
        return status;
    }

    public void setStatus(ContractStatus status) {
        this.status = status;
    }
}
