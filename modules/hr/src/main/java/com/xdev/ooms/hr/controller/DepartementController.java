package com.xdev.ooms.hr.controller;

import com.xdev.ooms.hr.Dtos.DepartmentDto;
import com.xdev.ooms.hr.model.Department;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/department")
public class DepartementController extends BaseControllerImpl<Department, DepartmentDto, DepartmentDto> {


    public DepartementController(BaseService<Department, DepartmentDto, DepartmentDto> baseService, ModelMapper modelMapper) {
        super(baseService, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "DEPARTEMENT";
    }
}
