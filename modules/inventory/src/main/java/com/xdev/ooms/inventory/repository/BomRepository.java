package com.xdev.ooms.inventory.repository;

import com.xdev.ooms.inventory.entity.BOM;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BomRepository extends BaseRepository<BOM> {
    List<BOM> findByProduitFinalId(UUID productId);

    Optional<BOM> findFirstByProduitFinalIdAndActiveTrue(UUID productId);

    long countByProduitFinalId(UUID productId);
}
