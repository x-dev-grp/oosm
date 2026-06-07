package com.xdev.ooms.security.userManagement.data;

import com.xdev.ooms.security.userManagement.models.Role;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends BaseRepository<Role> {
     Optional<Role> findByRoleName(String roleName);
}
