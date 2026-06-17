package com.xdev.ooms.production.qualitycontrol.listener;

import com.xdev.ooms.production.qualitycontrol.service.QualityControlProvisioningService;
import com.xdev.ooms.sharedkernel.events.TenantCreatedEvent;
import com.xdev.ooms.sharedkernel.utils.OSMLogger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TenantCreatedEventListener {

    private final QualityControlProvisioningService provisioningService;

    public TenantCreatedEventListener(QualityControlProvisioningService provisioningService) {
        this.provisioningService = provisioningService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTenantCreated(TenantCreatedEvent event) {
        if (event == null || event.tenantId() == null) {
            return;
        }

        try {
            int created = provisioningService.provisionDefaultRulesForTenant(event.tenantId());
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO,
                    "Provisioned {} Tunisia QC rules for tenant {}", created, event.tenantId());
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(),
                    "Failed to provision QC rules for tenant " + event.tenantId(), e);
        }
    }
}
