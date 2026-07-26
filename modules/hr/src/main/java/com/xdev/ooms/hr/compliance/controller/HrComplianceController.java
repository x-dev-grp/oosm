package com.xdev.ooms.hr.compliance.controller;

import com.xdev.ooms.hr.common.HrPermissionSupport;
import com.xdev.ooms.hr.compliance.dto.ComplianceSummaryDto;
import com.xdev.ooms.hr.compliance.dto.HrComplianceViolationDto;
import com.xdev.ooms.hr.compliance.entity.HrComplianceViolation;
import com.xdev.ooms.hr.compliance.enums.ComplianceViolationStatus;
import com.xdev.ooms.hr.compliance.repository.HrComplianceViolationRepository;
import com.xdev.ooms.hr.compliance.service.ComplianceEngine;
import com.xdev.ooms.sharedkernel.apiDTOs.ApiResponse;
import com.xdev.ooms.sharedkernel.models.Action;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/hr/compliance")
public class HrComplianceController {

    private final ComplianceEngine complianceEngine;
    private final HrComplianceViolationRepository violationRepository;
    private final ModelMapper modelMapper;

    public HrComplianceController(
            ComplianceEngine complianceEngine,
            HrComplianceViolationRepository violationRepository,
            ModelMapper modelMapper
    ) {
        this.complianceEngine = complianceEngine;
        this.violationRepository = violationRepository;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/summary")
    public ResponseEntity<com.xdev.ooms.sharedkernel.communicator.models.shared.ApiResponse<ComplianceSummaryDto>> summary() {
        try {
            HrPermissionSupport.requireAction("COMPLIANCE", Action.READ);
            ComplianceSummaryDto summary = complianceEngine.getSummary();
            return ResponseEntity.ok(new com.xdev.ooms.sharedkernel.communicator.models.shared.ApiResponse<>(
                    true, "Compliance summary", summary));
        } catch (Exception e) {
            return ResponseEntity.ok(new com.xdev.ooms.sharedkernel.communicator.models.shared.ApiResponse<>(
                    false, e.getMessage(), null));
        }
    }

    @GetMapping("/violations")
    public ResponseEntity<ApiResponse<HrComplianceViolation, HrComplianceViolationDto>> violations(
            @RequestParam(required = false) ComplianceViolationStatus status
    ) {
        try {
            HrPermissionSupport.requireAction("COMPLIANCE", Action.READ);
            ComplianceViolationStatus filter = status != null ? status : ComplianceViolationStatus.OPEN;
            List<HrComplianceViolationDto> dtos = violationRepository.findByStatusAndIsDeletedFalse(filter).stream()
                    .map(v -> modelMapper.map(v, HrComplianceViolationDto.class))
                    .toList();
            return ResponseEntity.ok(new ApiResponse<>(true, "Compliance violations", dtos));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PostMapping("/scan")
    public ResponseEntity<com.xdev.ooms.sharedkernel.communicator.models.shared.ApiResponse<List<HrComplianceViolationDto>>> scan() {
        try {
            HrPermissionSupport.requireAction("COMPLIANCE", Action.REPORT);
            List<HrComplianceViolationDto> dtos = complianceEngine.runScan().stream()
                    .map(v -> modelMapper.map(v, HrComplianceViolationDto.class))
                    .toList();
            return ResponseEntity.ok(new com.xdev.ooms.sharedkernel.communicator.models.shared.ApiResponse<>(
                    true, "Compliance scan completed", dtos));
        } catch (Exception e) {
            return ResponseEntity.ok(new com.xdev.ooms.sharedkernel.communicator.models.shared.ApiResponse<>(
                    false, e.getMessage(), null));
        }
    }
}
