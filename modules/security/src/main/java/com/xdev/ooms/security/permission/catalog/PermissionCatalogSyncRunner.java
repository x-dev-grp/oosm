package com.xdev.ooms.security.permission.catalog;

import com.xdev.ooms.security.permission.repository.PermissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(20)
public class PermissionCatalogSyncRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PermissionCatalogSyncRunner.class);

    private final PermissionCatalogSyncService syncService;
    private final PermissionRepository permissionRepository;
    private final boolean syncOnStartup;

    public PermissionCatalogSyncRunner(PermissionCatalogSyncService syncService,
                                       PermissionRepository permissionRepository,
                                       @Value("${app.security.permissions.sync-on-startup:false}") boolean syncOnStartup) {
        this.syncService = syncService;
        this.permissionRepository = permissionRepository;
        this.syncOnStartup = syncOnStartup;
    }

    @Override
    public void run(String... args) {
        long activeCount = permissionRepository.countByIsDeletedFalse();
        if (!syncOnStartup && activeCount > 0) {
            log.debug("Permission catalog sync skipped ({} active permissions)", activeCount);
            return;
        }

        if (activeCount == 0) {
            log.warn("Permission table is empty — seeding from permissions-spec.json");
        } else {
            log.info("Syncing permission catalog from permissions-spec.json (sync-on-startup=true)");
        }

        PermissionCatalogSyncResult result = syncService.syncFromCatalog();
        log.info(
                "Permission catalog sync complete: created={}, existing={}, legacyMerged={}, mirrorGrants={}",
                result.permissionsCreated(),
                result.permissionsExisting(),
                result.legacyAliasesMerged(),
                result.roleMirrorGrantsAdded()
        );
    }
}
