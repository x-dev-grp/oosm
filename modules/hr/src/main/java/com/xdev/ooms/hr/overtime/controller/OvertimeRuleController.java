package com.xdev.ooms.hr.overtime.controller;

import com.xdev.ooms.hr.overtime.dto.OvertimeRuleDto;
import com.xdev.ooms.hr.overtime.entity.OvertimeRule;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/overtime-rules")
public class OvertimeRuleController extends BaseControllerImpl<OvertimeRule, OvertimeRuleDto, OvertimeRuleDto> {

    public OvertimeRuleController(
            BaseService<OvertimeRule, OvertimeRuleDto, OvertimeRuleDto> baseService,
            ModelMapper modelMapper
    ) {
        super(baseService, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "OVERTIMERULE";
    }
}
