package com.xdev.ooms.inventory.repository;



import com.xdev.ooms.inventory.Enum.CategorieArticle;

import com.xdev.ooms.inventory.entity.ArticleSec;

import com.xdev.ooms.inventory.entity.Fournisseur;

import com.xdev.ooms.sharedkernel.repos.BaseRepository;

import org.springframework.data.jpa.repository.Query;

import org.springframework.stereotype.Repository;



import java.util.List;



@Repository

public interface ArticleSecRepository extends BaseRepository<ArticleSec> {



    boolean existsByNomAndFournisseur(String nom, Fournisseur fournisseur);



    List<ArticleSec> findByActifTrue();



    List<ArticleSec> findByCategorie(CategorieArticle categorie);



    @Query("SELECT a FROM ArticleSec a WHERE a.actif = TRUE AND COALESCE(a.isDeleted, FALSE) = FALSE")

    List<ArticleSec> findAllActiveNotDeleted();



    @Query("SELECT COUNT(a) FROM ArticleSec a WHERE a.actif = TRUE AND COALESCE(a.isDeleted, FALSE) = FALSE")

    long countActiveNotDeleted();

}

