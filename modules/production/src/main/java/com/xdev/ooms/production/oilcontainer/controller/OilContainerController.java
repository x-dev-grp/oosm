package com.xdev.ooms.production.oilcontainer.controller;

import com.xdev.ooms.production.oilcontainer.dto.OilContainerDTO;



import com.xdev.ooms.production.oilcontainer.entity.OilContainer;
import com.xdev.ooms.production.oilcontainer.service.OilContainerService;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/production/oil_container")
public class OilContainerController extends BaseControllerImpl<OilContainer, OilContainerDTO, OilContainerDTO> {
    public static final String X_TENANT_ID = "X-Tenant-ID";
    private final OilContainerService oilContainerService;

    public OilContainerController(BaseService<OilContainer, OilContainerDTO, OilContainerDTO> baseService, ModelMapper modelMapper, OilContainerService oilContainerService) {
        super(baseService, modelMapper);
        this.oilContainerService = oilContainerService;
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
    @Override
    protected String getResourceName() {
        return "OilContainer".toUpperCase();
    }
}

