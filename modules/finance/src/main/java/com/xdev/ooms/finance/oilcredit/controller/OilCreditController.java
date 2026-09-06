package com.xdev.ooms.finance.oilcredit.controller;


import com.xdev.ooms.finance.oilcredit.dto.OilCreditDto;
import com.xdev.ooms.finance.oilcredit.entity.OilCredit;
import com.xdev.ooms.finance.oilcredit.service.OilCreditService;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/finance/oil-credit")

public class OilCreditController extends BaseControllerImpl<OilCredit, OilCreditDto, OilCreditDto>
 {
    private final OilCreditService oilCreditService;

    public OilCreditController(BaseService<OilCredit, OilCreditDto, OilCreditDto> baseService, ModelMapper modelMapper, OilCreditService oilCreditService) {
        super(baseService, modelMapper);
        this.oilCreditService = oilCreditService;
    }

     @PutMapping("/{transactionId}/approve")
     public ResponseEntity<Void> approveOilCredit(@PathVariable UUID transactionId) {
        try {
            oilCreditService.approuveOilCredit(transactionId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
             return ResponseEntity.notFound().build();
        }

     }


     @Override
     protected String getResourceName() {
         return "OilCredit".toUpperCase();
     }
}
