package com.xdev.ooms.inventory.produitfinal.controller;

import com.xdev.ooms.inventory.produitfinal.dto.ProduitFinalDto;
import com.xdev.ooms.inventory.produitfinal.entity.ProduitFinal;
import com.xdev.ooms.inventory.Enum.ProduitFinalType;
import com.xdev.ooms.inventory.produitfinal.service.ProduitFinalService;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.utils.ExceptionHandler;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping({"/api/inventaire/produits-finis", "/api/inventaire/products", "/api/inventaire/skus"})
public class ProduitFinalController extends BaseControllerImpl<ProduitFinal, ProduitFinalDto, ProduitFinalDto> {

    private final ProduitFinalService produitFinalService;

    @Autowired
    public ProduitFinalController(ProduitFinalService produitFinalService, ModelMapper modelMapper) {
        super(produitFinalService, modelMapper);
        this.produitFinalService = produitFinalService;
    }

    @Transactional(readOnly = true)
    @GetMapping("/actifs")
    public ResponseEntity<?> getActiveProduitsFinaux() {
        try {
            return ResponseEntity.ok(attachPermittedActions(produitFinalService.getAllActiveProduitsFinaux()));
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "getActiveProduitsFinaux", e);
        }
    }

    @Transactional(readOnly = true)
    @GetMapping("/type/{type}")
    public ResponseEntity<?> getProduitsFinauxByType(@PathVariable ProduitFinalType type) {
        try {
            return ResponseEntity.ok(attachPermittedActions(produitFinalService.getProduitsFinauxByType(type)));
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "getProduitsFinauxByType", e);
        }
    }

    @PutMapping("/{id}/desactiver")
    public ResponseEntity<?> desactiverProduitFinal(@PathVariable UUID id) {
        try {
            produitFinalService.desactiverProduitFinal(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "desactiverProduitFinal", e);
        }
    }

    @PutMapping("/{id}/activer")
    public ResponseEntity<?> activerProduitFinal(@PathVariable UUID id) {
        try {
            produitFinalService.activerProduitFinal(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "activerProduitFinal", e);
        }
    }

    @Override
    protected String getResourceName() {
        return "PRODUITFINAL";
    }
}
