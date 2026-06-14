package com.xdev.ooms.hr.leave.controller;

import com.xdev.ooms.hr.leave.dto.LeaveRequestDto;
import com.xdev.ooms.hr.leave.entity.LeaveRequest;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/leaverequest")
public class LeaveRequestController extends BaseControllerImpl<LeaveRequest, LeaveRequestDto, LeaveRequestDto> {


    public LeaveRequestController(BaseService<LeaveRequest,LeaveRequestDto, LeaveRequestDto> baseService, ModelMapper modelMapper) {
        super(baseService, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "LEAVEREQUEST";
    }
}
