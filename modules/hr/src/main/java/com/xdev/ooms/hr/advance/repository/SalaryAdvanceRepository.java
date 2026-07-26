package com.xdev.ooms.hr.advance.repository;

import com.xdev.ooms.hr.advance.entity.SalaryAdvance;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SalaryAdvanceRepository extends BaseRepository<SalaryAdvance> {
    List<SalaryAdvance> findByEmployee_IdAndIsDeletedFalse(UUID employeeId);
}
