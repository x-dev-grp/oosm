package com.xdev.ooms.production.config;

import com.xdev.ooms.production.qualitycontrol.dto.QualityControlResultDto;
import com.xdev.ooms.production.qualitycontrol.entity.QualityControlResult;
import com.xdev.ooms.production.storageunit.dto.StorageUnitDto;
import com.xdev.ooms.production.storageunit.entity.StorageUnit;
import com.xdev.ooms.production.unifieddelivery.dto.UnifiedDeliveryDTO;
import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProductionMapperConfiguration {

    public ProductionMapperConfiguration(ModelMapper modelMapper) {
        modelMapper.getConfiguration().setAmbiguityIgnored(true);

        modelMapper.typeMap(StorageUnit.class, StorageUnitDto.class)
                .addMappings(mapper -> mapper.skip(StorageUnitDto::setSupplier));

        modelMapper.typeMap(QualityControlResult.class, QualityControlResultDto.class)
                .addMappings(mapper -> mapper.map(
                        src -> src.getDelivery() != null ? src.getDelivery().getId() : null,
                        QualityControlResultDto::setDeliveryId));

        modelMapper.typeMap(UnifiedDelivery.class, UnifiedDeliveryDTO.class)
                .addMappings(mapper -> mapper.map(
                        UnifiedDelivery::getQrHex,
                        UnifiedDeliveryDTO::setPublicCode));
    }
}
