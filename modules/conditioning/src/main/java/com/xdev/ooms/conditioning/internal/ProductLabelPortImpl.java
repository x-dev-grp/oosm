package com.xdev.ooms.conditioning.internal;

import com.xdev.ooms.conditioning.label.service.LabelContentService;
import com.xdev.ooms.sharedkernel.communicator.models.shared.LabelContentDto;
import com.xdev.ooms.sharedkernel.ports.ProductLabelPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProductLabelPortImpl implements ProductLabelPort {

    private final LabelContentService labelContentService;

    public ProductLabelPortImpl(LabelContentService labelContentService) {
        this.labelContentService = labelContentService;
    }

    @Override
    public List<LabelContentDto> findLabelsByProductId(UUID productId) {
        return labelContentService.getByProductId(productId);
    }

    @Override
    public void linkLabelsToProduct(UUID productId, List<UUID> labelIds) {
        labelContentService.linkLabelsToProduct(productId, labelIds);
    }
}
