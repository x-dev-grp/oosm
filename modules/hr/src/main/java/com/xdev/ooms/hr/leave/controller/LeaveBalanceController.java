package com.xdev.ooms.hr.leave.controller;

import com.xdev.ooms.hr.leave.dto.LeaveBalanceDto;
import com.xdev.ooms.hr.leave.entity.LeaveBalance;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/leave-balances")
public class LeaveBalanceController extends BaseControllerImpl<LeaveBalance, LeaveBalanceDto, LeaveBalanceDto> {

    public LeaveBalanceController(
            BaseService<LeaveBalance, LeaveBalanceDto, LeaveBalanceDto> baseService,
            ModelMapper modelMapper
    ) {
        super(baseService, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "LEAVEBALANCE";
    }
}
