package com.xdev.ooms.security.companyprofile.repository;

import com.xdev.ooms.security.companyprofile.entity.CompanyProfile;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface CompanyProfileRepository extends BaseRepository<CompanyProfile> {
    @Query("""
            SELECT c FROM CompanyProfile c
            WHERE COALESCE(c.isDeleted, FALSE) = FALSE
              AND (c.id IN :ids OR c.tenantId IN :ids)
            """)
    List<CompanyProfile> findActiveByIdOrTenantIdIn(@Param("ids") Collection<UUID> ids);

    @Query("SELECT COUNT(c) FROM CompanyProfile c WHERE COALESCE(c.isDeleted, FALSE) = FALSE")
    long countAllActiveTenants();

    @Query("SELECT COUNT(c) FROM CompanyProfile c WHERE COALESCE(c.isDeleted, FALSE) = FALSE AND c.active = TRUE")
    long countActiveTenants();

    @Query("""
            SELECT c FROM CompanyProfile c
            WHERE COALESCE(c.isDeleted, FALSE) = FALSE
            ORDER BY c.createdDate DESC
            """)
    List<CompanyProfile> findRecentTenants(Pageable pageable);
}