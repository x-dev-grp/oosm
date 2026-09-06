package com.xdev.ooms.hr.organization.controller;

import com.xdev.ooms.hr.organization.dto.DepartmentDto;
import com.xdev.ooms.hr.organization.entity.Department;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/departments")
public class DepartmentController extends BaseControllerImpl<Department, DepartmentDto, DepartmentDto> {

    public DepartmentController(BaseService<Department, DepartmentDto, DepartmentDto> baseService, ModelMapper modelMapper) {
        super(baseService, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "DEPARTMENT";
    }
}
