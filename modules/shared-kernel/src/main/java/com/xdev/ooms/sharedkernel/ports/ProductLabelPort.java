package com.xdev.ooms.sharedkernel.ports;

import com.xdev.ooms.sharedkernel.communicator.models.shared.LabelContentDto;

import java.util.List;
import java.util.UUID;

/**
 * Cross-module port: inventory links final products to conditioning label tickets.
 */
public interface ProductLabelPort {

    List<LabelContentDto> findLabelsByProductId(UUID productId);

    void linkLabelsToProduct(UUID productId, List<UUID> labelIds);
}
