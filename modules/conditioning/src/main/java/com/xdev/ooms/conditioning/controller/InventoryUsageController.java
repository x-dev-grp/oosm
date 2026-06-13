package com.xdev.ooms.conditioning.controller;

import com.xdev.ooms.conditioning.service.InventoryUsageService;
import com.xdev.ooms.inventory.dto.InventoryUsageBlockersDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/ordreConditionement/inventory-usage")
public class InventoryUsageController {

    private final InventoryUsageService inventoryUsageService;

    public InventoryUsageController(InventoryUsageService inventoryUsageService) {
        this.inventoryUsageService = inventoryUsageService;
    }

    @GetMapping("/articles/{articleId}")
    public ResponseEntity<InventoryUsageBlockersDto> getArticleUsageBlockers(@PathVariable UUID articleId) {
        return ResponseEntity.ok(inventoryUsageService.getArticleUsageBlockers(articleId));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<InventoryUsageBlockersDto> getProductUsageBlockers(@PathVariable UUID productId) {
        return ResponseEntity.ok(inventoryUsageService.getProductUsageBlockers(productId));
    }
}
