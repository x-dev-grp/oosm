package com.xdev.ooms.documents.commercial;

import com.xdev.ooms.production.parameter.entity.Parameter;
import com.xdev.ooms.production.parameter.service.ParameterService;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PurchaseVatParameterReader {

    public static final String AUTO_CALC_CODE = "TVA_ACHAT_AUTO_CALC";

    private final ParameterService parameterService;

    public PurchaseVatParameterReader(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public boolean isAutoCalcEnabled() {
        return isAutoCalcEnabled(TenantContext.getCurrentTenant());
    }

    public boolean isAutoCalcEnabled(UUID tenantId) {
        if (tenantId == null) {
            return true;
        }
        try {
            Parameter parameter = parameterService.getByCode(AUTO_CALC_CODE, tenantId);
            return Boolean.parseBoolean(parameter.getValue());
        } catch (RuntimeException ex) {
            return true;
        }
    }
}
