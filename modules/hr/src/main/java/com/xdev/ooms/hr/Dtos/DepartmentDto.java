package com.xdev.ooms.hr.Dtos;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.xdev.ooms.hr.model.Department;
import com.xdev.ooms.hr.model.Employee;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.util.List;


public class DepartmentDto extends BaseDto<Department> {

    private String name;
    private String description;


    @JsonIgnoreProperties({"department", "contrats", "payrolls", "pointages"})
    private List<EmployeeDto> employees;

    @JsonIgnoreProperties({"department", "contrats", "payrolls", "pointages"})
    private EmployeeDto manager;



    //geter and seter
    public EmployeeDto getManager() {
        return manager;
    }

    public void setManager(EmployeeDto manager) {
        this.manager = manager;
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


    public List<EmployeeDto> getEmployees() {
        return employees;
    }

    public void setEmployees(List<EmployeeDto> employees) {
        this.employees = employees;
    }


}