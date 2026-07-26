package com.xdev.ooms.hr.compliance.controller;

import com.xdev.ooms.hr.compliance.dto.HrComplianceViolationDto;
import com.xdev.ooms.hr.compliance.entity.HrComplianceViolation;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/compliance-violations")
public class HrComplianceViolationController
        extends BaseControllerImpl<HrComplianceViolation, HrComplianceViolationDto, HrComplianceViolationDto> {

    public HrComplianceViolationController(
            BaseService<HrComplianceViolation, HrComplianceViolationDto, HrComplianceViolationDto> baseService,
            ModelMapper modelMapper
    ) {
        super(baseService, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "COMPLIANCE";
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
}
