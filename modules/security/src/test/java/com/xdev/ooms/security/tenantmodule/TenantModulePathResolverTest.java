package com.xdev.ooms.security.tenantmodule;

import com.xdev.ooms.sharedkernel.models.OOSMModule;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TenantModulePathResolverTest {

    @Test
    void stockOperationsAcceptInventoryOrConditioning() {
        Set<OOSMModule> either = Set.of(OOSMModule.INVENTAIR, OOSMModule.CONDITIONING);
        assertRequiredModules("/api/inventaire/stocks", either);
        assertRequiredModules("/api/inventaire/stocks/by-location", either);
        assertRequiredModules("/api/inventaire/mouvements-stocks?page=0", either);
        assertRequiredModules("/api/inventaire/emplacements/42", either);
        assertRequiredModules("/api/inventaire/articles", either);
        assertRequiredModules("/api/inventaire/boms/42", either);
        assertRequiredModules("/api/inventaire/lignes", either);
        assertRequiredModules("/api/inventaire/products", either);
    }

    @Test
    void oilFiltrationAcceptsProductionOrConditioning() {
        Set<OOSMModule> either = Set.of(OOSMModule.PRODUCTION, OOSMModule.CONDITIONING);
        assertRequiredModules("/api/production/filtration-operations", either);
        assertRequiredModules("/api/production/filtration-operations/42", either);
    }

    @Test
    void otherInventoryAndProductionPathsKeepTheirModules() {
        assertRequiredModules("/api/inventaire/bons-commande", Set.of(OOSMModule.INVENTAIR));
        assertRequiredModules("/api/production/storage-units", Set.of(OOSMModule.PRODUCTION));
    }

    private static void assertRequiredModules(String path, Set<OOSMModule> modules) {
        assertEquals(modules, TenantModulePathResolver.requiredModules(path).orElseThrow());
    }
}
