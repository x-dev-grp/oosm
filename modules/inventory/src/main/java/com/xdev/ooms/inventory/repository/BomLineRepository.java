package com.xdev.ooms.inventory.repository;

import com.xdev.ooms.inventory.entity.BomLine;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;

import java.util.UUID;

public interface BomLineRepository extends BaseRepository<BomLine> {
    long countByArticle_Id(UUID articleId);

    long countByArticle_IdAndIsDeletedFalseAndBom_IsDeletedFalse(UUID articleId);
}
