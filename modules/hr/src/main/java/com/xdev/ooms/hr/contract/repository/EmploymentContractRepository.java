package com.xdev.ooms.hr.contract.repository;

import com.xdev.ooms.hr.common.enums.ContractStatus;
import com.xdev.ooms.hr.contract.entity.EmploymentContract;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmploymentContractRepository extends BaseRepository<EmploymentContract> {
    List<EmploymentContract> findByEmployee_IdAndIsDeletedFalse(UUID employeeId);

    List<EmploymentContract> findByEmployee_IdAndStatusAndIsDeletedFalse(UUID employeeId, ContractStatus status);

    @Query("""
            SELECT c FROM EmploymentContract c
            WHERE c.employee.id = :employeeId
              AND c.isDeleted = false
              AND c.status = com.xdev.ooms.hr.common.enums.ContractStatus.ACTIVE
              AND c.startDate <= :date
              AND (c.endDate IS NULL OR c.endDate >= :date)
            """)
            Optional<EmploymentContract> findActiveForEmployeeOnDate(
            @Param("employeeId") UUID employeeId,
            @Param("date") LocalDate date
    );

    @Query("""
            SELECT DISTINCT c.employee.id FROM EmploymentContract c
            WHERE c.isDeleted = false
              AND c.status = com.xdev.ooms.hr.common.enums.ContractStatus.ACTIVE
              AND c.startDate <= :periodEnd
              AND (c.endDate IS NULL OR c.endDate >= :periodStart)
            """)
    List<UUID> findEmployeeIdsWithActiveContractInPeriod(
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd
    );

    @Query("""
            SELECT COUNT(c) FROM EmploymentContract c
            WHERE c.isDeleted = false
              AND c.status = com.xdev.ooms.hr.common.enums.ContractStatus.ACTIVE
              AND c.endDate IS NOT NULL
              AND c.endDate >= :from
              AND c.endDate <= :to
            """)
    long countExpiringBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            SELECT c FROM EmploymentContract c
            WHERE c.isDeleted = false
              AND c.status = com.xdev.ooms.hr.common.enums.ContractStatus.ACTIVE
              AND c.endDate IS NOT NULL
              AND c.endDate >= :from
              AND c.endDate <= :to
            """)
    List<EmploymentContract> findExpiringBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
