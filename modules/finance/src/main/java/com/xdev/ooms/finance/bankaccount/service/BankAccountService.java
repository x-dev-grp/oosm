package com.xdev.ooms.finance.bankaccount.service;


import com.xdev.ooms.finance.bankaccount.dto.BankAccountDto;
import com.xdev.ooms.finance.bankaccount.entity.BankAccount;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;


@Service
public class BankAccountService extends BaseServiceImpl<BankAccount, BankAccountDto, BankAccountDto> {

    public BankAccountService(BaseRepository<BankAccount> repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
    }



}
