package com.xdev.ooms.conditioning.sync.repository;

import com.xdev.ooms.conditioning.sync.entity.OfflineOperation;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OfflineOperationRepository extends BaseRepository<OfflineOperation> {
    boolean existsByOperationId(String operationId);
    Optional<OfflineOperation> findByOperationId(String operationId);
}