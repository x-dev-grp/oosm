package com.xdev.ooms.hr.payroll.repository;

import com.xdev.ooms.hr.payroll.entity.PayrollPeriod;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PayrollPeriodRepository extends BaseRepository<PayrollPeriod> {

    @Query("""
            SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
            FROM PayrollPeriod p
            WHERE p.year = :year
              AND p.month = :month
              AND p.isDeleted = false
              AND (:excludeId IS NULL OR p.id <> :excludeId)
            """)
    boolean existsByYearAndMonthAndIdNotAndIsDeletedFalse(
            @Param("year") Integer year,
            @Param("month") Integer month,
            @Param("excludeId") UUID excludeId
    );

    @Query("""
            SELECT p FROM PayrollPeriod p
            WHERE p.isDeleted = false
              AND p.year = :year
              AND p.month = :month
            """)
    java.util.Optional<PayrollPeriod> findByYearAndMonthAndIsDeletedFalse(
            @Param("year") Integer year,
            @Param("month") Integer month
    );
}
