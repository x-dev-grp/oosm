package com.xdev.ooms.inventory.repository;

import com.xdev.ooms.inventory.Enum.StatutBonCommande;
import com.xdev.ooms.inventory.entity.LigneBonCommande;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.UUID;

@Repository
public interface LigneBonCommandeRepository extends BaseRepository<LigneBonCommande> {

    @Transactional
    void deleteByBonCommandeId(UUID bonCommandeId);

    @Query("""
            SELECT COUNT(l)
              FROM LigneBonCommande l
              JOIN l.bonCommande bc
             WHERE l.article.id = :articleId
               AND COALESCE(l.isDeleted, FALSE) = FALSE
               AND COALESCE(bc.isDeleted, FALSE) = FALSE
               AND bc.status IN :statuses
            """)
    long countByArticleIdAndBonCommandeStatusInAndIsDeletedFalse(
            @Param("articleId") UUID articleId,
            @Param("statuses") Collection<StatutBonCommande> statuses
    );
}
