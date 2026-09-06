package com.xdev.ooms.inventory.bom.controller;

import com.xdev.ooms.inventory.bom.dto.BOMDto;
import com.xdev.ooms.inventory.materialneeds.dto.MaterialNeedLineDto;
import com.xdev.ooms.inventory.bom.entity.BOM;
import com.xdev.ooms.inventory.bom.service.BomService;
import com.xdev.ooms.inventory.materialneeds.service.MaterialNeedsService;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.utils.ExceptionHandler;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventaire/boms")
public class BomController extends BaseControllerImpl<BOM, BOMDto, BOMDto> {

    private final BomService bomService;
    private final MaterialNeedsService materialNeedsService;

    @Autowired
    public BomController(BomService bomService,
                         MaterialNeedsService materialNeedsService,
                         ModelMapper modelMapper) {
        super(bomService, modelMapper);
        this.bomService = bomService;
        this.materialNeedsService = materialNeedsService;
    }

    @GetMapping({"/product/{productId}", "/sku/{productId}"})
    public ResponseEntity<?> getBomsByProduct(@PathVariable UUID productId) {
        try {
            return ResponseEntity.ok(attachPermittedActions(bomService.getBomsByProduct(productId)));
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "getBomsByProduct", e);
        }
    }

    @GetMapping({"/product/{productId}/active", "/sku/{productId}/active"})
    public ResponseEntity<?> getActiveBomForProduct(@PathVariable UUID productId) {
        try {
            BOMDto bom = bomService.getActiveBomForProduct(productId);
            if (bom == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Aucune nomenclature active pour ce produit"));
            }
            return ResponseEntity.ok(attachPermittedActions(bom));
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "getActiveBomForProduct", e);
        }
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateBom(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(attachPermittedActions(bomService.activateBom(id)));
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "activateBom", e);
        }
    }

    @GetMapping("/{id}/material-needs")
    public ResponseEntity<List<MaterialNeedLineDto>> getMaterialNeeds(
            @PathVariable UUID id,
            @RequestParam(name = "quantity") double quantity) {
        return ResponseEntity.ok(materialNeedsService.computeForBom(id, quantity));
    }

    @Override
    protected String getResourceName() {
        return "BOM";
    }
}
