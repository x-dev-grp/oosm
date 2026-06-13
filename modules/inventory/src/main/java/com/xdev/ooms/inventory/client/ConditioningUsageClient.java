package com.xdev.ooms.inventory.client;

import com.xdev.ooms.inventory.dto.InventoryUsageBlockersDto;

import java.util.UUID;

public interface ConditioningUsageClient {

    InventoryUsageBlockersDto getArticleUsageBlockers(UUID articleId);

    InventoryUsageBlockersDto getProductUsageBlockers(UUID productId);
}
