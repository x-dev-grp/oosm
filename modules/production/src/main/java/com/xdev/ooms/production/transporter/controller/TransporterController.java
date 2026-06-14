package com.xdev.ooms.production.transporter.controller;

import com.xdev.ooms.production.transporter.dto.TransporterDTO;




import com.xdev.ooms.production.transporter.entity.Transporter;
import com.xdev.ooms.production.transporter.service.TransporterService;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/production/transporter")

public class TransporterController extends BaseControllerImpl<Transporter, TransporterDTO, TransporterDTO> {

    private final TransporterService transporterDTOService;

    public TransporterController(BaseService<Transporter, TransporterDTO, TransporterDTO> baseService, ModelMapper modelMapper, TransporterService transporterDTOService) {
        super(baseService, modelMapper);
        this.transporterDTOService = transporterDTOService;
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
    @Override
    protected String getResourceName() {
        return "Transporter".toUpperCase();
    }
}
