package com.xdev.ooms.conditioning.repository;

import com.xdev.ooms.conditioning.model.LabelContent;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;

import java.util.List;
import java.util.UUID;

public interface LabelContentRepository extends BaseRepository<LabelContent> {
    java.util.Optional<LabelContent> findByIdAndIsDeletedFalse(UUID id);
    List<LabelContent> findAllByLotIdAndIsDeletedFalse(UUID lotId);
    List<LabelContent> findAllByTraceabilityLotIdAndIsDeletedFalse(UUID traceabilityLotId);
    List<LabelContent> findAllByPackagingIdAndIsDeletedFalse(UUID packagingId);
    List<LabelContent> findAllByProductIdAndIsDeletedFalse(UUID productId);
}
