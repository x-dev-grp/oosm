package com.xdev.ooms.hr.legal.repository;

import com.xdev.ooms.hr.legal.entity.TaxConfiguration;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaxConfigurationRepository extends BaseRepository<TaxConfiguration> {

    @Query("""
            SELECT t FROM TaxConfiguration t
            WHERE t.tenantId = :tenantId
              AND COALESCE(t.isDeleted, false) = false
              AND t.active = true
              AND t.effectiveFrom <= :asOf
              AND (t.effectiveTo IS NULL OR t.effectiveTo >= :asOf)
            ORDER BY t.version DESC
            """)
    List<TaxConfiguration> findEffective(
            @Param("tenantId") UUID tenantId,
            @Param("asOf") LocalDate asOf
    );
}
