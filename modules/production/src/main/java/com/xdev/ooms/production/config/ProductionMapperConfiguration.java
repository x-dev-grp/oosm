package com.xdev.ooms.production.config;

import com.xdev.ooms.production.oilsale.dto.OilSaleDTO;
import com.xdev.ooms.production.oilsale.entity.OilSale;
import com.xdev.ooms.production.storageunit.dto.StorageUnitDto;
import com.xdev.ooms.production.storageunit.entity.StorageUnit;
import com.xdev.ooms.production.storageunit.repository.StorageUnitRepo;
import com.xdev.ooms.production.supplier.dto.SupplierDto;
import com.xdev.ooms.production.supplier.entity.Supplier;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProductionMapperConfiguration {

    public ProductionMapperConfiguration(ModelMapper modelMapper, StorageUnitRepo storageUnitRepo) {
        modelMapper.getConfiguration().setAmbiguityIgnored(true);

        modelMapper.emptyTypeMap(StorageUnit.class, StorageUnitDto.class)
                .addMappings(mapper -> {
                    mapper.skip(StorageUnitDto::setSupplier);
                    mapper.skip(StorageUnitDto::setPublicCode);
                    mapper.skip(StorageUnitDto::setQrUrl);
                    mapper.skip(StorageUnitDto::setQrImageBase64);
                })
                .implicitMappings();

        modelMapper.emptyTypeMap(StorageUnitDto.class, StorageUnit.class)
                .addMappings(mapper -> mapper.skip(StorageUnit::setSupplier))
                .implicitMappings();

        modelMapper.emptyTypeMap(Supplier.class, SupplierDto.class)
                .addMappings(mapper -> {
                    mapper.map(Supplier::getName, SupplierDto::setName);
                    mapper.map(Supplier::getLastname, SupplierDto::setLastname);
                    mapper.map(Supplier::getFullName, SupplierDto::setFullName);
                    mapper.skip(SupplierDto::setStorageUnit);
                })
                .implicitMappings();

        modelMapper.emptyTypeMap(SupplierDto.class, Supplier.class)
                .addMappings(mapper -> {
                    mapper.map(SupplierDto::getName, Supplier::setName);
                    mapper.map(SupplierDto::getLastname, Supplier::setLastname);
                    mapper.skip(Supplier::setFullName);
                    mapper.skip(Supplier::setStorageUnit);
                })
                .implicitMappings();

        modelMapper.emptyTypeMap(Supplier.class, com.xdev.ooms.sharedkernel.communicator.models.shared.SupplierDto.class)
                .addMappings(mapper -> {
                    mapper.map(Supplier::getName, com.xdev.ooms.sharedkernel.communicator.models.shared.SupplierDto::setName);
                    mapper.map(Supplier::getLastname, com.xdev.ooms.sharedkernel.communicator.models.shared.SupplierDto::setLastname);
                    mapper.skip(com.xdev.ooms.sharedkernel.communicator.models.shared.SupplierDto::setStorageUnit);
                })
                .implicitMappings();

        modelMapper.emptyTypeMap(SupplierDto.class, com.xdev.ooms.sharedkernel.communicator.models.shared.SupplierDto.class)
                .addMappings(mapper -> {
                    mapper.map(SupplierDto::getName, com.xdev.ooms.sharedkernel.communicator.models.shared.SupplierDto::setName);
                    mapper.map(SupplierDto::getLastname, com.xdev.ooms.sharedkernel.communicator.models.shared.SupplierDto::setLastname);
                    mapper.skip(com.xdev.ooms.sharedkernel.communicator.models.shared.SupplierDto::setStorageUnit);
                })
                .implicitMappings();

        modelMapper.emptyTypeMap(com.xdev.ooms.sharedkernel.communicator.models.shared.SupplierDto.class, Supplier.class)
                .addMappings(mapper -> {
                    mapper.map(
                            com.xdev.ooms.sharedkernel.communicator.models.shared.SupplierDto::getName,
                            Supplier::setName);
                    mapper.map(
                            com.xdev.ooms.sharedkernel.communicator.models.shared.SupplierDto::getLastname,
                            Supplier::setLastname);
                    mapper.skip(Supplier::setFullName);
                    mapper.skip(Supplier::setStorageUnit);
                })
                .implicitMappings();

        modelMapper.emptyTypeMap(OilSaleDTO.class, OilSale.class)
                .addMappings(mapper -> {
                    mapper.skip(OilSale::setSupplier);
                    mapper.skip(OilSale::setStorageUnit);
                })
                .implicitMappings();

        modelMapper.emptyTypeMap(OilSale.class, OilSaleDTO.class)
                .addMappings(mapper -> mapper.skip(OilSaleDTO::setStorageUnit))
                .implicitMappings()
                .setPostConverter(context -> {
                    OilSale sale = context.getSource();
                    OilSaleDTO dto = context.getDestination();
                    if (sale.getStorageUnit() != null) {
                        storageUnitRepo.findByIdAndIsDeletedFalse(sale.getStorageUnit())
                                .ifPresent(unit -> dto.setStorageUnit(modelMapper.map(unit, StorageUnitDto.class)));
                    }
                    return dto;
                });
    }
}
