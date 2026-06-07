package com.xdev.ooms.conditioning.repository;

import com.xdev.ooms.conditioning.model.QCResult;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import java.util.List;
import java.util.UUID;

public interface QCResultRepository extends BaseRepository<QCResult> {
    List<QCResult> findByOfIdAndTenantIdOrderByDateControleDesc(UUID ofId, UUID tenantId);
}