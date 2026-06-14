package com.xdev.ooms.sharedkernel.mapper;

import com.xdev.ooms.sharedkernel.communicator.models.common.dtos.BaseDto;
import com.xdev.ooms.sharedkernel.basetype.dto.BaseTypeDto;
import com.xdev.ooms.sharedkernel.communicator.models.shared.OilTransactionDTO;
import com.xdev.ooms.sharedkernel.communicator.models.shared.StorageUnitDto;
import com.xdev.ooms.sharedkernel.communicator.models.shared.UnifiedDeliveryDTO;
import org.modelmapper.ModelMapper;

/**
 * Canonical copy utilities for production-related shared-kernel DTOs.
 * Cross-module contracts should use these types; module REST DTOs map via
 * {@code ProductionModuleDtoMapper} and {@code ProductionModuleDtoCopy}.
 */
public final class ProductionSharedDtoMapper {

    private static final ModelMapper MODEL_MAPPER = new ModelMapper();

    private ProductionSharedDtoMapper() {
    }

    public static void copyBaseFields(BaseDto source, BaseDto target) {
        if (source == null || target == null) {
            return;
        }
        target.setId(source.getId());
        target.setExternalId(source.getExternalId());
        target.setDeleted(source.getDeleted());
        target.setActions(source.getActions());
    }

    public static void copyBaseFields(
            com.xdev.ooms.sharedkernel.dtos.BaseDto<?> source,
            com.xdev.ooms.sharedkernel.dtos.BaseDto<?> target) {
        if (source == null || target == null) {
            return;
        }
        target.setId(source.getId());
        target.setExternalId(source.getExternalId());
        target.setDeleted(source.getDeleted());
        target.setActions(source.getActions());
    }

    public static OilTransactionDTO copyOilTransaction(OilTransactionDTO source) {
        if (source == null) {
            return null;
        }
        OilTransactionDTO target = new OilTransactionDTO();
        copyOilTransactionFields(source, target);
        return target;
    }

    public static void copyOilTransactionFields(OilTransactionDTO source, OilTransactionDTO target) {
        if (source == null || target == null) {
            return;
        }
        copyBaseFields(source, target);
        target.setStorageUnitDestination(copyStorageUnit(source.getStorageUnitDestination()));
        target.setStorageUnitSource(copyStorageUnit(source.getStorageUnitSource()));
        target.setQualityGrade(source.getQualityGrade());
        target.setQuantityKg(source.getQuantityKg());
        target.setUnitPrice(source.getUnitPrice());
        target.setTotalPrice(source.getTotalPrice());
        target.setTransactionType(source.getTransactionType());
        target.setTransactionState(source.getTransactionState());
        target.setReception(copyUnifiedDelivery(source.getReception()));
        target.setOilType(copyBaseType(source.getOilType()));
    }

    public static StorageUnitDto copyStorageUnit(StorageUnitDto source) {
        if (source == null) {
            return null;
        }
        StorageUnitDto target = new StorageUnitDto();
        copyStorageUnitFields(source, target);
        return target;
    }

    public static void copyStorageUnitFields(StorageUnitDto source, StorageUnitDto target) {
        if (source == null || target == null) {
            return;
        }
        copyBaseFields(source, target);
        target.setName(source.getName());
        target.setLocation(source.getLocation());
        target.setDescription(source.getDescription());
        target.setMaxCapacity(source.getMaxCapacity());
        target.setCurrentVolume(source.getCurrentVolume());
        target.setNextMaintenanceDate(source.getNextMaintenanceDate());
        target.setLastInspectionDate(source.getLastInspectionDate());
        target.setPaidStorage(source.isPaidStorage());
        target.setMonthlyRentalPrice(source.getMonthlyRentalPrice());
        target.setAvgCost(source.getAvgCost());
        target.setTotalCost(source.getTotalCost());
        target.setOilType(copyBaseType(source.getOilType()));
        target.setStatus(source.getStatus());
        target.setLastFillDate(source.getLastFillDate());
        target.setLastEmptyDate(source.getLastEmptyDate());
        target.setSupplier(FinanceSharedDtoMapper.copySupplier(source.getSupplier()));
        target.setQualityGrade(source.getQualityGrade());
        target.setLotNumber(source.getLotNumber());
        target.setFilteredOil(source.getFilteredOil());
    }

    public static BaseTypeDto copyBaseType(BaseTypeDto source) {
        if (source == null) {
            return null;
        }
        return MODEL_MAPPER.map(source, BaseTypeDto.class);
    }

    public static void copyBaseTypeFields(BaseTypeDto source, BaseTypeDto target) {
        if (source == null || target == null) {
            return;
        }
        MODEL_MAPPER.map(source, target);
    }

    public static UnifiedDeliveryDTO copyUnifiedDelivery(UnifiedDeliveryDTO source) {
        if (source == null) {
            return null;
        }
        UnifiedDeliveryDTO target = new UnifiedDeliveryDTO();
        copyUnifiedDeliveryFields(source, target);
        return target;
    }

    public static void copyUnifiedDeliveryFields(UnifiedDeliveryDTO source, UnifiedDeliveryDTO target) {
        if (source == null || target == null) {
            return;
        }
        copyBaseFields(source, target);
        target.setDeliveryNumber(source.getDeliveryNumber());
        target.setCategoryOliveOil(source.getCategoryOliveOil());
        target.setDeliveryType(source.getDeliveryType());
        target.setOperationType(source.getOperationType());
        target.setPaid(source.getPaid());
        target.setTrtDuration(source.getTrtDuration());
        target.setPoidsCamionVide(source.getPoidsCamionVide());
        target.setDescription(source.getDescription());
        target.setLotNumber(source.getLotNumber());
        target.setLotOliveNumber(source.getLotOliveNumber());
        target.setDeliveryDate(source.getDeliveryDate());
        target.setRegion(copyBaseType(source.getRegion()));
        target.setPoidsBrute(source.getPoidsBrute());
        target.setPoidsNet(source.getPoidsNet());
        target.setMatriculeCamion(source.getMatriculeCamion());
        target.setEtatCamion(source.getEtatCamion());
        target.setSupplier(FinanceSharedDtoMapper.copySupplier(source.getSupplier()));
        target.setGlobalLotNumber(source.getGlobalLotNumber());
        target.setOilVariety(copyBaseType(source.getOilVariety()));
        target.setOilQuantity(source.getOilQuantity());
        target.setUnitPrice(source.getUnitPrice());
        target.setPrice(source.getPrice());
        target.setPaidAmount(source.getPaidAmount());
        target.setUnpaidAmount(source.getUnpaidAmount());
        target.setOilType(copyBaseType(source.getOilType()));
        target.setTrtDate(source.getTrtDate());
        target.setOliveVariety(copyBaseType(source.getOliveVariety()));
        target.setSackCount(source.getSackCount());
        target.setOliveType(copyBaseType(source.getOliveType()));
        target.setStatus(source.getStatus());
        target.setRendement(source.getRendement());
        target.setOliveQuantity(source.getOliveQuantity());
        target.setParcel(source.getParcel());
        target.setStorageUnit(copyStorageUnit(source.getStorageUnit()));
    }
}
