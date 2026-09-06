package com.xdev.ooms.hr.loan.controller;

import com.xdev.ooms.hr.loan.dto.LoanInstallmentDto;
import com.xdev.ooms.hr.loan.entity.LoanInstallment;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/loan-installments")
public class LoanInstallmentController extends BaseControllerImpl<LoanInstallment, LoanInstallmentDto, LoanInstallmentDto> {

    public LoanInstallmentController(
            BaseService<LoanInstallment, LoanInstallmentDto, LoanInstallmentDto> baseService,
            ModelMapper modelMapper
    ) {
        super(baseService, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "EMPLOYEELOAN";
    }
}
