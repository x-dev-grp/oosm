package com.xdev.ooms.production.dayimport.service;

import com.xdev.ooms.production.dayimport.model.DayImportWorkbook;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.*;

/** Import authorization uses full authorities, never entity-name substring matching. */
public final class DayImportAccess {
    private static final ThreadLocal<Boolean> AUTOMATION = new ThreadLocal<>();
    private DayImportAccess() {}

    public static UUID tenant() {
        UUID id = TenantContext.getCurrentTenant();
        if (id == null) throw new AccessDeniedException("Tenant context is required");
        return id;
    }

    public static void requireImport() { require("RECEPTION:UNIFIEDDELIVERY:CREATE"); }

    public static void requireAdmin() {
        tenant();
        if (!Boolean.TRUE.equals(AUTOMATION.get()) && !isAdmin()) throw new AccessDeniedException("Tenant administration permission is required");
    }

    public static void requireWorkbook(DayImportWorkbook wb) {
        requireImport();
        if (!wb.getSuppliers().isEmpty()) require("RECEPTION:SUPPLIER:CREATE");
        if (!wb.getContainers().isEmpty()) require("PRODUCTION:OILCONTAINER:CREATE");
        if (!wb.getQcRules().isEmpty()) require("PRODUCTION:QUALITYCONTROLRULE:CREATE");
        if (!wb.getQcResults().isEmpty()) require("PRODUCTION:QUALITYCONTROLRESULT:CREATE");
        if (!wb.getPayments().isEmpty()) {
            require("RECEPTION:UNIFIEDDELIVERY:PAY");
            require("FINANCE:FINANCIALTRANSACTION:CREATE");
        }
        if (!wb.getExpenses().isEmpty()) require("FINANCE:EXPENSE:CREATE");
        if (!wb.getOilSales().isEmpty()) {
            require("FINANCE:OILSALE:CREATE");
            require("PRODUCTION:OILTRANSACTION:APPROVE");
        }
        if (wb.getReceptions().stream().anyMatch(r -> "OIL".equalsIgnoreCase(r.deliveryType)))
            require("RECEPTION:UNIFIEDDELIVERY:OIL_RECEPTION");
    }

    static void automated(Runnable action) {
        // Only the scheduler invokes this after checking the tenant's explicit automation setting.
        try { AUTOMATION.set(true); action.run(); } finally { AUTOMATION.remove(); }
    }

    private static boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;
        if (auth instanceof JwtAuthenticationToken jwt) {
            String role = jwt.getToken().getClaimAsString("role");
            if (role != null && Set.of("ADMIN", "OOSMADMIN").contains(role.toUpperCase(Locale.ROOT))) return true;
        }
        return auth.getAuthorities().stream().anyMatch(a -> Set.of("ADMIN", "OOSMADMIN", "ROLE_ADMIN", "ROLE_OOSMADMIN").contains(a.getAuthority()));
    }

    private static void require(String authority) {
        tenant();
        if (Boolean.TRUE.equals(AUTOMATION.get()) || isAdmin()) return;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Set<String> granted = new HashSet<>();
        if (auth != null && auth.isAuthenticated()) {
            auth.getAuthorities().forEach(a -> granted.add(a.getAuthority().toUpperCase(Locale.ROOT)));
            if (auth instanceof JwtAuthenticationToken jwt) {
                Object claim = jwt.getToken().getClaim("authorities");
                if (claim instanceof Collection<?> values) values.forEach(v -> granted.add(String.valueOf(v).toUpperCase(Locale.ROOT)));
            }
        }
        if (!granted.contains(authority)) throw new AccessDeniedException("Missing permission: " + authority);
    }
}
