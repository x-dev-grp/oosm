package com.xdev.ooms.production.internal;

import com.xdev.ooms.sharedkernel.basetype.dto.BaseTypeDto;
import com.xdev.ooms.sharedkernel.basetype.entity.BaseType;
import com.xdev.ooms.production.oiltransaction.dto.OilTransactionDTO;
import com.xdev.ooms.production.oiltransaction.entity.OilTransaction;
import com.xdev.ooms.production.storageunit.dto.StorageUnitDto;
import com.xdev.ooms.production.storageunit.entity.StorageUnit;
import com.xdev.ooms.production.supplier.dto.SupplierDto;
import com.xdev.ooms.production.supplier.entity.Supplier;
import com.xdev.ooms.production.unifieddelivery.dto.UnifiedDeliveryDTO;
import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;

import com.xdev.ooms.sharedkernel.Enum.Olive_Oil_Type;
import com.xdev.ooms.sharedkernel.communicator.models.common.dtos.BaseDto;
import com.xdev.ooms.sharedkernel.mapper.FinanceSharedDtoMapper;
import com.xdev.ooms.sharedkernel.mapper.ProductionSharedDtoMapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Maps between shared-kernel production DTOs (cross-module contract) and production module REST DTOs.
 */
@Component
public class ProductionModuleDtoMapper {

    private final ModelMapper modelMapper;

    public ProductionModuleDtoMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public com.xdev.ooms.production.oiltransaction.dto.OilTransactionDTO fromShared(
            com.xdev.ooms.sharedkernel.communicator.models.shared.OilTransactionDTO source) {
        if (source == null) {
            return null;
        }
        com.xdev.ooms.production.oiltransaction.dto.OilTransactionDTO target =
                new com.xdev.ooms.production.oiltransaction.dto.OilTransactionDTO();
        copySharedBaseToModule(source, target);
        target.setStorageUnitDestination(toModuleStorageUnit(source.getStorageUnitDestination()));
        target.setStorageUnitSource(toModuleStorageUnit(source.getStorageUnitSource()));
        target.setQualityGrade(source.getQualityGrade());
        target.setQuantityKg(source.getQuantityKg());
        target.setUnitPrice(source.getUnitPrice());
        target.setTotalPrice(source.getTotalPrice());
        target.setTransactionType(source.getTransactionType());
        target.setTransactionState(source.getTransactionState());
        target.setReception(toModuleUnifiedDelivery(source.getReception()));
        target.setOilType(toModuleOilType(source.getOilType()));
        return target;
    }

    public com.xdev.ooms.sharedkernel.communicator.models.shared.OilTransactionDTO toShared(
            com.xdev.ooms.production.oiltransaction.dto.OilTransactionDTO source) {
        if (source == null) {
            return null;
        }
        com.xdev.ooms.sharedkernel.communicator.models.shared.OilTransactionDTO target =
                new com.xdev.ooms.sharedkernel.communicator.models.shared.OilTransactionDTO();
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
        return target;
    }

