package com.xdev.ooms.documents.form.mapper;

import com.xdev.ooms.documents.form.dto.FormPdfConfigDto;
import com.xdev.ooms.production.qualitycontrol.entity.QualityControlResult;
import com.xdev.ooms.production.qualitycontrol.entity.QualityControlRule;
import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;
import com.xdev.ooms.sharedkernel.Enum.DeliveryType;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QualityControlPdfConfigMapperTest {

    private final QualityControlPdfConfigMapper mapper = new QualityControlPdfConfigMapper();

    @Test
    void printsRuleNameInsteadOfDescription() {
        QualityControlRule rule = new QualityControlRule();
        rule.setRuleName("Acidité");
        rule.setDescription("Technical regulatory description");

        QualityControlResult result = new QualityControlResult();
        result.setRule(rule);
        result.setMeasuredValue("0.4");

        UnifiedDelivery delivery = new UnifiedDelivery();
        delivery.setDeliveryType(DeliveryType.OIL);
        delivery.setQualityControlResults(Set.of(result));

        FormPdfConfigDto config = mapper.map(delivery);

        assertEquals("Acidité", config.getFields().getFirst().getLabel());
        assertEquals("0.4", config.getFields().getFirst().getValue());
    }
}
