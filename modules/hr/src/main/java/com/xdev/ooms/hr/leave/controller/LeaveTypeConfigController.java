package com.xdev.ooms.hr.leave.controller;

import com.xdev.ooms.hr.leave.dto.LeaveTypeConfigDto;
import com.xdev.ooms.hr.leave.entity.LeaveTypeConfig;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/leave-types")
public class LeaveTypeConfigController extends BaseControllerImpl<LeaveTypeConfig, LeaveTypeConfigDto, LeaveTypeConfigDto> {

    public LeaveTypeConfigController(
            BaseService<LeaveTypeConfig, LeaveTypeConfigDto, LeaveTypeConfigDto> baseService,
            ModelMapper modelMapper
    ) {
        super(baseService, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "LEAVETYPE";
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
}
