package com.xdev.ooms.production.unifieddelivery.controller;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnifiedDeliveryControllerMappingTest {

    @Test
    void deliveryMutationsUsePostMappings() {
        assertPostMapping("createOilRecFromOliveRec", "/createOilRecFromOliveRec/{uuid}");
        assertPostMapping("updateStatue", "/updateStatue/{id}/{status}");
        assertPostMapping("updatePrice", "/updateprice/{id}/{updateprice}");
    }

    private void assertPostMapping(String methodName, String path) {
        Method method = Arrays.stream(UnifiedDeliveryController.class.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals(methodName))
                .findFirst()
                .orElseThrow();
        PostMapping mapping = method.getAnnotation(PostMapping.class);
        assertNotNull(mapping, methodName + " must use POST");
        assertTrue(Arrays.asList(mapping.value()).contains(path));
    }
}
