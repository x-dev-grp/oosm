package com.xdev.ooms.sharedkernel.utils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {
        // Utility class
    }

    //lit le jeton JWT de l’utilisateur authentifié
    @SuppressWarnings("unchecked")
    public static Optional<Map<String, Object>> getCurrentOsmUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }

        if (auth.getPrincipal() instanceof Jwt jwt) {
            Object osmUserClaim = jwt.getClaim("osmUser");
            if (osmUserClaim instanceof Map<?, ?> map) {
                return Optional.of((Map<String, Object>) map);
            }
        }
        return Optional.empty();
    }

    public static Optional<String> getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }

        if (auth.getPrincipal() instanceof Jwt jwt) {
            String subject = jwt.getSubject();
            if (subject != null && !subject.isBlank()) {
                return Optional.of(subject);
            }
            Object osmUserClaim = jwt.getClaim("osmUser");
            if (osmUserClaim instanceof Map<?, ?> map && map.get("username") != null) {
                return Optional.of(map.get("username").toString());
            }
        }

        String name = auth.getName();
        if (name != null && !name.isBlank() && !"anonymousUser".equals(name)) {
            return Optional.of(name);
        }
        return Optional.empty();
    }

    public static Optional<UUID> getCurrentUserId() {
        return getCurrentOsmUser()
                .map(map -> map.get("id"))
                .map(Object::toString)
                .flatMap(value -> {
                    try {
                        return Optional.of(UUID.fromString(value));
                    } catch (IllegalArgumentException ex) {
                        return Optional.empty();
                    }
                });
    }

    public static Optional<String> getCurrentUserDisplayName() {
        return getCurrentOsmUser().map(map -> {
            String first = map.get("firstName") != null ? map.get("firstName").toString().trim() : "";
            String last = map.get("lastName") != null ? map.get("lastName").toString().trim() : "";
            String full = (first + " " + last).trim();
            if (!full.isBlank()) {
                return full;
            }
            Object username = map.get("username");
            return username != null ? username.toString() : null;
        });
    }

    public static Optional<String> getCurrentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }

        if (auth.getPrincipal() instanceof Jwt jwt) {
            String role = jwt.getClaimAsString("role");
            if (role != null && !role.isBlank()) {
                return Optional.of(normalizeRole(role));
            }
        }

        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority != null && !authority.isBlank())
                .map(SecurityUtils::normalizeRole)
                .filter(role -> "ADMIN".equals(role) || "OSMADMIN".equals(role))
                .findFirst();
    }

    public static boolean isOsmAdmin() {
        return getCurrentRole().map("OSMADMIN"::equals).orElse(false);
    }

    public static boolean isTenantAdmin() {
        return getCurrentRole().map("ADMIN"::equals).orElse(false);
    }

    public static boolean hasElevatedAdminAccess() {
        return getCurrentRole()
                .map(role -> "ADMIN".equals(role) || "OSMADMIN".equals(role))
                .orElse(false);
    }

    private static String normalizeRole(String role) {
        return role.trim().toUpperCase().replace("ROLE_", "");
    }
}
