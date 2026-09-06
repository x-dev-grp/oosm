package com.xdev.ooms.inventory.boncommande.controller;

import com.xdev.ooms.inventory.boncommande.dto.BonCommandeDto;
import com.xdev.ooms.inventory.boncommande.dto.LigneBonCommandeDto;
import com.xdev.ooms.inventory.boncommande.entity.BonCommande;
import com.xdev.ooms.inventory.boncommande.service.BonCommandeService;
import com.xdev.ooms.sharedkernel.apiDTOs.ApiResponse;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.utils.ExceptionHandler;
import org.apache.coyote.BadRequestException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventaire/bons-commande")
public class BonCommandeController extends BaseControllerImpl<BonCommande, BonCommandeDto, BonCommandeDto> {

    private final BonCommandeService bonCommandeService;

    @Autowired
    public BonCommandeController(BonCommandeService bonCommandeService, ModelMapper modelMapper) {
        super(bonCommandeService, modelMapper);
        this.bonCommandeService = bonCommandeService;
    }

    @PostMapping("/{id}/valider")
    public ResponseEntity<ApiResponse<BonCommande, BonCommandeDto>> validerBonCommande(@PathVariable UUID id) {
        try {
            bonCommandeService.validerBonCommande(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Bon commande validated successfully", null));
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "validerBonCommande", e);
        }
    }

    @PostMapping("/{id}/refuser")
    public ResponseEntity<ApiResponse<BonCommande, BonCommandeDto>> refuserBonCommande(
            @PathVariable UUID id,
            @RequestBody Map<String, String> payload) {
        try {
            String motif = payload.get("motif");
            if (motif == null || motif.trim().isEmpty()) {
                throw new BadRequestException("Motif is required for rejection");
            }
            bonCommandeService.refuserBonCommande(id, motif);
            return ResponseEntity.ok(new ApiResponse<>(true, "Bon commande rejected successfully", null));
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "refuserBonCommande", e);
        }
    }

    @PostMapping("/{id}/receptionner")
    public ResponseEntity<ApiResponse<BonCommande, BonCommandeDto>> receptionnerLignesBon(
            @PathVariable UUID id,
            @RequestBody List<LigneBonCommandeDto> lignesRecues) {
        try {
            BonCommandeDto bc = bonCommandeService.receptionnerCommande(id, lignesRecues);
            return ResponseEntity.ok(new ApiResponse<>(true, "Réception enregistrée avec succès", List.of(attachPermittedActions(bc))));
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "receptionnerLignesBon", e);
        }
    }

    @Override
    protected String getResourceName() {
        return "BONCOMMANDE";
    }
}
