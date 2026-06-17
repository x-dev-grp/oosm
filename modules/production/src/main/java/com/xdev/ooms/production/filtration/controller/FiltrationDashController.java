package com.xdev.ooms.production.filtration.controller;

import com.xdev.ooms.production.filtration.dto.FiltrationDashDto;
import com.xdev.ooms.production.filtration.entity.FiltrationOperation;
import com.xdev.ooms.production.filtration.service.FiltrationDashService;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/production/filtration-operations")
public class FiltrationDashController
        extends BaseControllerImpl<FiltrationOperation, FiltrationDashDto, FiltrationDashDto> {

    public FiltrationDashController(FiltrationDashService service, ModelMapper modelMapper) {
        super(service, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "FILTRATIONOPERATION";
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
}
