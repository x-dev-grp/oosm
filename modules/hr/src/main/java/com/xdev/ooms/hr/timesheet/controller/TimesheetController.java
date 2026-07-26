package com.xdev.ooms.hr.timesheet.controller;

import com.xdev.ooms.hr.timesheet.dto.TimesheetDto;
import com.xdev.ooms.hr.timesheet.entity.Timesheet;
import com.xdev.ooms.hr.timesheet.service.TimesheetService;
import com.xdev.ooms.sharedkernel.apiDTOs.ApiResponse;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hr/timesheets")
public class TimesheetController extends BaseControllerImpl<Timesheet, TimesheetDto, TimesheetDto> {

    private final TimesheetService timesheetService;

    public TimesheetController(
            BaseService<Timesheet, TimesheetDto, TimesheetDto> baseService,
            ModelMapper modelMapper,
            TimesheetService timesheetService
    ) {
        super(baseService, modelMapper);
        this.timesheetService = timesheetService;
    }

    @Override
    protected String getResourceName() {
        return "TIMESHEET";
    }

    @PostMapping("/{id}/validate")
    public ResponseEntity<ApiResponse<Timesheet, TimesheetDto>> validate(@PathVariable UUID id) {
        try {
            TimesheetDto result = timesheetService.validate(id);
            attachPermittedActions(result);
            return ResponseEntity.ok(new ApiResponse<>(true, "Timesheet validated", List.of(result)));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
}
