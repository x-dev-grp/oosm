package com.xdev.ooms.sharedkernel.ports;

import com.xdev.ooms.sharedkernel.communicator.models.shared.InventoryUsageBlockersDto;

import java.util.UUID;

/**
 * Cross-module port: inventory checks conditioning usage before delete.
 */
public interface ConditioningUsagePort {

    InventoryUsageBlockersDto getArticleUsageBlockers(UUID articleId);

    InventoryUsageBlockersDto getProductUsageBlockers(UUID productId);
}
