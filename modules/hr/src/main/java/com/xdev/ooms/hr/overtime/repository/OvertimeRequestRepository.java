package com.xdev.ooms.hr.overtime.repository;

import com.xdev.ooms.hr.overtime.entity.OvertimeRequest;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OvertimeRequestRepository extends BaseRepository<OvertimeRequest> {
    List<OvertimeRequest> findByEmployee_IdAndIsDeletedFalse(UUID employeeId);
}
