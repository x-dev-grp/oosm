package com.xdev.ooms.production.dayimport.repository;

import com.xdev.ooms.production.dayimport.entity.TenantGoogleDriveCredential;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantGoogleDriveCredentialRepository extends BaseRepository<TenantGoogleDriveCredential> {

    Optional<TenantGoogleDriveCredential> findFirstByTenantIdAndIsDeletedFalse(UUID tenantId);

    Optional<TenantGoogleDriveCredential> findFirstByTenantIdOrderByLastModifiedDateDesc(UUID tenantId);

    List<TenantGoogleDriveCredential> findAllByIsDeletedFalse();

    boolean existsByTenantIdAndIsDeletedFalse(UUID tenantId);
}
