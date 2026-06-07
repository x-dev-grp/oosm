package com.xdev.ooms.inventory.repository;

import com.xdev.ooms.inventory.entity.LigneBonCommande;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public interface LigneBonCommandeRepository extends BaseRepository<LigneBonCommande> {

    @Transactional
    void deleteByBonCommandeId(UUID bonCommandeId);
}