package com.xdev.ooms.hr.contract.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.xdev.ooms.hr.employee.entity.Employee;
import com.xdev.ooms.hr.poste.entity.Poste;
import com.xdev.ooms.sharedkernel.Enum.ContractStatus;
import com.xdev.ooms.sharedkernel.Enum.ContractType;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
public class Contract extends BaseEntity implements Serializable {
    private LocalDate startDate;
    private LocalDate endDate;
    private long salary;
    @ManyToOne
    @JoinColumn(name = "poste_id")
    private Poste poste;

    @Enumerated(EnumType.STRING)
    private ContractType contractType;

    @Enumerated(EnumType.STRING)
    private ContractStatus contractStatus;


    //relation avec employee
    @ManyToOne
    @JoinColumn(name = "employee_id")
    @JsonBackReference
    private Employee employee;


    // Getters et Setters

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

    public long getSalary() {
        return salary;
    }

    public void setSalary(long salary) {
        this.salary = salary;
    }

    public Poste getPoste() {
        return poste;
    }

    public void setPoste(Poste position) {
        this.poste = position;
    }

    public ContractType getContractType() {
        return contractType;
    }

    public void setContractType(ContractType contractType) {
        this.contractType = contractType;
    }

    public ContractStatus getContractStatus() {
        return contractStatus;
    }

    public void setContractStatus(ContractStatus contractStatus) {
        this.contractStatus = contractStatus;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}

