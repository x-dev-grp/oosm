package com.xdev.ooms.production.feignClients.services;

import com.xdev.ooms.sharedkernel.communicator.models.common.dtos.apiDTOs.ApiSingleResponse;
import com.xdev.ooms.sharedkernel.communicator.models.shared.FinancialTransactionDto;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class FinancialTransactionFeignService {

    public CompletableFuture<ApiSingleResponse<FinancialTransactionDto>> create(FinancialTransactionDto request) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException(
                "Financial transaction creation must be wired through a local finance port in the modular monolith"));
    }
}
