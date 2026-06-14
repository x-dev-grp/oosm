package com.xdev.ooms.inventory.ligneconditionnement.repository;


import com.xdev.ooms.inventory.Enum.Statue;
import com.xdev.ooms.inventory.ligneconditionnement.entity.LigneConditionnement;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LigneConditionnementRepository extends BaseRepository<LigneConditionnement> {

    List<LigneConditionnement> findByEtat(Statue etat);
    boolean existsByCode(String code);
}