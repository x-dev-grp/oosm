package com.xdev.ooms.production.repository;

import com.xdev.ooms.production.model.QualityControlResult;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QualityControlResultRepository extends BaseRepository<QualityControlResult> {
    List<QualityControlResult> findByDeliveryId(UUID deliveryId);
    // Fetch only oil QC results for a given olive delivery (reception) id
    List<QualityControlResult> findByDeliveryIdAndRule_OilQcTrue(UUID deliveryId);
    List<QualityControlResult> findByFiltrationOperationIdAndIsDeletedFalse(UUID filtrationOperationId);
    List<QualityControlResult> findByTraceabilityLotIdAndIsDeletedFalse(UUID traceabilityLotId);
}
