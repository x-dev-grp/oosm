package com.xdev.ooms.hr.employee.controller;

import com.xdev.ooms.hr.employee.dto.EmployeeDto;
import com.xdev.ooms.hr.employee.entity.Employee;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/employees")
public class EmployeeController extends BaseControllerImpl<Employee, EmployeeDto, EmployeeDto> {

    public EmployeeController(BaseService<Employee, EmployeeDto, EmployeeDto> baseService, ModelMapper modelMapper) {
        super(baseService, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "EMPLOYEE";
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
}
