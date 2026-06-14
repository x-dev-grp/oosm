package com.xdev.ooms.conditioning.expedition.repository;

import com.xdev.ooms.conditioning.expedition.entity.ExpeditionArticle;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExpeditionArticleRepository extends BaseRepository<ExpeditionArticle> {
    Optional<ExpeditionArticle> findByIdAndExpeditionIdAndIsDeletedFalse(UUID id, UUID expeditionId);
}
