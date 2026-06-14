package com.xdev.ooms.inventory.bom.repository;

import com.xdev.ooms.inventory.bom.entity.BOM;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BomRepository extends BaseRepository<BOM> {
    List<BOM> findByProduitFinalId(UUID productId);

    List<BOM> findByProduitFinalIdAndIsDeletedFalse(UUID productId);

    Optional<BOM> findFirstByProduitFinalIdAndActiveTrue(UUID productId);

    Optional<BOM> findFirstByProduitFinalIdAndActiveTrueAndIsDeletedFalse(UUID productId);

    long countByProduitFinalId(UUID productId);

    long countByProduitFinalIdAndIsDeletedFalse(UUID productId);
}
