package com.xdev.ooms.security.tenantmodule;

import com.xdev.ooms.sharedkernel.models.OOSMModule;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Maps API paths to OOSM modules. When multiple modules are returned, the tenant must have
 * at least one of them enabled (OR semantics).
 */
public final class TenantModulePathResolver {

    private record PathRule(String prefix, OOSMModule... modules) {
    }

    private static final List<PathRule> RULES = List.of(
            new PathRule("/api/hr/", OOSMModule.HR),
            new PathRule("/api/finance/", OOSMModule.FINANCE),
            new PathRule("/api/inventaire/", OOSMModule.INVENTAIR),
            new PathRule("/api/ordreConditionement/", OOSMModule.CONDITIONING),
            new PathRule("/api/production/deliveries", OOSMModule.RECEPTION, OOSMModule.PRODUCTION),
            new PathRule("/api/production/suppliers_type", OOSMModule.RECEPTION, OOSMModule.PRODUCTION),
            new PathRule("/api/production/millers", OOSMModule.RECEPTION, OOSMModule.PRODUCTION),
            new PathRule("/api/production/transporter", OOSMModule.RECEPTION, OOSMModule.PRODUCTION),
            new PathRule("/api/production/waste", OOSMModule.RECEPTION, OOSMModule.PRODUCTION),
            new PathRule("/api/production/oil_sale", OOSMModule.RECEPTION, OOSMModule.PRODUCTION),
            new PathRule("/api/production/planning", OOSMModule.RECEPTION, OOSMModule.PRODUCTION),
            new PathRule("/api/production/parameter", OOSMModule.RECEPTION, OOSMModule.PRODUCTION),
            new PathRule("/api/production/", OOSMModule.PRODUCTION),
            new PathRule("/api/security/user", OOSMModule.HABILITATION),
            new PathRule("/api/security/role", OOSMModule.HABILITATION),
            new PathRule("/api/security/permission", OOSMModule.HABILITATION)
    );

    private TenantModulePathResolver() {
    }

    public static Optional<Set<OOSMModule>> requiredModules(String requestUri) {
        if (requestUri == null || requestUri.isBlank()) {
            return Optional.empty();
        }
        String path = requestUri.split("\\?")[0];
        if (isExempt(path)) {
            return Optional.empty();
        }
        for (PathRule rule : RULES) {
            if (path.startsWith(rule.prefix())) {
                return Optional.of(EnumSet.copyOf(Arrays.asList(rule.modules())));
            }
        }
        return Optional.empty();
    }

    private static boolean isExempt(String path) {
        return path.startsWith("/api/public/")
                || path.startsWith("/api/admin/")
                || path.startsWith("/actuator/")
                || path.startsWith("/oauth2/")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/api/security/user/auth/")
                || path.startsWith("/api/security/user/me/")
                || path.startsWith("/api/security/company-profile/")
                || path.startsWith("/api/security/admin/")
                || path.startsWith("/api/security/company-profile/save")
                || path.startsWith("/api/notifications/")
                || path.startsWith("/api/documents/");
    }
}
