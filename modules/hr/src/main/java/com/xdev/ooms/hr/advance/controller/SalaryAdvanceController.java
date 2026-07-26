package com.xdev.ooms.hr.advance.controller;

import com.xdev.ooms.hr.advance.dto.SalaryAdvanceDto;
import com.xdev.ooms.hr.advance.entity.SalaryAdvance;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/salary-advances")
public class SalaryAdvanceController extends BaseControllerImpl<SalaryAdvance, SalaryAdvanceDto, SalaryAdvanceDto> {

    public SalaryAdvanceController(
            BaseService<SalaryAdvance, SalaryAdvanceDto, SalaryAdvanceDto> baseService,
            ModelMapper modelMapper
    ) {
        super(baseService, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "SALARYADVANCE";
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
}
