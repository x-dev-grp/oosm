package com.xdev.ooms.production.maintenance.repository;

import com.xdev.ooms.production.maintenance.entity.MaintenanceWorkOrder;
import com.xdev.ooms.production.maintenance.enums.MaintenanceAssetType;
import com.xdev.ooms.production.maintenance.enums.MaintenanceWorkOrderStatus;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;

@Repository
public interface MaintenanceWorkOrderRepository extends BaseRepository<MaintenanceWorkOrder> {

    boolean existsByAssetTypeAndAssetIdAndStatusIn(
            MaintenanceAssetType assetType,
            UUID assetId,
            Collection<MaintenanceWorkOrderStatus> statuses);
}
