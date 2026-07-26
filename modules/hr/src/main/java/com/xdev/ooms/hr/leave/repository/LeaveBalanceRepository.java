package com.xdev.ooms.hr.leave.repository;

import com.xdev.ooms.hr.leave.entity.LeaveBalance;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LeaveBalanceRepository extends BaseRepository<LeaveBalance> {
    List<LeaveBalance> findByEmployee_IdAndIsDeletedFalse(UUID employeeId);
}
