package com.xdev.ooms.hr.legal.controller;

import com.xdev.ooms.hr.legal.dto.TaxConfigurationDto;
import com.xdev.ooms.hr.legal.entity.TaxConfiguration;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/tax-configurations")
public class TaxConfigurationController extends BaseControllerImpl<TaxConfiguration, TaxConfigurationDto, TaxConfigurationDto> {

    public TaxConfigurationController(
            BaseService<TaxConfiguration, TaxConfigurationDto, TaxConfigurationDto> baseService,
            ModelMapper modelMapper
    ) {
        super(baseService, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "TAXCONFIGURATION";
    }
}
