package com.xdev.ooms.security.admin.controller;

import com.xdev.ooms.security.admin.service.AdminUserService;
import com.xdev.ooms.security.user.dto.OSMUserOUTDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/security/admin/users")
@PreAuthorize("authentication.tokenAttributes['role'] == 'OSMADMIN' or hasAnyAuthority('OSMADMIN', 'ROLE_OSMADMIN')")
public class AdminUserController {
    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @PostMapping("/osm-admin")
    public ResponseEntity<?> createOsmAdminUser(@RequestBody OSMUserOUTDTO userDTO, Authentication authentication) {
        if (!isOsmAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            OSMUserOUTDTO user = adminUserService.createOsmAdminUser(userDTO);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to create administrator");
        }
    }

    @PostMapping("/{userId}/issue-temporary-password")
    public ResponseEntity<?> issueTemporaryPassword(@PathVariable UUID userId, Authentication authentication) {
        if (!isOsmAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            OSMUserOUTDTO user = adminUserService.issueTemporaryPassword(userId);
            return ResponseEntity.ok(user);
        } catch (javax.security.auth.login.AccountLockedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Account is locked");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to issue temporary password");
        }
    }

    private boolean isOsmAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Object role = jwtAuth.getToken().getClaims().get("role");
            if (role != null && "OSMADMIN".equalsIgnoreCase(role.toString())) {
                return true;
            }
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority ->
                        "OSMADMIN".equalsIgnoreCase(authority)
                                || "ROLE_OSMADMIN".equalsIgnoreCase(authority)
                );
    }
}
