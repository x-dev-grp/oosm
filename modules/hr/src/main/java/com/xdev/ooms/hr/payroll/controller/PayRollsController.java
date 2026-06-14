package com.xdev.ooms.hr.payroll.controller;

import com.xdev.ooms.hr.payroll.dto.PayRollsDto;
import com.xdev.ooms.hr.payroll.entity.PayRolls;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/payrolls")
public class PayRollsController extends BaseControllerImpl<PayRolls, PayRollsDto, PayRollsDto> {


    public PayRollsController(BaseService<PayRolls, PayRollsDto, PayRollsDto> baseService, ModelMapper modelMapper) {
        super(baseService, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "PAYROLLS";
    }
}
