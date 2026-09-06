package com.xdev.ooms.sharedkernel.utils;

import com.xdev.ooms.sharedkernel.models.Action;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Generic permission checks against JWT authorities shaped as {@code MODULE:ENTITY:ACTION}.
 */
public final class PermissionSupport {

    private static final Set<String> ADMIN_ROLES = Set.of("ADMIN", "OOSMADMIN");

    private PermissionSupport() {
    }

    public static void requireAction(String resource, Action action) {
        if (action == null) {
            throw new AccessDeniedException("Action is required");
        }
        if (hasAction(resource, action)) {
            return;
        }
        throw new AccessDeniedException("Missing permission: " + resource + ":" + action.name());
    }

    public static boolean hasAction(String resource, Action action) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || action == null || resource == null) {
            return false;
        }
        String role = extractRole(authentication);
        if (ADMIN_ROLES.contains(role)) {
            return true;
        }
        return extractResourcePermissions(authentication, resource).contains(action.name());
    }

    private static String extractRole(Authentication authentication) {
        Map<String, Object> claims = extractClaims(authentication);
        if (claims != null && claims.get("role") != null) {
            return claims.get("role").toString().toUpperCase().replace("ROLE_", "");
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .map(String::toUpperCase)
                .map(authority -> authority.replace("ROLE_", ""))
                .filter(ADMIN_ROLES::contains)
                .findFirst()
                .orElse("");
    }

    @SuppressWarnings("unchecked")
    private static Set<String> extractResourcePermissions(Authentication authentication, String resource) {
        List<String> rawAuthorities = Collections.emptyList();
        Map<String, Object> claims = extractClaims(authentication);
        if (claims != null) {
            Object auths = claims.get("authorities");
            if (auths instanceof Collection<?> collection) {
                rawAuthorities = collection.stream().map(Object::toString).collect(Collectors.toList());
            }
        }
        if (rawAuthorities.isEmpty()) {
            rawAuthorities = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
        }

        String filter = ":" + resource.toUpperCase() + ":";
        return rawAuthorities.stream()
                .filter(Objects::nonNull)
                .filter(auth -> auth.toUpperCase().contains(filter))
                .map(PermissionSupport::extractAction)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> extractClaims(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal == null) {
            return null;
        }
        try {
            Method method = principal.getClass().getMethod("getClaims");
            Object maybeClaims = method.invoke(principal);
            if (maybeClaims instanceof Map<?, ?> map) {
                return (Map<String, Object>) map;
            }
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
        return null;
    }

    private static String extractAction(String authority) {
        if (authority == null || authority.isEmpty()) {
            return null;
        }
        int lastColon = authority.lastIndexOf(':');
        if (lastColon < 0 || lastColon == authority.length() - 1) {
            return null;
        }
        return authority.substring(lastColon + 1);
    }
}
