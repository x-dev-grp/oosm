package com.xdev.ooms.hr.compliance.repository;

import com.xdev.ooms.hr.compliance.entity.HrComplianceViolation;
import com.xdev.ooms.hr.compliance.enums.ComplianceSeverity;
import com.xdev.ooms.hr.compliance.enums.ComplianceViolationStatus;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HrComplianceViolationRepository extends BaseRepository<HrComplianceViolation> {

    List<HrComplianceViolation> findByStatusAndIsDeletedFalse(ComplianceViolationStatus status);

    long countByStatusAndIsDeletedFalse(ComplianceViolationStatus status);

    long countByStatusAndSeverityAndIsDeletedFalse(ComplianceViolationStatus status, ComplianceSeverity severity);

    @Query("""
            SELECT COUNT(v) FROM HrComplianceViolation v
            WHERE v.isDeleted = false
              AND v.status IN (com.xdev.ooms.hr.compliance.enums.ComplianceViolationStatus.OPEN,
                               com.xdev.ooms.hr.compliance.enums.ComplianceViolationStatus.ACKNOWLEDGED)
              AND v.severity = :severity
            """)
    long countOpenBySeverity(@Param("severity") ComplianceSeverity severity);

    @Modifying
    @Query("""
            UPDATE HrComplianceViolation v
            SET v.status = com.xdev.ooms.hr.compliance.enums.ComplianceViolationStatus.RESOLVED
            WHERE v.isDeleted = false
              AND v.status IN (com.xdev.ooms.hr.compliance.enums.ComplianceViolationStatus.OPEN,
                               com.xdev.ooms.hr.compliance.enums.ComplianceViolationStatus.ACKNOWLEDGED)
              AND v.code = :code
              AND v.entityId = :entityId
            """)
    int resolveOpenByCodeAndEntity(@Param("code") String code, @Param("entityId") UUID entityId);

    @Query("""
            SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END
            FROM HrComplianceViolation v
            WHERE v.isDeleted = false
              AND v.code = :code
              AND v.entityId = :entityId
              AND v.status IN (com.xdev.ooms.hr.compliance.enums.ComplianceViolationStatus.OPEN,
                               com.xdev.ooms.hr.compliance.enums.ComplianceViolationStatus.ACKNOWLEDGED)
            """)
    boolean existsOpenByCodeAndEntity(@Param("code") String code, @Param("entityId") UUID entityId);
}
