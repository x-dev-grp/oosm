package com.xdev.ooms.hr.legal.controller;

import com.xdev.ooms.hr.legal.dto.SalaryComponentDto;
import com.xdev.ooms.hr.legal.entity.SalaryComponent;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/salary-components")
public class SalaryComponentController extends BaseControllerImpl<SalaryComponent, SalaryComponentDto, SalaryComponentDto> {

    public SalaryComponentController(
            BaseService<SalaryComponent, SalaryComponentDto, SalaryComponentDto> baseService,
            ModelMapper modelMapper
    ) {
        super(baseService, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "SALARYCOMPONENT";
    }
}
