package com.xdev.ooms.production.internal;

import com.xdev.ooms.production.oiltransaction.dto.OilTransactionDTO;
import com.xdev.ooms.production.storageunit.dto.StorageUnitDto;
import com.xdev.ooms.production.supplier.dto.SupplierDto;
import com.xdev.ooms.production.unifieddelivery.dto.UnifiedDeliveryDTO;
import com.xdev.ooms.sharedkernel.Enum.Olive_Oil_Type;
import com.xdev.ooms.sharedkernel.basetype.dto.BaseTypeDto;
import com.xdev.ooms.sharedkernel.communicator.models.common.dtos.BaseDto;
import com.xdev.ooms.sharedkernel.mapper.FinanceSharedDtoMapper;
import com.xdev.ooms.sharedkernel.mapper.ProductionSharedDtoMapper;

/**
 * Module-side copy utilities for production REST DTOs.
 * Shared-kernel canonical copies live in {@link ProductionSharedDtoMapper}.
 */
final class ProductionModuleDtoCopy {

    private ProductionModuleDtoCopy() {
    }

    static void copyOilTransactionToShared(
            OilTransactionDTO source,
            com.xdev.ooms.sharedkernel.communicator.models.shared.OilTransactionDTO target) {
        copyModuleBaseToShared(source, target);
        target.setStorageUnitDestination(toSharedStorageUnit(source.getStorageUnitDestination()));
        target.setStorageUnitSource(toSharedStorageUnit(source.getStorageUnitSource()));
        target.setQualityGrade(source.getQualityGrade());
        target.setQuantityKg(source.getQuantityKg());
        target.setUnitPrice(source.getUnitPrice());
        target.setTotalPrice(source.getTotalPrice());
        target.setTransactionType(source.getTransactionType());
        target.setTransactionState(source.getTransactionState());
        target.setReception(toSharedUnifiedDelivery(source.getReception()));
        target.setOilType(toSharedOilType(source.getOilType()));
    }

    private static StorageUnitDto toModuleStorageUnit(
            com.xdev.ooms.sharedkernel.communicator.models.shared.StorageUnitDto source) {
        if (source == null) {
            return null;
        }
        StorageUnitDto target = new StorageUnitDto();
        copySharedBaseToModule(source, target);
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
        target.setOilVariety(toModuleBaseType(source.getOilType()));
        target.setStatus(source.getStatus());
        target.setLastFillDate(source.getLastFillDate());
        target.setLastEmptyDate(source.getLastEmptyDate());
        target.setSupplier(toModuleSupplier(source.getSupplier()));
        target.setQualityGrade(source.getQualityGrade());
        target.setLotNumber(source.getLotNumber());
        target.setFilteredOil(source.getFilteredOil());
        return target;
    }

    private static com.xdev.ooms.sharedkernel.communicator.models.shared.StorageUnitDto toSharedStorageUnit(
            StorageUnitDto source) {
        if (source == null) {
            return null;
        }
        com.xdev.ooms.sharedkernel.communicator.models.shared.StorageUnitDto target =
                new com.xdev.ooms.sharedkernel.communicator.models.shared.StorageUnitDto();
        copyModuleBaseToShared(source, target);
        target.setName(source.getName());
        target.setLocation(source.getLocation());
        target.setDescription(source.getDescription());
        target.setMaxCapacity(source.getMaxCapacity());
        target.setCurrentVolume(source.getCurrentVolume());
        target.setNextMaintenanceDate(source.getNextMaintenanceDate());
        target.setLastInspectionDate(source.getLastInspectionDate());
        target.setPaidStorage(Boolean.TRUE.equals(source.getPaidStorage()));
        if (source.getMonthlyRentalPrice() != null) {
            target.setMonthlyRentalPrice(source.getMonthlyRentalPrice());
        }
        target.setAvgCost(source.getAvgCost());
        target.setTotalCost(source.getTotalCost());
        target.setOilType(toSharedBaseType(source.getOilVariety()));
        target.setStatus(source.getStatus());
        target.setLastFillDate(source.getLastFillDate());
        target.setLastEmptyDate(source.getLastEmptyDate());
        target.setSupplier(FinanceSharedDtoMapper.copySupplier(toSharedSupplier(source.getSupplier())));
        target.setQualityGrade(source.getQualityGrade());
        target.setLotNumber(source.getLotNumber());
        target.setFilteredOil(source.getFilteredOil());
        return target;
    }

