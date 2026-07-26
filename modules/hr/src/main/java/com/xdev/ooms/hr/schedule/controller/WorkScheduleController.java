package com.xdev.ooms.hr.schedule.controller;

import com.xdev.ooms.hr.schedule.dto.WorkScheduleDto;
import com.xdev.ooms.hr.schedule.entity.WorkSchedule;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/work-schedules")
public class WorkScheduleController extends BaseControllerImpl<WorkSchedule, WorkScheduleDto, WorkScheduleDto> {

    public WorkScheduleController(
            BaseService<WorkSchedule, WorkScheduleDto, WorkScheduleDto> baseService,
            ModelMapper modelMapper
    ) {
        super(baseService, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "WORKSCHEDULE";
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
}
