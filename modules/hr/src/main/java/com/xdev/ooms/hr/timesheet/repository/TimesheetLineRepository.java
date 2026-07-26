package com.xdev.ooms.hr.timesheet.repository;

import com.xdev.ooms.hr.timesheet.entity.TimesheetLine;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TimesheetLineRepository extends BaseRepository<TimesheetLine> {
    List<TimesheetLine> findByTimesheet_IdAndIsDeletedFalseOrderByDateAsc(UUID timesheetId);
}
