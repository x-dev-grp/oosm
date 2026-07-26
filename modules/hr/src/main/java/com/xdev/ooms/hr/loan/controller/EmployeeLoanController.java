package com.xdev.ooms.hr.loan.controller;

import com.xdev.ooms.hr.loan.dto.EmployeeLoanDto;
import com.xdev.ooms.hr.loan.entity.EmployeeLoan;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/employee-loans")
public class EmployeeLoanController extends BaseControllerImpl<EmployeeLoan, EmployeeLoanDto, EmployeeLoanDto> {

    public EmployeeLoanController(
            BaseService<EmployeeLoan, EmployeeLoanDto, EmployeeLoanDto> baseService,
            ModelMapper modelMapper
    ) {
        super(baseService, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "EMPLOYEELOAN";
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
}
