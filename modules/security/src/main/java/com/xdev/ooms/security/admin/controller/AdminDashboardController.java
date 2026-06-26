package com.xdev.ooms.security.admin.controller;

import com.xdev.ooms.security.admin.dto.AdminDashboardStatsDTO;
import com.xdev.ooms.security.admin.service.AdminDashboardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/security/admin/dashboard")
@PreAuthorize("authentication.tokenAttributes['role'] == 'OOSMADMIN' or hasAnyAuthority('OOSMADMIN', 'ROLE_OOSMADMIN')")
public class AdminDashboardController {
    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminDashboardStatsDTO> getStats(Authentication authentication) {
        if (!isOosmAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(adminDashboardService.getStats());
    }

    private boolean isOosmAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Object role = jwtAuth.getToken().getClaims().get("role");
            if (role != null && "OOSMADMIN".equalsIgnoreCase(role.toString())) {
                return true;
            }
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority ->
                        "OOSMADMIN".equalsIgnoreCase(authority)
                                || "ROLE_OOSMADMIN".equalsIgnoreCase(authority)
                );
    }
}
