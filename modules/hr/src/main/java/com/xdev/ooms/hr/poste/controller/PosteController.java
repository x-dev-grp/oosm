package com.xdev.ooms.hr.poste.controller;

import com.xdev.ooms.hr.poste.dto.PosteDto;
import com.xdev.ooms.hr.poste.entity.Poste;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/postes")
public class PosteController extends BaseControllerImpl<Poste, PosteDto, PosteDto> {

    public PosteController(BaseService<Poste, PosteDto, PosteDto> baseService, ModelMapper modelMapper) {
        super(baseService, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "POSTE";
    }
}
