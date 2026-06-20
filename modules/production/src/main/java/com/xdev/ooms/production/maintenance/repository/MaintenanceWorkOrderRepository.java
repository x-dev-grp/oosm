package com.xdev.ooms.production.maintenance.repository;

import com.xdev.ooms.production.maintenance.entity.MaintenanceWorkOrder;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaintenanceWorkOrderRepository extends BaseRepository<MaintenanceWorkOrder> {
}
