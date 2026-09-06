package com.xdev.ooms.hr.organization.controller;

import com.xdev.ooms.hr.organization.dto.EmployeeCategoryDto;
import com.xdev.ooms.hr.organization.entity.EmployeeCategory;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/employee-categories")
public class EmployeeCategoryController extends BaseControllerImpl<EmployeeCategory, EmployeeCategoryDto, EmployeeCategoryDto> {

    public EmployeeCategoryController(
            BaseService<EmployeeCategory, EmployeeCategoryDto, EmployeeCategoryDto> baseService,
            ModelMapper modelMapper
    ) {
        super(baseService, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "EMPLOYEECATEGORY";
    }
}
