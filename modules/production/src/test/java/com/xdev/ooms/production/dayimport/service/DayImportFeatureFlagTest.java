package com.xdev.ooms.production.dayimport.service;

import com.xdev.ooms.production.dayimport.controller.DayImportController;
import com.xdev.ooms.production.dayimport.controller.PublicGoogleDriveOAuthController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.AnnotatedElementUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DayImportFeatureFlagTest {

    @Test
    void externallyReachableDayImportBeansRequireExplicitOptIn() {
        assertRequiresOptIn(DayImportController.class);
        assertRequiresOptIn(PublicGoogleDriveOAuthController.class);
        assertRequiresOptIn(DayImportDriveService.class);
    }

    private void assertRequiresOptIn(Class<?> type) {
        ConditionalOnProperty condition = AnnotatedElementUtils.findMergedAnnotation(
                type, ConditionalOnProperty.class);

        assertNotNull(condition, () -> type.getSimpleName() + " must be feature-gated");
        assertEquals("oosm.features.day-import.enabled", condition.name()[0]);
        assertEquals("true", condition.havingValue());
        assertEquals(false, condition.matchIfMissing());
    }
}
