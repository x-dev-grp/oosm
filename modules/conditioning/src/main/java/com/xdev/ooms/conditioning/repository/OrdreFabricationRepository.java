package com.xdev.ooms.conditioning.repository;

import com.xdev.ooms.conditioning.Enum.StatutOF;
import com.xdev.ooms.conditioning.dto.analytics.OfAnalyticsProjection;
import com.xdev.ooms.conditioning.model.OrdreFabrication;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrdreFabricationRepository extends BaseRepository<OrdreFabrication> {
    Optional<OrdreFabrication> findByCodeAndTenantIdAndIsDeletedFalse(String code, UUID tenantId);
    List<OrdreFabrication> findByStatut(StatutOF statut);
    List<OrdreFabrication> findByDateDebutPrevueBetween(LocalDateTime debut, LocalDateTime fin);
    List<OrdreFabrication> findAllByProjetIdAndIsDeletedFalse(UUID projetId);
    List<OfAnalyticsProjection> findByTenantIdAndIsDeletedFalse(UUID projetId);
}