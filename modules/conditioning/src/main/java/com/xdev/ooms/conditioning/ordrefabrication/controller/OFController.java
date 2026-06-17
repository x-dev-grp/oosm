package com.xdev.ooms.conditioning.ordrefabrication.controller;

import com.xdev.ooms.conditioning.ordrefabrication.dto.AjustementConsommationDto;
import com.xdev.ooms.conditioning.ordrefabrication.dto.OrdreFabricationDto;
import com.xdev.ooms.conditioning.ordrefabrication.dto.SaisieProductionDto;
import com.xdev.ooms.conditioning.ordrefabrication.entity.OrdreFabrication;
import com.xdev.ooms.conditioning.ordrefabrication.service.OFService;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.qr.model.QrResolveResponse;
import com.xdev.ooms.sharedkernel.utils.ExceptionHandler;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/ordreConditionement/of")
public class OFController extends BaseControllerImpl<OrdreFabrication, OrdreFabricationDto, OrdreFabricationDto> {

    private final OFService ofService;

    @Autowired
    public OFController(OFService ofService, ModelMapper modelMapper) {
        super(ofService, modelMapper);
        this.ofService = ofService;
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<OrdreFabricationDto>> getOFByProject(@PathVariable UUID projectId) {
        return ResponseEntity.ok(attachPermittedActions(ofService.getByProject(projectId)));
    }

    @PutMapping("/{id}/demarrer")
    public ResponseEntity<?> demarrerOF(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(attachPermittedActions(ofService.demarrerOF(id)));
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "demarrerOF", e);
        }
    }

    @PutMapping("/{id}/pause")
    public ResponseEntity<?> pauseOF(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(attachPermittedActions(ofService.mettreEnPause(id)));
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "pauseOF", e);
        }
    }

    @PutMapping("/{id}/reprise")
    public ResponseEntity<?> reprendreOF(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(attachPermittedActions(ofService.reprendreOF(id)));
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "reprendreOF", e);
        }
    }

    @PutMapping("/{id}/cloturer")
    public ResponseEntity<?> cloturerOF(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(attachPermittedActions(ofService.cloturerOF(id)));
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "cloturerOF", e);
        }
    }

    @PutMapping("/{id}/production")
    public ResponseEntity<?> saisirProduction(@PathVariable UUID id, @RequestBody SaisieProductionDto dto) {
        try {
            return ResponseEntity.ok(attachPermittedActions(ofService.saisirProduction(id, dto)));
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "saisirProduction", e);
        }
    }

    @PutMapping("/{id}/ajustements")
    public ResponseEntity<?> ajusterConsommation(@PathVariable UUID id, @RequestBody AjustementConsommationDto ajustement) {
        try {
            return ResponseEntity.ok(attachPermittedActions(ofService.ajusterConsommation(id, ajustement)));
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "ajusterConsommation", e);
        }
    }

    @GetMapping("/{id}/qr-image")
    public ResponseEntity<byte[]> getQrImage(@PathVariable UUID id) {
        OrdreFabrication entity = ofService.getEntityById(id);
        if (entity.getQrHex() == null || entity.getQrHex().isBlank()) {
            byte[] image = ofService.generateQrImageFromEntity(entity);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(image);
        }

        byte[] image = ofService.generateQrImage(entity.getQrHex());
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(image);
    }

    @Override
    public ResponseEntity<?> resolve(@PathVariable String publicCode) {
        try {
            QrResolveResponse resolveResponse = ofService.resolve(publicCode);
            return ResponseEntity.ok(resolveResponse);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage(), "code", "NOT_FOUND"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage(), "code", "INVALID_FORMAT"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur interne: " + e.getMessage()));
        }
    }

    @Override
    protected String getResourceName() {
        return "OF";
    }
}
