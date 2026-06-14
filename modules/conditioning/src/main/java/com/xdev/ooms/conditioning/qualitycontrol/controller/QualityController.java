package com.xdev.ooms.conditioning.qualitycontrol.controller;


import com.xdev.ooms.conditioning.qualitycontrol.dto.QCControlPointDTO;
import com.xdev.ooms.conditioning.qualitycontrol.dto.QCPlanDTO;
import com.xdev.ooms.conditioning.qualitycontrol.dto.QCResultDTO;
import com.xdev.ooms.conditioning.qualitycontrol.entity.QCControlPoint;
import com.xdev.ooms.conditioning.qualitycontrol.entity.QCPlan;
import com.xdev.ooms.conditioning.qualitycontrol.entity.QCResult;
import com.xdev.ooms.conditioning.qualitycontrol.service.QCPlanService;
import com.xdev.ooms.conditioning.qualitycontrol.service.QCResultService;
import com.xdev.ooms.sharedkernel.apiDTOs.ApiResponse;
import com.xdev.ooms.sharedkernel.apiDTOs.ApiSingleResponse;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.models.Action;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/ordreConditionement/qualite")
public class QualityController extends BaseControllerImpl<QCPlan, QCPlanDTO, QCPlanDTO> {

    private final QCPlanService planService;
    private final QCResultService resultService;
    private static final Set<Action> QUALITY_ACTIONS = Set.of(
            Action.READ,
            Action.CREATE,
            Action.UPDATE,
            Action.DELETE,
            Action.VALIDATE,
            Action.UPDATE_STATUS,
            Action.GEN_PDF
    );

    public QualityController(QCPlanService planService, QCResultService resultService, ModelMapper modelMapper) {
        super(planService, modelMapper);
        this.planService = planService;
        this.resultService = resultService;
    }

    @PostMapping("/plans/of/{ofId}/create")
    public ResponseEntity<ApiSingleResponse<QCPlan, QCPlanDTO>> createPlan(
            @PathVariable UUID ofId,
            @RequestParam String titre) {
        try {
            QCPlanDTO plan = planService.createPlan(ofId, titre);
            return ResponseEntity.ok(new ApiSingleResponse<>(true, "Plan créé", attachQualityActions(plan)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiSingleResponse<>(false, e.getMessage(), null));
        }
    }

    @PostMapping("/plans/{planId}/points/addControlPoint")
    public ResponseEntity<ApiSingleResponse<QCControlPoint, QCControlPointDTO>> addControlPoint(
            @PathVariable UUID planId,
            @RequestBody QCControlPointDTO dto) {
        try {
            QCControlPointDTO point = planService.addControlPoint(planId, dto);
            return ResponseEntity.ok(new ApiSingleResponse<>(true, "Point ajouté", attachQualityActions(point)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiSingleResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/plans/of/{ofId}/points/active")
    public ResponseEntity<ApiResponse<QCControlPoint, QCControlPointDTO>> getActivePoints(@PathVariable UUID ofId) {
        try {
            List<QCControlPointDTO> points = planService.getPointsForOF(ofId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Points récupérés", attachQualityActions(points)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/plans/all")
    public ResponseEntity<ApiResponse<QCPlan, QCPlanDTO>> getAllPlans() {
        List<QCPlanDTO> plans = planService.findAll();
        return ResponseEntity.ok(new ApiResponse<>(true, "Plans récupérés", attachPermittedActions(plans)));
    }


    @PostMapping("/resultats/add")
    public ResponseEntity<ApiSingleResponse<QCResult, QCResultDTO>> enregistrerResultat(@RequestBody QCResultDTO dto) {
        try {
            QCResultDTO resultat = resultService.enregistrerResultat(dto);
            return ResponseEntity.ok(new ApiSingleResponse<>(true, "Résultat enregistré", attachQualityActions(resultat)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiSingleResponse<>(false, e.getMessage(), null));
        }
    }

    @PutMapping("/resultats/of/{ofId}/debloquer")
    public ResponseEntity<ApiResponse<QCPlan, QCPlanDTO>> debloquerOF(@PathVariable UUID ofId) {
        try {
            resultService.verifierEtDebloquerOF(ofId);
            return ResponseEntity.ok(new ApiResponse<>(true, "OF débloqué", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/resultats/of/{ofId}/historique")
    public ResponseEntity<ApiResponse<QCResult, QCResultDTO>> getHistoriqueOF(@PathVariable UUID ofId) {
        List<QCResultDTO> historique = resultService.getHistoriqueOF(ofId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Historique récupéré", attachQualityActions(historique)));
    }

    @GetMapping("/plans/of/{ofId}")
    public ResponseEntity<ApiSingleResponse<QCPlan, QCPlanDTO>> getPlanByOfId(@PathVariable UUID ofId) {
        try {
            QCPlanDTO plan = planService.findActivePlanByOfId(ofId);
            return ResponseEntity.ok(new ApiSingleResponse<>(true, "Plan récupéré", attachPermittedActions(plan)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiSingleResponse<>(false, e.getMessage(), null));
        }
    }
    @DeleteMapping("/plans/points/{pointId}")
    public ResponseEntity<ApiResponse<QCPlan, QCPlanDTO>> deleteControlPoint(@PathVariable UUID pointId) {
        try {
            planService.deleteControlPoint(pointId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Point supprimé", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    private <DTO extends com.xdev.ooms.sharedkernel.dtos.BaseDto<?>> DTO attachQualityActions(DTO dto) {
        return attachPermittedActions(dto, getResourceName(), QUALITY_ACTIONS);
    }

    private <DTO extends com.xdev.ooms.sharedkernel.dtos.BaseDto<?>> List<DTO> attachQualityActions(List<DTO> dtos) {
        return attachPermittedActions(dtos, getResourceName(), QUALITY_ACTIONS);
    }

    @Override
    protected String getResourceName() {
        return "QUALITY";
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
}