    private com.xdev.ooms.production.storageunit.dto.StorageUnitDto toModuleStorageUnit(
            com.xdev.ooms.sharedkernel.communicator.models.shared.StorageUnitDto source) {
        if (source == null) {
            return null;
        }
        com.xdev.ooms.production.storageunit.dto.StorageUnitDto target = new com.xdev.ooms.production.storageunit.dto.StorageUnitDto();
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

    private com.xdev.ooms.sharedkernel.communicator.models.shared.StorageUnitDto toSharedStorageUnit(
            com.xdev.ooms.production.storageunit.dto.StorageUnitDto source) {
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

    private com.xdev.ooms.production.unifieddelivery.dto.UnifiedDeliveryDTO toModuleUnifiedDelivery(
            com.xdev.ooms.sharedkernel.communicator.models.shared.UnifiedDeliveryDTO source) {
        if (source == null) {
            return null;
        }
        com.xdev.ooms.production.unifieddelivery.dto.UnifiedDeliveryDTO target =
                new com.xdev.ooms.production.unifieddelivery.dto.UnifiedDeliveryDTO();
        copySharedBaseToModule(source, target);
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
        target.setRegion(toModuleBaseType(source.getRegion()));
        target.setPoidsBrute(source.getPoidsBrute());
        target.setPoidsNet(source.getPoidsNet());
        target.setMatriculeCamion(source.getMatriculeCamion());
        target.setEtatCamion(source.getEtatCamion());
        target.setSupplier(toModuleSupplier(source.getSupplier()));
        target.setGlobalLotNumber(source.getGlobalLotNumber());
        target.setOilVariety(toModuleBaseType(source.getOilVariety()));
        target.setOilQuantity(source.getOilQuantity());
        target.setUnitPrice(source.getUnitPrice());
        target.setPrice(source.getPrice());
        target.setPaidAmount(source.getPaidAmount());
        target.setUnpaidAmount(source.getUnpaidAmount());
        target.setOilType(toModuleOilType(source.getOilType()));
        target.setTrtDate(source.getTrtDate());
        target.setOliveVariety(toModuleBaseType(source.getOliveVariety()));
        target.setSackCount(source.getSackCount());
        target.setOliveType(toModuleOilType(source.getOliveType()));
        target.setStatus(source.getStatus());
        target.setRendement(source.getRendement());
        target.setOliveQuantity(source.getOliveQuantity());
        target.setParcel(toModuleBaseTypeFromName(source.getParcel()));
        target.setStorageUnit(toModuleStorageUnit(source.getStorageUnit()));
        return target;
    }

    private com.xdev.ooms.sharedkernel.communicator.models.shared.UnifiedDeliveryDTO toSharedUnifiedDelivery(
            com.xdev.ooms.production.unifieddelivery.dto.UnifiedDeliveryDTO source) {
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

    private BaseTypeDto copyBaseType(BaseTypeDto source) {
        if (source == null) {
            return null;
        }
        return modelMapper.map(source, BaseTypeDto.class);
    }

    private BaseTypeDto toModuleBaseType(BaseTypeDto source) {
        return copyBaseType(source);
    }

    private BaseTypeDto toSharedBaseType(BaseTypeDto source) {
        return copyBaseType(source);
    }

    private com.xdev.ooms.production.supplier.dto.SupplierDto toModuleSupplier(
            com.xdev.ooms.sharedkernel.communicator.models.shared.SupplierDto source) {
        if (source == null) {
            return null;
        }
        com.xdev.ooms.production.supplier.dto.SupplierDto target = new com.xdev.ooms.production.supplier.dto.SupplierDto();
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

    private com.xdev.ooms.sharedkernel.communicator.models.shared.SupplierDto toSharedSupplier(
            com.xdev.ooms.production.supplier.dto.SupplierDto source) {
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

    private com.xdev.ooms.sharedkernel.basetype.dto.BaseTypeDto toModuleBaseTypeFromName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        com.xdev.ooms.sharedkernel.basetype.dto.BaseTypeDto target = new com.xdev.ooms.sharedkernel.basetype.dto.BaseTypeDto();
        target.setName(name);
        return target;
    }

    private String toSharedParcelName(com.xdev.ooms.sharedkernel.basetype.dto.BaseTypeDto source) {
        return source != null ? source.getName() : null;
    }

    private Olive_Oil_Type toModuleOilType(
            com.xdev.ooms.sharedkernel.basetype.dto.BaseTypeDto source) {
        if (source == null) {
            return null;
        }
        if (source.getName() != null && !source.getName().isBlank()) {
            return Olive_Oil_Type.from(source.getName());
        }
        if (source.getExternalId() != null) {
            return Olive_Oil_Type.from(source.getExternalId().toString());
        }
        return null;
    }

    private com.xdev.ooms.sharedkernel.basetype.dto.BaseTypeDto toSharedOilType(
            Olive_Oil_Type source) {
        return toSharedBaseTypeType(source);
    }

    private com.xdev.ooms.sharedkernel.basetype.dto.BaseTypeDto toSharedBaseTypeType(
            Olive_Oil_Type source) {
        if (source == null) {
            return null;
        }
        com.xdev.ooms.sharedkernel.basetype.dto.BaseTypeDto target =
                new com.xdev.ooms.sharedkernel.basetype.dto.BaseTypeDto();
        target.setName(source.name());
        return target;
    }

    private void copySharedBaseToModule(
            BaseDto source,
            com.xdev.ooms.sharedkernel.dtos.BaseDto<?> target) {
        target.setId(source.getId());
        target.setExternalId(source.getExternalId());
        target.setDeleted(source.getDeleted());
    }

    private void copyModuleBaseToShared(
            com.xdev.ooms.sharedkernel.dtos.BaseDto<?> source,
            BaseDto target) {
        target.setId(source.getId());
        target.setExternalId(source.getExternalId());
        target.setDeleted(source.getDeleted());
    }
}
