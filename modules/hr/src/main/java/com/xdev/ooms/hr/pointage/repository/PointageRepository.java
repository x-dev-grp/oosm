package com.xdev.ooms.hr.pointage.repository;

import com.xdev.ooms.hr.pointage.entity.Pointage;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface PointageRepository extends BaseRepository<Pointage> {
    List<Pointage> findByEmployee_IdAndIsDeletedFalse(UUID employeeId);

    @Query("""
            SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
            FROM Pointage p
            WHERE p.employee.id = :employeeId
              AND p.workDate = :workDate
              AND p.isDeleted = false
              AND (:excludeId IS NULL OR p.id <> :excludeId)
            """)
    boolean existsByEmployee_IdAndWorkDateAndIdNotAndIsDeletedFalse(
            @Param("employeeId") UUID employeeId,
            @Param("workDate") LocalDate workDate,
            @Param("excludeId") UUID excludeId
    );

    @Query("""
            SELECT COUNT(DISTINCT p.employee.id) FROM Pointage p
            WHERE p.isDeleted = false
              AND p.workDate = :workDate
              AND p.checkIn IS NOT NULL
            """)
    long countPresentOn(@Param("workDate") LocalDate workDate);

    @Query("""
            SELECT COUNT(p) FROM Pointage p
            WHERE p.isDeleted = false
              AND p.workDate = :workDate
              AND p.anomalyCodes IS NOT NULL
              AND p.anomalyCodes <> ''
            """)
    long countAnomaliesOn(@Param("workDate") LocalDate workDate);

    @Query("""
            SELECT COUNT(p) FROM Pointage p
            WHERE p.isDeleted = false
              AND p.anomalyCodes IS NOT NULL
              AND p.anomalyCodes <> ''
              AND p.workDate >= :from
            """)
    long countRecentAnomalies(@Param("from") LocalDate from);
}
