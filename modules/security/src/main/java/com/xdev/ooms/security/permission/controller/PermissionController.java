package com.xdev.ooms.security.permission.controller;


import com.xdev.ooms.security.permission.catalog.PermissionCatalogStatusDTO;
import com.xdev.ooms.security.permission.catalog.PermissionCatalogSyncResult;
import com.xdev.ooms.security.permission.catalog.PermissionCatalogSyncService;
import com.xdev.ooms.security.permission.dto.PermissionDTO;
import com.xdev.ooms.security.permission.entity.Permission;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import com.xdev.ooms.sharedkernel.utils.OOSMLogger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/security/permission")
public class PermissionController extends BaseControllerImpl<Permission, PermissionDTO, PermissionDTO> {

    private static final String OOSM_ADMIN =
            "authentication.tokenAttributes['role'] == 'OOSMADMIN' or hasAnyAuthority('OOSMADMIN', 'ROLE_OOSMADMIN')";

    private final PermissionCatalogSyncService permissionCatalogSyncService;
    private final boolean syncOnStartup;

    public PermissionController(BaseService<Permission, PermissionDTO, PermissionDTO> baseService,
                                ModelMapper modelMapper,
                                PermissionCatalogSyncService permissionCatalogSyncService,
                                @Value("${app.security.permissions.sync-on-startup:false}") boolean syncOnStartup) {
        super(baseService, modelMapper);
        this.permissionCatalogSyncService = permissionCatalogSyncService;
        this.syncOnStartup = syncOnStartup;

        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "PermissionController", "Initializing PermissionController");

        try {
            OOSMLogger.logMethodExit(this.getClass(), "PermissionController", "PermissionController initialized successfully");
            OOSMLogger.logPerformance(this.getClass(), "PermissionController", startTime, System.currentTimeMillis());
            OOSMLogger.logSecurityEvent(this.getClass(), "PERMISSION_CONTROLLER_INITIALIZED",
                "Permission controller initialized successfully");

        } catch (Exception e) {
            OOSMLogger.logException(this.getClass(), "Error initializing PermissionController", e);
            throw e;
        }
    }

    @Override
    protected String getResourceName() {
        return "PERMISSION";
    }

    @GetMapping("/catalog-status")
    @PreAuthorize(OOSM_ADMIN)
    public ResponseEntity<PermissionCatalogStatusDTO> catalogStatus() {
        return ResponseEntity.ok(permissionCatalogSyncService.getCatalogStatus(syncOnStartup));
    }

    @PostMapping("/sync-catalog")
    @PreAuthorize(OOSM_ADMIN)
    public ResponseEntity<Map<String, Object>> syncCatalog() {
        try {
            PermissionCatalogSyncResult result = permissionCatalogSyncService.syncFromCatalog();
            PermissionCatalogStatusDTO status = permissionCatalogSyncService.getCatalogStatus(syncOnStartup);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("success", true);
            body.put("message", "Permission catalog synchronized");
            body.put("created", result.permissionsCreated());
            body.put("existing", result.permissionsExisting());
            body.put("legacyMerged", result.legacyAliasesMerged());
            body.put("mirrorGrants", result.roleMirrorGrantsAdded());
            body.put("status", status);
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            OOSMLogger.logException(this.getClass(), "Manual permission catalog sync failed", e);
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", e.getMessage() != null ? e.getMessage() : "Permission catalog sync failed"
            ));
        }
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
}
