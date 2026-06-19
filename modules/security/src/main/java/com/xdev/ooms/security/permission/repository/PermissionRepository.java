package com.xdev.ooms.security.permission.repository;

import com.xdev.ooms.security.permission.entity.Permission;
import com.xdev.ooms.sharedkernel.models.OSMModule;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends BaseRepository<Permission> {
    Optional<Permission> findByModuleAndEntityAndPermissionName(
            OSMModule module, String entity, String permissionName
    );

    List<Permission> findByModuleAndEntityIgnoreCaseAndIsDeletedFalse(OSMModule module, String entity);
}