    private static com.xdev.ooms.sharedkernel.communicator.models.shared.UnifiedDeliveryDTO toSharedUnifiedDelivery(
            UnifiedDeliveryDTO source) {
        if (source == null) {
            return null;
        }
        com.xdev.ooms.sharedkernel.communicator.models.shared.UnifiedDeliveryDTO target =
                new com.xdev.ooms.sharedkernel.communicator.models.shared.UnifiedDeliveryDTO();
        copyModuleBaseToShared(source, target);
        target.setDeliveryNumber(source.getDeliveryNumber());
        target.setCategoryOliveOil(source.getCategoryOliveOil());
        target.setDeliveryType(source.getDeliveryType());
        target.setOperationType(source.getOperationType());
        target.setPaid(source.getPaid());
        target.setTrtDuration(source.getTrtDuration() != null ? source.getTrtDuration() : 0);
        target.setPoidsCamionVide(source.getPoidsCamionVide());
        target.setDescription(source.getDescription());
        target.setLotNumber(source.getLotNumber());
        target.setLotOliveNumber(source.getLotOliveNumber());
        target.setDeliveryDate(source.getDeliveryDate());
        target.setRegion(toSharedBaseType(source.getRegion()));
        target.setPoidsBrute(source.getPoidsBrute());
        target.setPoidsNet(source.getPoidsNet());
        target.setMatriculeCamion(source.getMatriculeCamion());
        target.setEtatCamion(source.getEtatCamion());
        target.setSupplier(toSharedSupplier(source.getSupplier()));
        target.setGlobalLotNumber(source.getGlobalLotNumber());
        target.setOilVariety(toSharedBaseType(source.getOilVariety()));
        target.setOilQuantity(source.getOilQuantity());
        target.setUnitPrice(source.getUnitPrice());
        target.setPrice(source.getPrice());
        target.setPaidAmount(source.getPaidAmount());
        target.setUnpaidAmount(source.getUnpaidAmount());
        target.setOilType(toSharedOilType(source.getOilType()));
        target.setTrtDate(source.getTrtDate());
        target.setOliveVariety(toSharedBaseType(source.getOliveVariety()));
        target.setSackCount(source.getSackCount());
        target.setOliveType(toSharedBaseTypeType(source.getOliveType()));
        target.setStatus(source.getStatus());
        target.setRendement(source.getRendement());
        target.setOliveQuantity(source.getOliveQuantity());
        target.setParcel(toSharedParcelName(source.getParcel()));
        target.setStorageUnit(toSharedStorageUnit(source.getStorageUnit()));
        return target;
    }

    private static BaseTypeDto copyBaseType(BaseTypeDto source) {
        return ProductionSharedDtoMapper.copyBaseType(source);
    }

    private static BaseTypeDto toModuleBaseType(BaseTypeDto source) {
        return copyBaseType(source);
    }

    private static BaseTypeDto toSharedBaseType(BaseTypeDto source) {
        return copyBaseType(source);
    }

    private static SupplierDto toModuleSupplier(
            com.xdev.ooms.sharedkernel.communicator.models.shared.SupplierDto source) {
        if (source == null) {
            return null;
        }
        SupplierDto target = new SupplierDto();
        copySharedBaseToModule(source, target);
        target.setGenericSupplierType(toModuleBaseType(source.getGenericSupplierType()));
        target.setHasStorage(source.getHasStorage());
        target.setName(source.getName());
        target.setLastname(source.getLastname());
        target.setPhone(source.getPhone());
        target.setEmail(source.getEmail());
        target.setAddress(source.getAddress());
        target.setRegion(toModuleBaseType(source.getRegion()));
        target.setRib(source.getRib());
        target.setBankName(source.getBankName());
        target.setMatriculeFiscal(source.getMatriculeFiscal());
        target.setStorageUnit(toModuleStorageUnit(source.getStorageUnit()));
        return target;
    }

    private static com.xdev.ooms.sharedkernel.communicator.models.shared.SupplierDto toSharedSupplier(
            SupplierDto source) {
        if (source == null) {
            return null;
        }
        com.xdev.ooms.sharedkernel.communicator.models.shared.SupplierDto target =
                new com.xdev.ooms.sharedkernel.communicator.models.shared.SupplierDto();
        copyModuleBaseToShared(source, target);
        target.setGenericSupplierType(toSharedBaseType(source.getGenericSupplierType()));
        target.setHasStorage(source.getHasStorage());
        target.setName(source.getName());
        target.setLastname(source.getLastname());
        target.setPhone(source.getPhone());
        target.setEmail(source.getEmail());
        target.setAddress(source.getAddress());
        target.setRegion(toSharedBaseType(source.getRegion()));
        target.setRib(source.getRib());
        target.setBankName(source.getBankName());
        target.setMatriculeFiscal(source.getMatriculeFiscal());
        target.setStorageUnit(toSharedStorageUnit(source.getStorageUnit()));
        return target;
    }

    private static BaseTypeDto toModuleBaseTypeFromName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        BaseTypeDto target = new BaseTypeDto();
        target.setName(name);
        return target;
    }

    private static String toSharedParcelName(BaseTypeDto source) {
        return source != null ? source.getName() : null;
    }

    private static Olive_Oil_Type toModuleOilType(
            BaseTypeDto source) {
        if (source == null) {
            return null;
        }
        if (source.getName() != null && !source.getName().isBlank()) {
            return Olive_Oil_Type.from(source.getName());
        }
        if (source.getId() != null) {
            return Olive_Oil_Type.from(source.getId().toString());
        }
        return null;
    }

    private static BaseTypeDto toSharedOilType(
            Olive_Oil_Type source) {
        return toSharedBaseTypeType(source);
    }

    private static BaseTypeDto toSharedBaseTypeType(
            Olive_Oil_Type source) {
        if (source == null) {
            return null;
        }
        BaseTypeDto target =
                new BaseTypeDto();
        target.setName(source.name());
        return target;
    }

    private static void copySharedBaseToModule(
            BaseDto source,
            com.xdev.ooms.sharedkernel.dtos.BaseDto<?> target) {
        target.setId(source.getId());
        target.setDeleted(source.getDeleted());
    }

    private static void copyModuleBaseToShared(
            com.xdev.ooms.sharedkernel.dtos.BaseDto<?> source,
            BaseDto target) {
        target.setId(source.getId());
        target.setDeleted(source.getDeleted());
    }
}
