package com.xdev.ooms.production.filtration.controller;

import com.xdev.ooms.sharedkernel.utils.OSMLogger;

import com.xdev.ooms.production.filtration.dto.FiltrationCompletionDto;
import com.xdev.ooms.production.filtration.dto.FiltrationRequestDto;
import com.xdev.ooms.production.filtration.dto.FiltrationResultDto;
import com.xdev.ooms.production.filtration.dto.FiltrationStatus;
import com.xdev.ooms.production.filtration.dto.UpdateFiltrationStatusDto;

import com.xdev.ooms.production.filtration.service.FiltrationService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/production/filtration")
public class FiltrationController {
    private final FiltrationService filtrationService;

    public FiltrationController(FiltrationService filtrationService) {
        this.filtrationService = filtrationService;
    }

    /**
     * Créer une nouvelle opération de filtration
     */
    @PostMapping
    public ResponseEntity<?> createFiltration(@Valid @RequestBody FiltrationRequestDto req) {
        try {

            FiltrationResultDto result = filtrationService.createFiltration(req);

            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse("Erreur de validation: " + e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Une erreur technique est survenue"));
        }
    }



    //endpoint de modification générale
    @PutMapping("/{operationId}")
    public ResponseEntity<?> updateFiltration(
            @PathVariable UUID operationId,
            @Valid @RequestBody FiltrationRequestDto req) {
        String requestId = generateRequestId();

        try {
            OSMLogger.info(FiltrationController.class, "Request ID: {} - Modification de l'opération {}", requestId, operationId);

            FiltrationResultDto result = filtrationService.updateFiltration(operationId, req);

            OSMLogger.info(FiltrationController.class, "Request ID: {} - Opération {} modifiée avec succès", requestId, operationId);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException | IllegalStateException e) {
            OSMLogger.error(FiltrationController.class, "Request ID: {} - Erreur de validation: {}", requestId, e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse("Erreur: " + e.getMessage()));

        } catch (Exception e) {
            OSMLogger.error(FiltrationController.class, "Request ID: {} - Erreur inattendue: {}", requestId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Une erreur technique est survenue"));
        }
    }


    @PutMapping("/{operationId}/start")
    public ResponseEntity<?> startFiltration(@PathVariable UUID operationId) {
        String requestId = generateRequestId();

        try {

            FiltrationResultDto result = filtrationService.startFiltration(operationId);

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse("Erreur: " + e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Une erreur technique est survenue"));
        }
    }

    @PutMapping("/{operationId}/complete")
    public ResponseEntity<?> completeFiltration(
            @PathVariable UUID operationId,
            @Valid @RequestBody FiltrationCompletionDto completionData) { // [NOUVEAU] Paramètre ajouté
        String requestId = generateRequestId();

        try {
            // Appel du service avec les données de completion
            FiltrationResultDto result = filtrationService.completeFiltration(operationId, completionData);

            OSMLogger.info(FiltrationController.class, "Request ID: {} - Opération {} terminée avec succès", requestId, operationId);
            return ResponseEntity.ok(result);

        }   catch (Exception e) {
            OSMLogger.error(FiltrationController.class, "Request ID: {} - Erreur inattendue: {}", requestId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur: " + e.getMessage()));
        }
    }

    @PutMapping("/{operationId}/status")
    public ResponseEntity<?> updateFiltrationStatus(
            @PathVariable UUID operationId,
            @Valid @RequestBody UpdateFiltrationStatusDto statusDto) {
        String requestId = generateRequestId();

        try {
            OSMLogger.info(FiltrationController.class, "Request ID: {} - Mise à jour du statut de l'opération {} vers {}",
                    requestId, operationId, statusDto.getStatus());

            FiltrationResultDto result = filtrationService.updateFiltrationStatus(operationId, statusDto);

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse("Erreur: " + e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Une erreur technique est survenue"));
        }
    }


    @PutMapping("/{operationId}/note")
    public ResponseEntity<?> addNote(
            @PathVariable UUID operationId,
            @RequestBody String note) { // La note est envoyée dans le body
        String requestId = generateRequestId();

        try {

            // [NOUVEAU] Appel à la nouvelle méthode du service
            FiltrationResultDto result = filtrationService.addNote(operationId, note);

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse("Erreur: " + e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Une erreur technique est survenue"));
        }
    }
    @DeleteMapping("/{operationId}")
    public ResponseEntity<?> deleteFiltration(@PathVariable UUID operationId) {
        String requestId = generateRequestId();
        try {

            filtrationService.deleteFiltration(operationId);

            return ResponseEntity.noContent().build(); // 204 — matches Observable<void> on the frontend

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Opération non trouvée: " + e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Une erreur technique est survenue"));
        }
    }
    /**
     *  Récupérer une opération spécifique
     */
    @GetMapping("/{operationId}")
    public ResponseEntity<?> getFiltration(@PathVariable UUID operationId) {
        String requestId = generateRequestId();

        try {

            FiltrationResultDto result = filtrationService.getFiltrationById(operationId);

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Opération non trouvée: " + e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Une erreur technique est survenue"));
        }
    }

    /**
     * Récupérer toutes les opérations
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllFiltrations() {
        String requestId = generateRequestId();

        try {
            OSMLogger.info(FiltrationController.class, "Request ID: {} - Récupération de toutes les opérations", requestId);

            List<FiltrationResultDto> results = filtrationService.getAllFiltrations();

            OSMLogger.info(FiltrationController.class, "Request ID: {} - {} opérations trouvées", requestId, results.size());
            return ResponseEntity.ok(results);

        } catch (Exception e) {
            OSMLogger.error(FiltrationController.class, "Request ID: {} - Erreur inattendue: {}", requestId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Une erreur technique est survenue"));
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getFiltrationsByStatus(@PathVariable FiltrationStatus status) {
        String requestId = generateRequestId();

        try {
            OSMLogger.info(FiltrationController.class, "Request ID: {} - Récupération des opérations avec statut: {}", requestId, status);

            // [NOUVEAU] Appel à la nouvelle méthode du service
            List<FiltrationResultDto> results = filtrationService.getFiltrationsByStatus(status);

            return ResponseEntity.ok(results);

        } catch (Exception e) {
            OSMLogger.error(FiltrationController.class, "Request ID: {} - Erreur: {}", requestId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Une erreur technique est survenue"));
        }




    }

    // retourner les détails de traçabilité d’une opération
    @GetMapping("/{operationId}/traceability")
    public ResponseEntity<?> getTraceability(@PathVariable UUID operationId) {
        FiltrationResultDto dto = filtrationService.getFiltrationById(operationId);
        // On peut aussi enrichir avec les livraisons associées au lot source.
        return ResponseEntity.ok(dto);
    }

    private String generateRequestId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private static class ErrorResponse {
        private final String message;
        private final long timestamp;

        public ErrorResponse(String message) {
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        public String getMessage() { return message; }
        public long getTimestamp() { return timestamp; }
    }
}