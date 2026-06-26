package com.xdev.ooms.finance.bankaccount.controller;


import com.xdev.ooms.finance.bankaccount.dto.BankAccountDto;
import com.xdev.ooms.finance.bankaccount.entity.BankAccount;
 import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.models.OOSMModule;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance/banks")

public class BankAccountController extends BaseControllerImpl<BankAccount, BankAccountDto, BankAccountDto> {


    public BankAccountController(BaseService<BankAccount, BankAccountDto, BankAccountDto> baseService, ModelMapper modelMapper) {
        super(baseService, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "BANKACCOUNT" ;
    }


    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
}
