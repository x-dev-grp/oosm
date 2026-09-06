package com.xdev.ooms.security.permission.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class PermissionCatalogLoader {

    public static final String SPEC_RESOURCE = "permissions/permissions-spec.json";

    private final ObjectMapper objectMapper;

    public PermissionCatalogLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public PermissionCatalogSpec loadSpec() throws IOException {
        try (InputStream in = openSpecStream()) {
            return objectMapper.readValue(in, PermissionCatalogSpec.class);
        }
    }

    /** Raw classpath bytes of the live permissions-spec.json (for admin download / preview). */
    public byte[] readRawSpecBytes() throws IOException {
        try (InputStream in = openSpecStream()) {
            return in.readAllBytes();
        }
    }

    private InputStream openSpecStream() throws IOException {
        ClassPathResource resource = new ClassPathResource(SPEC_RESOURCE);
        return resource.getInputStream();
    }

    public List<String> resolveActions(PermissionCatalogSpec spec, PermissionCatalogSpec.EntitySpec entitySpec) {
        Map<String, List<String>> profiles = spec.getActionProfiles();
        List<String> actions;

        if (entitySpec.getPermissions() != null && !entitySpec.getPermissions().isEmpty()) {
            actions = new ArrayList<>(entitySpec.getPermissions());
        } else if (entitySpec.getProfile() != null) {
            List<String> profileActions = profiles.get(entitySpec.getProfile());
            if (profileActions == null || profileActions.isEmpty()) {
                throw new IllegalStateException("Unknown permission profile: " + entitySpec.getProfile());
            }
            actions = new ArrayList<>(profileActions);
        } else {
            throw new IllegalStateException("Entity must define profile or permissions");
        }

        if (entitySpec.getAddActions() != null) {
            actions.addAll(entitySpec.getAddActions());
        }
        if (entitySpec.getRemoveActions() != null) {
            Set<String> remove = new LinkedHashSet<>();
            entitySpec.getRemoveActions().forEach(a -> remove.add(a.toUpperCase()));
            actions.removeIf(a -> remove.contains(a.toUpperCase()));
        }

        Set<String> normalized = new LinkedHashSet<>();
        for (String action : actions) {
            if (action != null && !action.isBlank()) {
                normalized.add(action.trim().toUpperCase());
            }
        }
        return normalized.stream().sorted().toList();
    }
}
