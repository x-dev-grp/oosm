package com.xdev.ooms.hr.legal.controller;

import com.xdev.ooms.hr.legal.dto.TaxBracketDto;
import com.xdev.ooms.hr.legal.entity.TaxBracket;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/tax-brackets")
public class TaxBracketController extends BaseControllerImpl<TaxBracket, TaxBracketDto, TaxBracketDto> {

    public TaxBracketController(
            BaseService<TaxBracket, TaxBracketDto, TaxBracketDto> baseService,
            ModelMapper modelMapper
    ) {
        super(baseService, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "TAXBRACKET";
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
}
