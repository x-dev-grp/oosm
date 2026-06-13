package com.xdev.ooms.conditioning.projet.repository;

import com.xdev.ooms.conditioning.projet.entity.ProjetProduit;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProjetProduitRepository extends BaseRepository<ProjetProduit> {

    @Query("""
            SELECT COUNT(pp) FROM ProjetProduit pp
            JOIN pp.projet p
            WHERE pp.productId = :productId
            AND COALESCE(pp.isDeleted, FALSE) = FALSE
            AND COALESCE(p.isDeleted, FALSE) = FALSE
            AND UPPER(COALESCE(p.statut, '')) NOT IN ('ANNULE', 'CLOTURE', 'TERMINE')
            """)
    long countActiveProjectsByProductId(@Param("productId") UUID productId);
}
