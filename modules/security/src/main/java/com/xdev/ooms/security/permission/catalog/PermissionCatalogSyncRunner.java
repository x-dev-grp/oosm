package com.xdev.ooms.security.permission.catalog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(20)
@ConditionalOnProperty(name = "app.security.permissions.sync-on-startup", havingValue = "true")
public class PermissionCatalogSyncRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PermissionCatalogSyncRunner.class);

    private final PermissionCatalogSyncService syncService;

    public PermissionCatalogSyncRunner(PermissionCatalogSyncService syncService) {
        this.syncService = syncService;
    }

    @Override
    public void run(String... args) {
        log.info("Syncing permission catalog from permissions-spec.json...");
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
