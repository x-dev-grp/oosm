package com.xdev.ooms.production.oilcontainersale.controller;

import com.xdev.ooms.production.oilcontainer.entity.OilContainer;
import com.xdev.ooms.production.oilcontainersale.dto.OilContainerSaleDto;



import com.xdev.ooms.production.oilcontainersale.entity.OilContainerSale;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/production/oil_container_sale")
public class OilContainerSalesController extends BaseControllerImpl<OilContainerSale, OilContainerSaleDto, OilContainerSaleDto> {
    public static final String X_TENANT_ID = "X-Tenant-ID";


    public OilContainerSalesController(BaseService<OilContainerSale, OilContainerSaleDto, OilContainerSaleDto> baseService, ModelMapper modelMapper) {
        super(baseService, modelMapper);

    }
@Override
    protected String getResourceName() {
        return "OilContainer".toUpperCase();
    }
}

