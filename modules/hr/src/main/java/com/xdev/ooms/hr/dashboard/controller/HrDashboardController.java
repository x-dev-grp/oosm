package com.xdev.ooms.hr.dashboard.controller;

import com.xdev.ooms.hr.common.HrPermissionSupport;
import com.xdev.ooms.hr.dashboard.dto.HrDashboardStatsDto;
import com.xdev.ooms.hr.dashboard.service.HrDashboardService;
import com.xdev.ooms.sharedkernel.communicator.models.shared.ApiResponse;
import com.xdev.ooms.sharedkernel.models.Action;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/dashboard")
public class HrDashboardController {

    private final HrDashboardService hrDashboardService;

    public HrDashboardController(HrDashboardService hrDashboardService) {
        this.hrDashboardService = hrDashboardService;
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<HrDashboardStatsDto>> stats() {
        try {
            HrPermissionSupport.requireAction("EMPLOYEE", Action.READ);
            HrDashboardStatsDto stats = hrDashboardService.getStats();
            return ResponseEntity.ok(new ApiResponse<>(true, "HR dashboard stats", stats));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
}
