package com.xdev.ooms.conditioning.internal;

import com.xdev.ooms.conditioning.inventoryusage.service.InventoryUsageService;
import com.xdev.ooms.sharedkernel.communicator.models.shared.InventoryUsageBlockersDto;
import com.xdev.ooms.sharedkernel.ports.ConditioningUsagePort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ConditioningUsagePortImpl implements ConditioningUsagePort {

    private final InventoryUsageService inventoryUsageService;

    public ConditioningUsagePortImpl(InventoryUsageService inventoryUsageService) {
        this.inventoryUsageService = inventoryUsageService;
    }

    @Override
    public InventoryUsageBlockersDto getArticleUsageBlockers(UUID articleId) {
        return inventoryUsageService.getArticleUsageBlockers(articleId);
    }

    @Override
    public InventoryUsageBlockersDto getProductUsageBlockers(UUID productId) {
        return inventoryUsageService.getProductUsageBlockers(productId);
    }
}
