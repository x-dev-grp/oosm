package com.xdev.ooms.security.permission.catalog;

import com.xdev.ooms.security.permission.entity.Permission;
import com.xdev.ooms.security.permission.repository.PermissionRepository;
import com.xdev.ooms.security.role.entity.Role;
import com.xdev.ooms.security.role.repository.RoleRepository;
import com.xdev.ooms.sharedkernel.models.OOSMModule;
import com.xdev.ooms.sharedkernel.utils.OOSMLogger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class PermissionCatalogSyncService {

    private final PermissionCatalogLoader catalogLoader;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    public PermissionCatalogSyncService(PermissionCatalogLoader catalogLoader,
                                        PermissionRepository permissionRepository,
                                        RoleRepository roleRepository) {
        this.catalogLoader = catalogLoader;
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public PermissionCatalogSyncResult syncFromCatalog() {
        try {
            PermissionCatalogSpec spec = catalogLoader.loadSpec();
            int created = 0;
            int existing = 0;

            for (Map.Entry<String, PermissionCatalogSpec.EntitySpec> entry : spec.getEntities().entrySet()) {
                SyncCount count = ensureEntityPermissions(spec, entry.getKey(), entry.getValue());
                created += count.created();
                existing += count.existing();
            }

            int legacyMerged = mergeLegacyAliases(spec);
            int mirrorGrants = mirrorRolePermissions(spec);

            PermissionCatalogSyncResult result = new PermissionCatalogSyncResult(
                    created, existing, legacyMerged, mirrorGrants);

            OOSMLogger.logBusinessEvent(this.getClass(), "PERMISSION_CATALOG_SYNC",
                    "created=" + created + ", existing=" + existing
                            + ", legacyMerged=" + legacyMerged + ", mirrorGrants=" + mirrorGrants);
            return result;
        } catch (Exception e) {
            OOSMLogger.logException(this.getClass(), "Permission catalog sync failed", e);
            throw new IllegalStateException("Permission catalog sync failed: " + e.getMessage(), e);
        }
    }

    private SyncCount ensureEntityPermissions(PermissionCatalogSpec spec,
                                              String entityName,
                                              PermissionCatalogSpec.EntitySpec entitySpec) {
        OOSMModule module = parseModule(entitySpec.getModule(), entityName);
        List<String> actions = catalogLoader.resolveActions(spec, entitySpec);

        int created = 0;
        int existing = 0;
        for (String action : actions) {
            if (ensurePermission(module, entityName, action)) {
                created++;
            } else {
                existing++;
            }
        }
        return new SyncCount(created, existing);
    }

    private boolean ensurePermission(OOSMModule module, String entity, String action) {
        Optional<Permission> active = permissionRepository
                .findByModuleAndEntityAndPermissionNameAndIsDeletedFalse(module, entity, action);
        if (active.isPresent()) {
            return false;
        }

        Optional<Permission> existing = permissionRepository
                .findByModuleAndEntityAndPermissionName(module, entity, action);
        if (existing.isPresent()) {
            Permission permission = existing.get();
            if (Boolean.TRUE.equals(permission.getDeleted())) {
                permission.setDeleted(false);
                permissionRepository.save(permission);
                return true;
            }
            return false;
        }

        Permission permission = new Permission();
        permission.setModule(module);
        permission.setEntity(entity);
        permission.setPermissionName(action);
        permission.setDeleted(false);
        permissionRepository.save(permission);
        return true;
    }

    private int mergeLegacyAliases(PermissionCatalogSpec spec) {
        AtomicInteger merged = new AtomicInteger();

            for (Map.Entry<String, PermissionCatalogSpec.EntitySpec> entry : spec.getEntities().entrySet()) {
                String canonicalEntity = entry.getKey();
                PermissionCatalogSpec.EntitySpec entitySpec = entry.getValue();
                if (entitySpec.getLegacyAliases() == null || entitySpec.getLegacyAliases().isEmpty()) {
                    continue;
                }

                OOSMModule module = parseModule(entitySpec.getModule(), canonicalEntity);
                for (String alias : entitySpec.getLegacyAliases()) {
                    List<Permission> legacyPermissions = permissionRepository
                            .findByModuleAndEntityIgnoreCaseAndIsDeletedFalse(module, alias);

                    for (Permission legacy : legacyPermissions) {
                        Optional<Permission> target = permissionRepository
                                .findByModuleAndEntityAndPermissionNameAndIsDeletedFalse(
                                        module, canonicalEntity, legacy.getPermissionName());

                        if (target.isPresent()) {
                            merged.addAndGet(mergeRoleGrants(legacy, target.get()));
                            legacy.setDeleted(true);
                            permissionRepository.save(legacy);
                        }
                    }
                }
            }

        return merged.get();
    }

    private int mirrorRolePermissions(PermissionCatalogSpec spec) {
        int grantsAdded = 0;

        for (PermissionCatalogSpec.RoleMirrorSpec mirror : spec.getRoleMirrors()) {
            OOSMModule sourceModule = parseModule(mirror.getSourceModule(), mirror.getSourceEntity());
            OOSMModule targetModule = parseModule(mirror.getTargetModule(), mirror.getTargetEntity());

            List<Permission> sourcePermissions = permissionRepository
                    .findByModuleAndEntityIgnoreCaseAndIsDeletedFalse(sourceModule, mirror.getSourceEntity());

            for (Permission sourcePermission : sourcePermissions) {
                Optional<Permission> targetPermission = permissionRepository
                        .findByModuleAndEntityAndPermissionNameAndIsDeletedFalse(
                                targetModule, mirror.getTargetEntity(), sourcePermission.getPermissionName());
                if (targetPermission.isEmpty()) {
                    continue;
                }
                grantsAdded += mergeRoleGrants(sourcePermission, targetPermission.get());
            }
        }

        return grantsAdded;
    }

    private int mergeRoleGrants(Permission fromPermission, Permission toPermission) {
        int added = 0;
        List<Role> roles = roleRepository.findAllByIsDeletedFalse();

        for (Role role : roles) {
            boolean hasSource = role.getPermissions().stream()
                    .anyMatch(p -> p.getId().equals(fromPermission.getId()));
            if (!hasSource) {
                continue;
            }

            boolean alreadyHasTarget = role.getPermissions().stream()
                    .anyMatch(p -> p.getId().equals(toPermission.getId()));
            if (alreadyHasTarget) {
                continue;
            }

            Set<Permission> updated = new HashSet<>(role.getPermissions());
            updated.add(toPermission);
            role.setPermissions(updated);
            roleRepository.save(role);
            added++;
        }

        return added;
    }

    private OOSMModule parseModule(String moduleName, String context) {
        if (moduleName == null || moduleName.isBlank()) {
            throw new IllegalStateException("Missing module for " + context);
        }
        return OOSMModule.valueOf(moduleName.trim().toUpperCase());
    }

    private record SyncCount(int created, int existing) {
    }
}
