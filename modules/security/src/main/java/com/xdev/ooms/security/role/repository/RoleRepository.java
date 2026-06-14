package com.xdev.ooms.security.role.repository;

import com.xdev.ooms.security.role.entity.Role;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends BaseRepository<Role> {
     Optional<Role> findByRoleName(String roleName);
}
