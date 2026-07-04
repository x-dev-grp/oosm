package com.xdev.ooms.hr.contract.entity;

import com.xdev.ooms.hr.common.enums.ContractStatus;
import com.xdev.ooms.hr.common.enums.ContractType;
import com.xdev.ooms.hr.employee.entity.Employee;
import com.xdev.ooms.hr.poste.entity.Poste;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "hr_employment_contract")
public class EmploymentContract extends BaseEntity implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "poste_id", nullable = false)
    private Poste poste;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContractType contractType;
    @Column(nullable = false)
    private LocalDate startDate;
    private LocalDate endDate;
    private Double salary;
    @Enumerated(EnumType.STRING)
    private ContractStatus status;

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Poste getPoste() {
        return poste;
    }

    public void setPoste(Poste poste) {
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
