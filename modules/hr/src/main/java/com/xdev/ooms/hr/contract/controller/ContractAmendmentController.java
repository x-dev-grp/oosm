package com.xdev.ooms.hr.contract.controller;

import com.xdev.ooms.hr.contract.dto.ContractAmendmentDto;
import com.xdev.ooms.hr.contract.entity.ContractAmendment;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/contract-amendments")
public class ContractAmendmentController extends BaseControllerImpl<ContractAmendment, ContractAmendmentDto, ContractAmendmentDto> {

    public ContractAmendmentController(
            BaseService<ContractAmendment, ContractAmendmentDto, ContractAmendmentDto> baseService,
            ModelMapper modelMapper
    ) {
        super(baseService, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "CONTRACTAMENDMENT";
    }
}
