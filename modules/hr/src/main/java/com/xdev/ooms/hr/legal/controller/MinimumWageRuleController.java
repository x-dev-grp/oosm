package com.xdev.ooms.hr.legal.controller;

import com.xdev.ooms.hr.legal.dto.MinimumWageRuleDto;
import com.xdev.ooms.hr.legal.entity.MinimumWageRule;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/minimum-wage-rules")
public class MinimumWageRuleController extends BaseControllerImpl<MinimumWageRule, MinimumWageRuleDto, MinimumWageRuleDto> {

    public MinimumWageRuleController(
            BaseService<MinimumWageRule, MinimumWageRuleDto, MinimumWageRuleDto> baseService,
            ModelMapper modelMapper
    ) {
        super(baseService, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "MINIMUMWAGERULE";
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
}
