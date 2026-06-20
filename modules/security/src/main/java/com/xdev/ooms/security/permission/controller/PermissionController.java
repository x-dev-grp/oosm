package com.xdev.ooms.security.permission.controller;


import com.xdev.ooms.security.permission.catalog.PermissionCatalogSyncResult;
import com.xdev.ooms.security.permission.catalog.PermissionCatalogSyncService;
import com.xdev.ooms.security.permission.dto.PermissionDTO;
import com.xdev.ooms.security.permission.entity.Permission;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import com.xdev.ooms.sharedkernel.utils.OSMLogger;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/security/permission")
public class PermissionController extends BaseControllerImpl<Permission, PermissionDTO, PermissionDTO> {

    private final PermissionCatalogSyncService permissionCatalogSyncService;

    public PermissionController(BaseService<Permission, PermissionDTO, PermissionDTO> baseService,
                                ModelMapper modelMapper,
                                PermissionCatalogSyncService permissionCatalogSyncService) {
        super(baseService, modelMapper);
        this.permissionCatalogSyncService = permissionCatalogSyncService;
        
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "PermissionController", "Initializing PermissionController");
        
        try {
            OSMLogger.logMethodExit(this.getClass(), "PermissionController", "PermissionController initialized successfully");
            OSMLogger.logPerformance(this.getClass(), "PermissionController", startTime, System.currentTimeMillis());
            OSMLogger.logSecurityEvent(this.getClass(), "PERMISSION_CONTROLLER_INITIALIZED", 
                "Permission controller initialized successfully");
            
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error initializing PermissionController", e);
            throw e;
        }
    }

    @Override
    protected String getResourceName() {
        return "PERMISSION";
    }

    @PostMapping("/sync-catalog")
    public ResponseEntity<Map<String, Object>> syncCatalog() {
        PermissionCatalogSyncResult result = permissionCatalogSyncService.syncFromCatalog();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Permission catalog synchronized",
                "created", result.permissionsCreated(),
                "existing", result.permissionsExisting(),
                "legacyMerged", result.legacyAliasesMerged(),
                "mirrorGrants", result.roleMirrorGrantsAdded()
        ));
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
}
