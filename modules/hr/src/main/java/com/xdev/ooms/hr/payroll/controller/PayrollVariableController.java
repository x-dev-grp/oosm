package com.xdev.ooms.hr.payroll.controller;

import com.xdev.ooms.hr.payroll.dto.PayrollVariableDto;
import com.xdev.ooms.hr.payroll.entity.PayrollVariable;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/payroll-variables")
public class PayrollVariableController extends BaseControllerImpl<PayrollVariable, PayrollVariableDto, PayrollVariableDto> {

    public PayrollVariableController(
            BaseService<PayrollVariable, PayrollVariableDto, PayrollVariableDto> baseService,
            ModelMapper modelMapper
    ) {
        super(baseService, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "PAYROLLVARIABLE";
    }
}
