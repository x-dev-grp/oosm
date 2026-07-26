package com.xdev.ooms.hr.legal.repository;

import com.xdev.ooms.hr.legal.entity.MinimumWageRule;
import com.xdev.ooms.hr.legal.enums.WeeklyRegimeType;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface MinimumWageRuleRepository extends BaseRepository<MinimumWageRule> {

    @Query("""
            SELECT m FROM MinimumWageRule m
            WHERE m.tenantId = :tenantId
              AND COALESCE(m.isDeleted, false) = false
              AND m.active = true
              AND m.profile = :profile
              AND m.weeklyRegime = :regime
              AND m.effectiveFrom <= :asOf
              AND (m.effectiveTo IS NULL OR m.effectiveTo >= :asOf)
            ORDER BY m.effectiveFrom DESC
            """)
    List<MinimumWageRule> findEffective(
            @Param("tenantId") UUID tenantId,
            @Param("profile") String profile,
            @Param("regime") WeeklyRegimeType regime,
            @Param("asOf") LocalDate asOf
    );
}
