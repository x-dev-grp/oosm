package com.xdev.ooms.security.tenantmodule;

import com.xdev.ooms.sharedkernel.models.OOSMModule;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TenantModulePathResolverTest {

    @Test
    void conditioningOwnsStockOperationsAndOilFiltration() {
        assertRequiredModule("/api/inventaire/stocks", OOSMModule.CONDITIONING);
        assertRequiredModule("/api/inventaire/stocks/by-location", OOSMModule.CONDITIONING);
        assertRequiredModule("/api/inventaire/mouvements-stocks?page=0", OOSMModule.CONDITIONING);
        assertRequiredModule("/api/inventaire/emplacements/42", OOSMModule.CONDITIONING);
        assertRequiredModule("/api/production/filtration-operations", OOSMModule.CONDITIONING);
        assertRequiredModule("/api/production/filtration-operations/42", OOSMModule.CONDITIONING);
        assertRequiredModule("/api/inventaire/articles", OOSMModule.CONDITIONING);
        assertRequiredModule("/api/inventaire/boms/42", OOSMModule.CONDITIONING);
        assertRequiredModule("/api/inventaire/lignes", OOSMModule.CONDITIONING);
        assertRequiredModule("/api/inventaire/products", OOSMModule.CONDITIONING);
    }

    @Test
    void otherInventoryAndProductionPathsKeepTheirModules() {
        assertRequiredModule("/api/inventaire/bons-commande", OOSMModule.INVENTAIR);
        assertRequiredModule("/api/production/storage-units", OOSMModule.PRODUCTION);
    }

    private static void assertRequiredModule(String path, OOSMModule module) {
        assertEquals(Set.of(module), TenantModulePathResolver.requiredModules(path).orElseThrow());
    }
}
