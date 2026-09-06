package com.xdev.ooms.production.qualitycontrol.repository;



import com.xdev.ooms.production.qualitycontrol.entity.QualityControlResult;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QualityControlResultRepository extends BaseRepository<QualityControlResult> {
    List<QualityControlResult> findByDeliveryId(UUID deliveryId);

    @Query("select r from QualityControlResult r left join fetch r.rule where r.delivery.id = :deliveryId")
    List<QualityControlResult> findByDeliveryIdWithRule(@Param("deliveryId") UUID deliveryId);
    // Fetch only oil QC results for a given olive delivery (reception) id
    List<QualityControlResult> findByDeliveryIdAndRule_OilQcTrue(UUID deliveryId);
    List<QualityControlResult> findByFiltrationOperationIdAndIsDeletedFalse(UUID filtrationOperationId);
    List<QualityControlResult> findByTraceabilityLotIdAndIsDeletedFalse(UUID traceabilityLotId);
}
