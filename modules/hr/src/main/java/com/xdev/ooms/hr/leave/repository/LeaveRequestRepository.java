package com.xdev.ooms.hr.leave.repository;

import com.xdev.ooms.hr.leave.entity.LeaveRequest;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface LeaveRequestRepository extends BaseRepository<LeaveRequest> {
    List<LeaveRequest> findByEmployee_IdAndIsDeletedFalse(UUID employeeId);

    @Query("""
            SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END
            FROM LeaveRequest l
            WHERE l.employee.id = :employeeId
              AND l.isDeleted = false
              AND l.status IN (com.xdev.ooms.hr.common.enums.LeaveStatus.PENDING,
                               com.xdev.ooms.hr.common.enums.LeaveStatus.APPROVED)
              AND (:excludeId IS NULL OR l.id <> :excludeId)
              AND l.startDate <= :endDate
              AND l.endDate >= :startDate
            """)
    boolean existsOverlapping(
            @Param("employeeId") UUID employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludeId") UUID excludeId
    );

    @Query("""
            SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END
            FROM LeaveRequest l
            WHERE l.employee.id = :employeeId
              AND l.isDeleted = false
              AND l.status = com.xdev.ooms.hr.common.enums.LeaveStatus.APPROVED
              AND :date BETWEEN l.startDate AND l.endDate
            """)
    boolean hasApprovedLeaveOn(@Param("employeeId") UUID employeeId, @Param("date") LocalDate date);

    long countByStatusAndIsDeletedFalse(com.xdev.ooms.hr.common.enums.LeaveStatus status);

    @Query("""
            SELECT COUNT(DISTINCT l.employee.id) FROM LeaveRequest l
            WHERE l.isDeleted = false
              AND l.status = com.xdev.ooms.hr.common.enums.LeaveStatus.APPROVED
              AND :date BETWEEN l.startDate AND l.endDate
            """)
    long countEmployeesOnLeaveOn(@Param("date") LocalDate date);

    @Query("""
            SELECT l FROM LeaveRequest l
            WHERE l.isDeleted = false
              AND l.status = com.xdev.ooms.hr.common.enums.LeaveStatus.PENDING
            """)
    List<LeaveRequest> findPending();
}
