package com.xdev.ooms.sharedkernel.repos;

import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.history.RevisionRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@NoRepositoryBean
public interface BaseRepository<E extends BaseEntity> extends JpaRepository<E, UUID>, RevisionRepository<E, UUID, Integer>, JpaSpecificationExecutor<E> {
    List<E> findByTenantId(UUID tenantId);

    Optional<E> findByExternalIdAndIsDeletedFalse(UUID externalId);

    Optional<E> findByIdAndTenantIdAndIsDeletedFalse(UUID id, UUID tenantId);

    List<E> findAllByTenantIdAndIsDeletedFalse(UUID tenantId);

    Page<E> findAllByTenantIdAndIsDeletedFalse(UUID tenantId, Pageable pageable);

    Optional<E> findByIdAndIsDeletedFalse(UUID id);

    List<E> findAllByIsDeletedFalse();

    Page<E> findAllByIsDeletedFalse(Pageable pageable);

    //------QRCode----------//
    Optional<E> findByQrHex(String qrHex);
    boolean existsByQrHex(String qrHex);

    Optional<E> findByQrHexAndTenantIdAndIsDeletedFalse(String qrHex, UUID tenantId);

    Optional<E> findByQrHexIgnoreCaseAndIsDeletedFalse(String qrHex);

    Optional<E> findByQrHexIgnoreCaseAndTenantIdAndIsDeletedFalse(String qrHex, UUID tenantId);
    //------QRCode----------//
}
