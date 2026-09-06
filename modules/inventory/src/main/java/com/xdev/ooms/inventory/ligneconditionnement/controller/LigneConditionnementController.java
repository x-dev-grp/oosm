package com.xdev.ooms.inventory.ligneconditionnement.controller;

import com.xdev.ooms.inventory.Enum.Statue;
import com.xdev.ooms.inventory.ligneconditionnement.dto.LigneConditionnementDto;
import com.xdev.ooms.inventory.ligneconditionnement.entity.LigneConditionnement;
import com.xdev.ooms.inventory.ligneconditionnement.service.LigneConditionnementService;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.utils.ExceptionHandler;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventaire/lignes")
public class LigneConditionnementController extends BaseControllerImpl<LigneConditionnement, LigneConditionnementDto, LigneConditionnementDto> {

    private final LigneConditionnementService ligneService;

    @Autowired
    public LigneConditionnementController(LigneConditionnementService ligneService, ModelMapper modelMapper) {
        super(ligneService, modelMapper);
        this.ligneService = ligneService;
    }

    @PutMapping("/{id}/desactiver")
    public ResponseEntity<?> desactiverLigne(@PathVariable UUID id) {
        try {
            ligneService.desactiverLigne(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "desactiverLigne", e);
        }
    }

    @PutMapping("/{id}/activer")
    public ResponseEntity<?> activerLigne(@PathVariable UUID id) {
        try {
            ligneService.activerLigne(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "activerLigne", e);
        }
    }

    @PutMapping("/{id}/changer-etat")
    public ResponseEntity<?> changerEtat(@PathVariable UUID id, @RequestBody Map<String, Statue> payload) {
        try {
            Statue nouvelEtat = payload.get("etat");
            return ResponseEntity.ok(attachPermittedActions(ligneService.changerEtat(id, nouvelEtat)));
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "changerEtat", e);
        }
    }

    @Transactional(readOnly = true)
    @GetMapping("/actifs")
    public ResponseEntity<?> getLignesActives() {
        try {
            return ResponseEntity.ok(attachPermittedActions(ligneService.getLignesActives()));
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "getLignesActives", e);
        }
    }

    @Override
    protected String getResourceName() {
        return "LIGNECONDITIONNEMENT";
    }
}
