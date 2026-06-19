package com.xdev.ooms.security.permission.catalog;

public record PermissionCatalogSyncResult(
        int permissionsCreated,
        int permissionsExisting,
        int legacyAliasesMerged,
        int roleMirrorGrantsAdded
) {
}
