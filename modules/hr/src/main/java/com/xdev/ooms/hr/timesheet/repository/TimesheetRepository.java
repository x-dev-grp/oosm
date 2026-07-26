package com.xdev.ooms.hr.timesheet.repository;

import com.xdev.ooms.hr.timesheet.entity.Timesheet;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TimesheetRepository extends BaseRepository<Timesheet> {
    List<Timesheet> findByEmployee_IdAndIsDeletedFalse(UUID employeeId);
}
