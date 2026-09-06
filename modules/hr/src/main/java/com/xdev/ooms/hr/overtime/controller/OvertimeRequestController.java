package com.xdev.ooms.hr.overtime.controller;

import com.xdev.ooms.hr.overtime.dto.OvertimeRequestDto;
import com.xdev.ooms.hr.overtime.entity.OvertimeRequest;
import com.xdev.ooms.hr.overtime.service.OvertimeRequestService;
import com.xdev.ooms.sharedkernel.apiDTOs.ApiResponse;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hr/overtime-requests")
public class OvertimeRequestController extends BaseControllerImpl<OvertimeRequest, OvertimeRequestDto, OvertimeRequestDto> {

    private final OvertimeRequestService overtimeRequestService;

    public OvertimeRequestController(
            BaseService<OvertimeRequest, OvertimeRequestDto, OvertimeRequestDto> baseService,
            ModelMapper modelMapper,
            OvertimeRequestService overtimeRequestService
    ) {
        super(baseService, modelMapper);
        this.overtimeRequestService = overtimeRequestService;
    }

    @Override
    protected String getResourceName() {
        return "OVERTIMEREQUEST";
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<OvertimeRequest, OvertimeRequestDto>> approve(@PathVariable UUID id) {
        try {
            OvertimeRequestDto result = overtimeRequestService.approve(id);
            attachPermittedActions(result);
            return ResponseEntity.ok(new ApiResponse<>(true, "Overtime request approved", List.of(result)));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<OvertimeRequest, OvertimeRequestDto>> reject(@PathVariable UUID id) {
        try {
            OvertimeRequestDto result = overtimeRequestService.reject(id);
            attachPermittedActions(result);
            return ResponseEntity.ok(new ApiResponse<>(true, "Overtime request rejected", List.of(result)));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
}
