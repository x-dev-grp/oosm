package com.xdev.ooms.hr.department.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.xdev.ooms.hr.employee.entity.Employee;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.List;

@Entity
public class Department extends BaseEntity implements Serializable {
    private String name;
    private String description;

    //relation avec employee
    @OneToOne
    @JoinColumn(name = "manager_id", nullable = true)
    @JsonIgnoreProperties({"department", "managedDepartment", "contrats", "payrolls", "pointages"})
    private Employee manager;
    @OneToMany(mappedBy = "department",  cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JsonManagedReference
    private List<Employee> employees;



    // Getters et Setters
    public List<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Employee getManager() {
        return manager;
    }

    public void setManager(Employee managerId) {
        this.manager = managerId;
    }

}

