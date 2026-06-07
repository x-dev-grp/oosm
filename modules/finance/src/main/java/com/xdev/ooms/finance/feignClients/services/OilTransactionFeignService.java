package com.xdev.ooms.finance.feignClients.services;

import com.xdev.ooms.sharedkernel.communicator.models.common.dtos.apiDTOs.ApiSingleResponse;
import com.xdev.ooms.sharedkernel.communicator.models.shared.OilTransactionDTO;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class OilTransactionFeignService {

    public CompletableFuture<ApiSingleResponse<OilTransactionDTO>> create(OilTransactionDTO request) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException(
                "Oil transaction creation must be wired through a local production port in the modular monolith"));
    }
}
