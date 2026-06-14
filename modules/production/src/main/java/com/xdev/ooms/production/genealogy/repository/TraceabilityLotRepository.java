package com.xdev.ooms.production.genealogy.repository;



import com.xdev.ooms.production.genealogy.entity.TraceabilityLot;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TraceabilityLotRepository extends BaseRepository<TraceabilityLot> {
    Optional<TraceabilityLot> findByIdAndIsDeletedFalse(UUID id);

    Optional<TraceabilityLot> findFirstByStorageUnitIdAndActiveTrueAndIsDeletedFalseOrderByCapturedAtDesc(UUID storageUnitId);

    Optional<TraceabilityLot> findFirstByLotNumberAndIsDeletedFalseOrderByCapturedAtDesc(String lotNumber);

    Optional<TraceabilityLot> findFirstByFiltrationOperationIdAndIsDeletedFalseOrderByCapturedAtDesc(UUID filtrationOperationId);

    List<TraceabilityLot> findAllByRootReceptionIdAndIsDeletedFalse(UUID rootReceptionId);
}
