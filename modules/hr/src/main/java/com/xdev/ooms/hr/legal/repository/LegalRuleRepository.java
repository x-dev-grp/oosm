package com.xdev.ooms.hr.legal.repository;

import com.xdev.ooms.hr.legal.entity.LegalRule;
import com.xdev.ooms.hr.legal.enums.LegalRuleCategory;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface LegalRuleRepository extends BaseRepository<LegalRule> {

    @Query("""
            SELECT r FROM LegalRule r
            WHERE r.tenantId = :tenantId
              AND COALESCE(r.isDeleted, false) = false
              AND r.active = true
              AND r.code = :code
              AND r.category = :category
              AND r.effectiveFrom <= :asOf
              AND (r.effectiveTo IS NULL OR r.effectiveTo >= :asOf)
            ORDER BY r.version DESC
            """)
    List<LegalRule> findEffectiveByCodeAndCategory(
            @Param("tenantId") UUID tenantId,
            @Param("code") String code,
            @Param("category") LegalRuleCategory category,
            @Param("asOf") LocalDate asOf
    );
}
