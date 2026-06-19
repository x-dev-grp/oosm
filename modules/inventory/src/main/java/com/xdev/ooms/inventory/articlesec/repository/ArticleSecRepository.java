package com.xdev.ooms.inventory.articlesec.repository;

import com.xdev.ooms.inventory.Enum.CategorieArticle;
import com.xdev.ooms.inventory.articlesec.entity.ArticleSec;
import com.xdev.ooms.inventory.materielsupplier.entity.MaterielSupplier;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ArticleSecRepository extends BaseRepository<ArticleSec> {

    boolean existsByNomAndMaterielSupplier(String nom, MaterielSupplier materielSupplier);

    boolean existsByNomAndMaterielSupplierAndIsDeletedFalse(String nom, MaterielSupplier materielSupplier);

    List<ArticleSec> findByActifTrue();

    List<ArticleSec> findByActifTrueAndIsDeletedFalse();

    List<ArticleSec> findByCategorie(CategorieArticle categorie);

    List<ArticleSec> findByCategorieAndIsDeletedFalse(CategorieArticle categorie);

    @Query("SELECT a FROM ArticleSec a WHERE a.actif = TRUE AND COALESCE(a.isDeleted, FALSE) = FALSE")
    List<ArticleSec> findAllActiveNotDeleted();

    @Query("SELECT COUNT(a) FROM ArticleSec a WHERE a.actif = TRUE AND COALESCE(a.isDeleted, FALSE) = FALSE")
    long countActiveNotDeleted();

    long countByMaterielSupplier_IdAndIsDeletedFalse(UUID materielSupplierId);
}
