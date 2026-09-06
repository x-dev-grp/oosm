package com.xdev.ooms.inventory.emplacementstock.controller;

import com.xdev.ooms.inventory.emplacementstock.dto.EmplacementStockDto;
import com.xdev.ooms.inventory.emplacementstock.entity.EmplacementStock;
import com.xdev.ooms.inventory.emplacementstock.service.EmplacementStockService;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.utils.ExceptionHandler;
import org.apache.coyote.BadRequestException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventaire/emplacements")
public class EmplacementStockController extends BaseControllerImpl<EmplacementStock, EmplacementStockDto, EmplacementStockDto> {

    private final EmplacementStockService emplacementService;

    @Autowired
    public EmplacementStockController(EmplacementStockService emplacementService, ModelMapper modelMapper) {
        super(emplacementService, modelMapper);
        this.emplacementService = emplacementService;
    }

    @PutMapping("/{id}/activer")
    public ResponseEntity<?> activerEmplacement(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(attachPermittedActions(emplacementService.activerEmplacement(id)));
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "activerEmplacement", e);
        }
    }

    @PutMapping("/{id}/desactiver")
    public ResponseEntity<?> desactiverEmplacement(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(attachPermittedActions(emplacementService.desactiverEmplacement(id)));
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "desactiverEmplacement", e);
        }
    }

    @Transactional(readOnly = true)
    @GetMapping("/reserves/{client}")
    public ResponseEntity<?> getEmplacementsReservesPour(@PathVariable String client) {
        try {
            if (client == null || client.trim().isEmpty()) {
                throw new BadRequestException("Client cannot be empty");
            }
            return ResponseEntity.ok(attachPermittedActions(emplacementService.getEmplacementsReservesPour(client)));
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "getEmplacementsReservesPour", e);
        }
    }

    @PutMapping("/{id}/reserver")
    public ResponseEntity<?> reserverEmplacement(@PathVariable UUID id, @RequestBody Map<String, String> payload) {
        try {
            String reservePour = payload.get("reservePour");
            if (reservePour == null || reservePour.trim().isEmpty()) {
                throw new BadRequestException("reservePour is required for reservation");
            }
            return ResponseEntity.ok(attachPermittedActions(emplacementService.reserverEmplacement(id, reservePour)));
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "reserverEmplacement", e);
        }
    }

    @PutMapping("/{id}/liberer")
    public ResponseEntity<?> libererEmplacement(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(attachPermittedActions(emplacementService.libererEmplacement(id)));
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "libererEmplacement", e);
        }
    }

    @PutMapping("/{id}/capacite")
    public ResponseEntity<?> mettreAJourCapacite(@PathVariable UUID id, @RequestBody Map<String, String> payload) {
        try {
            String nouvelleCapacite = payload.get("capaciteActuelle");
            if (nouvelleCapacite == null || nouvelleCapacite.trim().isEmpty()) {
                throw new BadRequestException("capaciteActuelle is required for capacity update");
            }
            try {
                Double.parseDouble(nouvelleCapacite);
            } catch (NumberFormatException e) {
                throw new BadRequestException("capaciteActuelle must be a valid number");
            }
            return ResponseEntity.ok(attachPermittedActions(emplacementService.mettreAJourCapacite(id, nouvelleCapacite)));
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "mettreAJourCapacite", e);
        }
    }
@Override
    protected String getResourceName() {
        return "EMPLACEMENTSTOCK";
    }
}
