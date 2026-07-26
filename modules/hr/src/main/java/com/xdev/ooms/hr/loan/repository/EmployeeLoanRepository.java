package com.xdev.ooms.hr.loan.repository;

import com.xdev.ooms.hr.loan.entity.EmployeeLoan;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmployeeLoanRepository extends BaseRepository<EmployeeLoan> {
    List<EmployeeLoan> findByEmployee_IdAndIsDeletedFalse(UUID employeeId);
}
