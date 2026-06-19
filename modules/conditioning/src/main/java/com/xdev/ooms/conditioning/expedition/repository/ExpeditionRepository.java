package com.xdev.ooms.conditioning.expedition.repository;

import com.xdev.ooms.conditioning.expedition.entity.Expedition;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExpeditionRepository extends BaseRepository<Expedition> {

    @Query("""
            SELECT DISTINCT e FROM Expedition e
            LEFT JOIN FETCH e.projet p
            LEFT JOIN FETCH p.client
            LEFT JOIN FETCH e.lines
            WHERE e.id = :id AND e.isDeleted = false
            """)
    Optional<Expedition> findByIdForPdf(@Param("id") UUID id);
    Optional<Expedition> findByIdAndIsDeletedFalse(UUID id);

    Optional<Expedition> findByQrHexAndIsDeletedFalse(String qrHex);

    Optional<Expedition> findByExpeditionNumberIgnoreCaseAndIsDeletedFalse(String expeditionNumber);

    List<Expedition> findAllByIsDeletedFalseOrderByCreatedDateDesc();

    List<Expedition> findAllByTenantIdAndIsDeletedFalseOrderByCreatedDateDesc(UUID tenantId);

    List<Expedition> findAllByProjetIdAndIsDeletedFalseOrderByCreatedDateDesc(UUID projetId);
}
