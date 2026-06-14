package com.xdev.ooms.inventory.produitfinal.repository;



import com.xdev.ooms.inventory.produitfinal.entity.ProduitFinal;
import com.xdev.ooms.inventory.Enum.ProduitFinalType;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProduitFinalRepository extends BaseRepository<ProduitFinal> {
    Optional<ProduitFinal> findByIdAndIsDeletedFalse(UUID id);
    Optional<ProduitFinal> findByNameAndIsDeletedFalse(String name);
    Optional<ProduitFinal> findByCodeAndIsDeletedFalse(String code);
    List<ProduitFinal> findByIsDeletedFalse();
    List<ProduitFinal> findByActifTrueAndIsDeletedFalse();
    List<ProduitFinal> findByTypeAndIsDeletedFalse(ProduitFinalType type);

}
