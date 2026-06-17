package com.xdev.ooms.production.supplier.service;


import com.xdev.ooms.production.supplier.entity.Supplier;
import com.xdev.ooms.sharedkernel.basetype.entity.BaseType;
import com.xdev.ooms.production.supplier.dto.SupplierDto;
import com.xdev.ooms.production.unifieddelivery.repository.DeliveryRepository;
import com.xdev.ooms.sharedkernel.basetype.repository.GenericRepository;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import com.xdev.ooms.sharedkernel.utils.OSMLogger;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class SupplierTypeService extends BaseServiceImpl<Supplier, SupplierDto, SupplierDto> {

    private final GenericRepository baseTypeRepository;
    private final DeliveryRepository deliveryRepository;

    public SupplierTypeService(BaseRepository<Supplier> repository,
                               ModelMapper modelMapper,
                               GenericRepository baseTypeRepository,
                               DeliveryRepository deliveryRepository) {
        super(repository, modelMapper);
        this.baseTypeRepository = baseTypeRepository;
        this.deliveryRepository = deliveryRepository;
    }

    public long getPaidPaymentsCount(UUID supplierId) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "getPaidPaymentsCount", supplierId);

        try {
            long count = deliveryRepository.countFullyPaidDeliveriesBySupplierId(supplierId);

            OSMLogger.logDataAccess(this.getClass(), "PAID_PAYMENTS_COUNT", "Supplier");
            OSMLogger.logBusinessEvent(this.getClass(), "SUPPLIER_PAID_PAYMENTS_QUERIED",
                    "Found " + count + " paid payments for supplier: " + supplierId);
            OSMLogger.logMethodExit(this.getClass(), "getPaidPaymentsCount", count);
            OSMLogger.logPerformance(this.getClass(), "getPaidPaymentsCount", startTime, System.currentTimeMillis());

            return count;

        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error getting paid payments count for supplier: " + supplierId, e);
            throw e;
        }
    }

    public long getUnpaidPaymentsCount(UUID supplierId) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "getUnpaidPaymentsCount", supplierId);

        try {
            long count = deliveryRepository.countUnpaidDeliveriesBySupplierId(supplierId);

            OSMLogger.logDataAccess(this.getClass(), "UNPAID_PAYMENTS_COUNT", "Supplier");
            OSMLogger.logBusinessEvent(this.getClass(), "SUPPLIER_UNPAID_PAYMENTS_QUERIED",
                    "Found " + count + " unpaid payments for supplier: " + supplierId);
            OSMLogger.logMethodExit(this.getClass(), "getUnpaidPaymentsCount", count);
            OSMLogger.logPerformance(this.getClass(), "getUnpaidPaymentsCount", startTime, System.currentTimeMillis());

            return count;

        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error getting unpaid payments count for supplier: " + supplierId, e);
            throw e;
        }
    }

    @Override
    public Set<Action> actionsMapping(Supplier supplier) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "actionsMapping", supplier);

        try {
            Set<Action> actions = new HashSet<>();
            actions.addAll(Set.of(Action.UPDATE, Action.DELETE, Action.READ, Action.DETAIL));

            OSMLogger.logMethodExit(this.getClass(), "actionsMapping", "Actions: " + actions);
            OSMLogger.logPerformance(this.getClass(), "actionsMapping", startTime, System.currentTimeMillis());

            return actions;

        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error mapping actions for supplier: " + supplier.getId(), e);
            throw e;
        }
    }
}
