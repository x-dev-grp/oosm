package com.xdev.ooms.hr.contract.controller;

import com.xdev.ooms.hr.contract.dto.EmploymentContractDto;
import com.xdev.ooms.hr.contract.entity.EmploymentContract;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/contracts")
public class EmploymentContractController extends BaseControllerImpl<EmploymentContract, EmploymentContractDto, EmploymentContractDto> {

    public EmploymentContractController(BaseService<EmploymentContract, EmploymentContractDto, EmploymentContractDto> baseService, ModelMapper modelMapper) {
        super(baseService, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "CONTRACT";
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
}
