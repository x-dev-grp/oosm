package com.xdev.ooms.production.repository;

import com.xdev.ooms.production.model.StorageUnit;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StorageUnitRepo extends BaseRepository<StorageUnit> {
    UUID id(UUID id);

    Optional<StorageUnit> findByQrHexIgnoreCaseAndIsDeletedFalse(String qrHex);

    Optional<StorageUnit> findByQrHexIgnoreCaseAndTenantIdAndIsDeletedFalse(String qrHex, UUID tenantId);
}
