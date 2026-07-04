package com.xdev.ooms.security.permission.catalog;

public record PermissionCatalogStatusDTO(
        int specVersion,
        int specEntityCount,
        int specProfileCount,
        int expectedPermissionCount,
        long dbActivePermissionCount,
        boolean syncOnStartup,
        String specSource
) {
}
