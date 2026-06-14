package com.xdev.ooms.production.parameter.repository;



import com.xdev.ooms.production.parameter.entity.Parameter;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository

public interface ParameterRepo extends BaseRepository<Parameter> {
    List<Parameter> findByTenantId(UUID tenantId);

    Optional<Parameter> findByTenantIdAndCode(UUID tenantId, String code);

    @Query("SELECT DISTINCT p.tenantId FROM Parameter p WHERE p.tenantId IS NOT NULL")
    List<UUID> findDistinctTenantIds();
}
