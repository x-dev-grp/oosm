package com.xdev.ooms.conditioning.projet.repository;

import com.xdev.ooms.conditioning.projet.entity.Projet;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjetRepository extends BaseRepository<Projet> {

    @Query("""
            SELECT DISTINCT p FROM Projet p
            LEFT JOIN FETCH p.client
            WHERE p.id = :id AND p.isDeleted = false
            """)
    Optional<Projet> findByIdForPdf(@Param("id") UUID id);

    Optional<Projet> findByQrHex(String qrHex);
    boolean existsByQrHex(String qrHex);
    Optional<Projet> findByQrHexAndTenantIdAndIsDeletedFalse(String qrHex, UUID tenantId);

    Optional<Projet> findByCodeAndTenantIdAndIsDeletedFalse(String code, UUID tenantId);

    Optional<Projet> findByCodeAndIsDeletedFalse(String code);

    Optional<Projet> findByCodeIgnoreCaseAndIsDeletedFalse(String code);

    @Query("SELECT p FROM Projet p WHERE (UPPER(p.qrHex) = UPPER(:code) OR UPPER(p.code) = UPPER(:code)) AND p.isDeleted = false")
    Optional<Projet> searchByCodeCustom(String code);

    Optional<Projet> findByCodeIgnoreCaseAndTenantIdAndIsDeletedFalse(String code, UUID tenantId);
}