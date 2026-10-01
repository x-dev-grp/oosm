package com.xdev.ooms.sharedkernel.utils;

import com.xdev.ooms.sharedkernel.config.TenantContext;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.EntityNotFoundException;

import java.util.Optional;
import java.util.UUID;

/**
 * Rows without a tenant are shared reference data; callers without a tenant context are platform
 * administrators or system jobs.
 */
public final class TenantAccess {

    private TenantAccess() {
    }

    public static boolean isAccessible(BaseEntity entity) {
        if (entity == null) {
            return false;
        }
        UUID tenant = TenantContext.getCurrentTenant();
        return tenant == null || entity.getTenantId() == null || tenant.equals(entity.getTenantId());
    }

    public static <T extends BaseEntity> T require(Optional<T> entity, String label, UUID id) {
        return entity.filter(TenantAccess::isAccessible)
                .orElseThrow(() -> new EntityNotFoundException(label + " introuvable : " + id));
    }
}
