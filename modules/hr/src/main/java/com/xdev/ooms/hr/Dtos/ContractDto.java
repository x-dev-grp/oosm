package com.xdev.ooms.hr.Dtos;

import com.xdev.ooms.sharedkernel.Enum.ContractStatus;
import com.xdev.ooms.sharedkernel.Enum.ContractType;
import com.xdev.ooms.hr.model.Contract;
import com.xdev.ooms.hr.model.Employee;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDate;

public class ContractDto extends BaseDto<Contract> {
    private Employee employee;
    private LocalDate startDate;
    private LocalDate endDate;
    private long salary;
    private PosteDto poste;
    private ContractStatus contractStatus;


    //geter and Seter
    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
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

    @Enumerated(EnumType.STRING)
    private ContractType contractType;

    public ContractType getContractType() {
        return contractType;
    }

    public long getSalary() {
        return salary;
    }

    public void setSalary(long salary) {
        this.salary = salary;
    }

    public void setContractType(ContractType contractType) {
        this.contractType = contractType;
    }

    public PosteDto getPoste() {
        return poste;
    }

    public void setPoste(PosteDto poste) {
        this.poste = poste;
    }

    public ContractStatus getContractStatus() {
        return contractStatus;
    }

    public void setContractStatus(ContractStatus contractStatus) {
        this.contractStatus = contractStatus;
    }
}

