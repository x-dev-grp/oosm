package com.xdev.ooms.finance.config;

import com.xdev.ooms.finance.financialtransaction.dto.FinancialTransactionDto;
import com.xdev.ooms.finance.financialtransaction.entity.FinancialTransaction;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FinanceMapperConfiguration {

    public FinanceMapperConfiguration(ModelMapper modelMapper) {
        modelMapper.typeMap(FinancialTransactionDto.class, FinancialTransaction.class)
                .addMappings(mapper -> {
                    mapper.skip(FinancialTransaction::setSupplier);
                    mapper.skip(FinancialTransaction::setBankAccount);
                    mapper.skip(FinancialTransaction::setExpense);
                });

        modelMapper.typeMap(FinancialTransaction.class, FinancialTransactionDto.class)
                .addMappings(mapper -> mapper.skip(FinancialTransactionDto::setSyncProductionState));
    }
}
