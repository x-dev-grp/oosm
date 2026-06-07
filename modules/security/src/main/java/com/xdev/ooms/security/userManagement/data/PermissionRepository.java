package com.xdev.ooms.security.userManagement.data;

import com.xdev.ooms.security.userManagement.models.Permission;
import com.xdev.ooms.sharedkernel.models.OSMModule;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends BaseRepository<Permission> {
    // Derived query method (works if fields are named exactly as in the entity)
    Optional<Permission> findByModuleAndEntityAndPermissionName(
            OSMModule module, String entity, String permissionName
    );

}
