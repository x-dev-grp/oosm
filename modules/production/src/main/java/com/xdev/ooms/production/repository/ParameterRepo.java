package com.xdev.ooms.production.repository;

import com.xdev.ooms.production.model.Parameter;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository

public interface ParameterRepo extends BaseRepository<Parameter> {
    List<Parameter> findByTenantId(UUID tenantId);

    Optional<Parameter> findByTenantIdAndCode(UUID tenantId, String code);
}
