package com.xdev.ooms.hr.leave.controller;

import com.xdev.ooms.hr.leave.dto.LeaveRequestDto;
import com.xdev.ooms.hr.leave.entity.LeaveRequest;
import com.xdev.ooms.hr.leave.service.LeaveRequestService;
import com.xdev.ooms.sharedkernel.apiDTOs.ApiResponse;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hr/leave-requests")
public class LeaveRequestController extends BaseControllerImpl<LeaveRequest, LeaveRequestDto, LeaveRequestDto> {

    private final LeaveRequestService leaveRequestService;

    public LeaveRequestController(
            BaseService<LeaveRequest, LeaveRequestDto, LeaveRequestDto> baseService,
            ModelMapper modelMapper,
            LeaveRequestService leaveRequestService
    ) {
        super(baseService, modelMapper);
        this.leaveRequestService = leaveRequestService;
    }

    @Override
    protected String getResourceName() {
        return "LEAVEREQUEST";
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<LeaveRequest, LeaveRequestDto>> approve(@PathVariable UUID id) {
        try {
            LeaveRequestDto result = leaveRequestService.approve(id);
            attachPermittedActions(result);
            return ResponseEntity.ok(new ApiResponse<>(true, "Leave request approved", List.of(result)));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<LeaveRequest, LeaveRequestDto>> reject(@PathVariable UUID id) {
        try {
            LeaveRequestDto result = leaveRequestService.reject(id);
            attachPermittedActions(result);
            return ResponseEntity.ok(new ApiResponse<>(true, "Leave request rejected", List.of(result)));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
}
