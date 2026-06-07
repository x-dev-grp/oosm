package com.xdev.ooms.sharedkernel.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Component
public class TenantFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                Jwt jwt = jwtAuth.getToken();
                Map<String, Object> claims = jwt.getClaims();

                if (claims.containsKey("osmUser")) {
                    Map<String, Object> osmUser = (Map<String, Object>) claims.get("osmUser");
                    Object tenantIdObj = osmUser.get("tenantId");

                    if (tenantIdObj != null) {
                        UUID tenantId = UUID.fromString(tenantIdObj.toString());
                        TenantContext.setCurrentTenant(tenantId);
                    }
                }
            }

            // Optional override via X-Tenant-ID header
//            String headerTenantId = request.getHeader("X-Tenant-ID");
//            if (headerTenantId != null) {
//                TenantContext.setCurrentTenant(UUID.fromString(headerTenantId));
//            }

            filterChain.doFilter(request, response);

        } finally {
            TenantContext.clear();
        }
    }
}

