package com.xdev.ooms.security.tenantmodule;

import com.xdev.ooms.security.tenantmodule.service.TenantModuleService;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import com.xdev.ooms.sharedkernel.models.OOSMModule;
import com.xdev.ooms.sharedkernel.utils.SecurityUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
@Order(50)
public class TenantModuleApiAccessFilter extends OncePerRequestFilter {

    private final TenantModuleService tenantModuleService;

    public TenantModuleApiAccessFilter(TenantModuleService tenantModuleService) {
        this.tenantModuleService = tenantModuleService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (SecurityUtils.isOosmAdmin()) {
            filterChain.doFilter(request, response);
            return;
        }

        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<Set<OOSMModule>> requiredModules = TenantModulePathResolver.requiredModules(request.getRequestURI());
        if (requiredModules.isPresent()
                && !tenantModuleService.isAnyModuleEnabled(tenantId, requiredModules.get())) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Module not enabled for this tenant\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
