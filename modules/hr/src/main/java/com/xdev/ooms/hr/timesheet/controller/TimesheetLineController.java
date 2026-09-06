package com.xdev.ooms.hr.timesheet.controller;

import com.xdev.ooms.hr.timesheet.dto.TimesheetLineDto;
import com.xdev.ooms.hr.timesheet.entity.TimesheetLine;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/timesheet-lines")
public class TimesheetLineController extends BaseControllerImpl<TimesheetLine, TimesheetLineDto, TimesheetLineDto> {

    public TimesheetLineController(
            BaseService<TimesheetLine, TimesheetLineDto, TimesheetLineDto> baseService,
            ModelMapper modelMapper
    ) {
        super(baseService, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "TIMESHEET";
    }
}
