package com.xdev.ooms.production.feignClients.services;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class OilCeditFeignService {

    public CompletableFuture<ResponseEntity<Void>> approveOilCredit(UUID id) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException(
                "Oil credit approval must be wired through a local finance port in the modular monolith"));
    }
}
