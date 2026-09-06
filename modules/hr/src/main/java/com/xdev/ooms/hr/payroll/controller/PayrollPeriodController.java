package com.xdev.ooms.hr.payroll.controller;

import com.xdev.ooms.hr.common.enums.PayrollPeriodStatus;
import com.xdev.ooms.hr.payroll.dto.PayrollPeriodDto;
import com.xdev.ooms.hr.payroll.entity.PayrollPeriod;
import com.xdev.ooms.hr.payroll.service.PayrollPeriodService;
import com.xdev.ooms.sharedkernel.apiDTOs.ApiResponse;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hr/payroll-periods")
public class PayrollPeriodController extends BaseControllerImpl<PayrollPeriod, PayrollPeriodDto, PayrollPeriodDto> {

    private final PayrollPeriodService payrollPeriodService;

    public PayrollPeriodController(
            BaseService<PayrollPeriod, PayrollPeriodDto, PayrollPeriodDto> baseService,
            ModelMapper modelMapper,
            PayrollPeriodService payrollPeriodService
    ) {
        super(baseService, modelMapper);
        this.payrollPeriodService = payrollPeriodService;
    }

    @Override
    protected String getResourceName() {
        return "PAYROLLPERIOD";
    }

    @PostMapping("/{id}/generate-payslips")
    public ResponseEntity<ApiResponse<PayrollPeriod, PayrollPeriodDto>> generatePayslips(@PathVariable UUID id) {
        try {
            PayrollPeriodDto result = payrollPeriodService.generatePayslips(id);
            attachPermittedActions(result);
            return ResponseEntity.ok(new ApiResponse<>(true, "Payslips generated successfully", List.of(result)));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PatchMapping("/{id}/status/{status}")
    public ResponseEntity<ApiResponse<PayrollPeriod, PayrollPeriodDto>> advanceStatus(
            @PathVariable UUID id,
            @PathVariable PayrollPeriodStatus status
    ) {
        try {
            PayrollPeriodDto result = payrollPeriodService.advanceStatus(id, status);
            attachPermittedActions(result);
            return ResponseEntity.ok(new ApiResponse<>(true, "Payroll period status updated", List.of(result)));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
}
