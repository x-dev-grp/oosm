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

    @Override
    protected String getEntityType() {
        return "BANKACCOUNT";
    }

    @Override
    protected String getLabel(BankAccount entity) {
        if (entity == null) {
            return "Bank account";
        }
        if (entity.getBankName() != null && !entity.getBankName().isBlank()) {
            return entity.getBankName();
        }
        if (entity.getIban() != null && !entity.getIban().isBlank()) {
            return entity.getIban();
        }
        return entity.getId() != null ? "Bank account " + entity.getId() : "Bank account";
    }

    @Override
    protected String getStatus(BankAccount entity) {
        if (entity == null) {
            return "UNKNOWN";
        }
        return Boolean.TRUE.equals(entity.getActive()) ? "ACTIVE" : "INACTIVE";
    }

    @Override
    protected String getMobileRoute() {
        return "/finance/bank-accounts";
    }

    @Override
    protected String getWebRoute(BankAccount entity) {
        if (entity == null || entity.getId() == null) {
            return "/finance/bank-accounts";
        }
        return "/finance/bank-accounts/" + entity.getId() + "/view";
    }
}
