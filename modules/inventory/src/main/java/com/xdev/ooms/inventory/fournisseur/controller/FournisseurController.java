package com.xdev.ooms.inventory.fournisseur.controller;

import com.xdev.ooms.inventory.fournisseur.dto.FournisseurDto;
import com.xdev.ooms.inventory.fournisseur.entity.Fournisseur;
import com.xdev.ooms.inventory.fournisseur.service.FournisseurService;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.utils.ExceptionHandler;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/inventaire/fournisseurs")
public class FournisseurController extends BaseControllerImpl<Fournisseur, FournisseurDto, FournisseurDto> {

    private final FournisseurService fournisseurService;

    @Autowired
    public FournisseurController(FournisseurService fournisseurService, ModelMapper modelMapper) {
        super(fournisseurService, modelMapper);
        this.fournisseurService = fournisseurService;
    }

    @Transactional(readOnly = true)
    @GetMapping("/actifs")
    public ResponseEntity<?> getActiveFournisseurs() {
        try {
            return ResponseEntity.ok(attachPermittedActions(fournisseurService.getActiveFournisseurs()));
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "getActiveFournisseurs", e);
        }
    }

    @PutMapping("/{id}/activer")
    public ResponseEntity<?> activerFournisseur(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(attachPermittedActions(fournisseurService.activerFournisseur(id)));
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "activerFournisseur", e);
        }
    }

    @PutMapping("/{id}/desactiver")
    public ResponseEntity<?> desactiverFournisseur(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(attachPermittedActions(fournisseurService.desactiverFournisseur(id)));
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "desactiverFournisseur", e);
        }
    }

    @Override
    protected String getResourceName() {
        return "FOURNISSEUR";
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
}
