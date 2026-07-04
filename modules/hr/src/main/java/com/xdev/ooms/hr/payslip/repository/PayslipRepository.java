package com.xdev.ooms.hr.payslip.repository;

import com.xdev.ooms.hr.payslip.entity.Payslip;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayslipRepository extends BaseRepository<Payslip> {
    List<Payslip> findByEmployee_IdAndIsDeletedFalse(UUID employeeId);

    List<Payslip> findByPayrollPeriod_IdAndIsDeletedFalse(UUID payrollPeriodId);

    @Query("""
            SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
            FROM Payslip p
            WHERE p.employee.id = :employeeId
              AND p.payrollPeriod.id = :payrollPeriodId
              AND p.isDeleted = false
              AND (:excludeId IS NULL OR p.id <> :excludeId)
            """)
    boolean existsByEmployee_IdAndPayrollPeriod_IdAndIdNotAndIsDeletedFalse(
            @Param("employeeId") UUID employeeId,
            @Param("payrollPeriodId") UUID payrollPeriodId,
            @Param("excludeId") UUID excludeId
    );

    @Query("""
            SELECT p FROM Payslip p
            LEFT JOIN FETCH p.employee
            LEFT JOIN FETCH p.payrollPeriod
            WHERE p.id = :id AND p.isDeleted = false
            """)
    Optional<Payslip> findWithDetailsByIdAndIsDeletedFalse(@Param("id") UUID id);
}
