package com.xdev.ooms.hr.legal.repository;

import com.xdev.ooms.hr.legal.entity.SocialSecurityConfiguration;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface SocialSecurityConfigurationRepository extends BaseRepository<SocialSecurityConfiguration> {

    boolean existsByTenantIdAndIsDeletedFalse(UUID tenantId);

    @Query("""
            SELECT s FROM SocialSecurityConfiguration s
            WHERE s.tenantId = :tenantId
              AND COALESCE(s.isDeleted, false) = false
              AND s.active = true
              AND s.effectiveFrom <= :asOf
              AND (s.effectiveTo IS NULL OR s.effectiveTo >= :asOf)
            ORDER BY s.version DESC
            """)
    List<SocialSecurityConfiguration> findEffective(
            @Param("tenantId") UUID tenantId,
            @Param("asOf") LocalDate asOf
    );
}
