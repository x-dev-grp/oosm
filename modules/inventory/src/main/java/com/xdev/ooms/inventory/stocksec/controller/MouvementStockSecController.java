package com.xdev.ooms.inventory.stocksec.controller;

import com.xdev.ooms.inventory.stocksec.dto.MouvementStockSecDto;
import com.xdev.ooms.inventory.stocksec.entity.MouvementStockSec;
import com.xdev.ooms.inventory.stocksec.service.MouvementStockSecService;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventaire/mouvements-stocks")
public class MouvementStockSecController
        extends BaseControllerImpl<MouvementStockSec, MouvementStockSecDto, MouvementStockSecDto> {

    public MouvementStockSecController(MouvementStockSecService service, ModelMapper modelMapper) {
        super(service, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "MOUVEMENTSTOCKSEC";
    }
}
