package com.xdev.ooms.sharedkernel.utils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Map;
import java.util.Optional;

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
}
