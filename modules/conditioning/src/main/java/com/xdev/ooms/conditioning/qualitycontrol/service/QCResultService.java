package com.xdev.ooms.conditioning.qualitycontrol.service;

import com.xdev.ooms.sharedkernel.utils.OOSMLogger;

import com.xdev.ooms.conditioning.Enum.StatutOF;
import com.xdev.ooms.conditioning.ordrefabrication.service.OFService;
import com.xdev.ooms.conditioning.qualitycontrol.dto.QCResultDTO;
import com.xdev.ooms.conditioning.Enum.ControlType;
import com.xdev.ooms.conditioning.Enum.QualityStatus;
import com.xdev.ooms.conditioning.Enum.ResultStatus;
import com.xdev.ooms.conditioning.ordrefabrication.entity.OrdreFabrication;
import com.xdev.ooms.conditioning.ordrefabrication.repository.OrdreFabricationRepository;
import com.xdev.ooms.conditioning.qualitycontrol.entity.QCControlPoint;
import com.xdev.ooms.conditioning.qualitycontrol.entity.QCPlan;
import com.xdev.ooms.conditioning.qualitycontrol.entity.QCResult;
import com.xdev.ooms.conditioning.qualitycontrol.repository.QCControlPointRepository;
import com.xdev.ooms.conditioning.qualitycontrol.repository.QCPlanRepository;
import com.xdev.ooms.conditioning.qualitycontrol.repository.QCResultRepository;
import com.xdev.ooms.sharedkernel.ports.NotificationEvent;
import com.xdev.ooms.sharedkernel.ports.NotificationPort;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class QCResultService extends BaseServiceImpl<QCResult, QCResultDTO, QCResultDTO> {

    private final QCResultRepository resultRepository;
    private final QCControlPointRepository controlPointRepository;
    private final OrdreFabricationRepository ofRepository;
    private final ModelMapper modelMapper;
    private final QCPlanRepository qcPlanRepository;
    private final OFService ofService;
    private final NotificationPort notificationPort;

    public QCResultService(BaseRepository<QCResult> repository,
                           QCResultRepository resultRepository,
                           QCControlPointRepository controlPointRepository,
                           OrdreFabricationRepository ofRepository,
                           ModelMapper modelMapper,
                           QCPlanRepository qcPlanRepository,
                           OFService ofService,
                           NotificationPort notificationPort) {
        super(repository, modelMapper);
        this.resultRepository = resultRepository;
        this.controlPointRepository = controlPointRepository;
        this.ofRepository = ofRepository;
        this.modelMapper = modelMapper;
        this.qcPlanRepository = qcPlanRepository;
        this.ofService = ofService;
        this.notificationPort = notificationPort;
    }

    @Transactional
    public QCResultDTO enregistrerResultat(QCResultDTO dto) {
        QCControlPoint point = controlPointRepository.findById(dto.getControlPointId())
                .orElseThrow(() -> new RuntimeException("Point de contrôle inconnu"));
        OrdreFabrication of = ofRepository.findById(dto.getOfId())
                .orElseThrow(() -> new RuntimeException("OF inconnu"));

        QCResult result = modelMapper.map(dto, QCResult.class);
        result.setControlPoint(point);
        result.setOf(of);
        result.setDateControle(LocalDateTime.now());
        if (point.getType() == ControlType.NUMERIC && dto.getStatut() == null) {
            try {
                Double val = Double.parseDouble(dto.getValeur());
                if (val >= point.getMinValue() && val <= point.getMaxValue()) {
                    result.setStatut(ResultStatus.OK);
                } else {
                    result.setStatut(ResultStatus.NOK);
                }
            } catch (NumberFormatException e) {
                throw new RuntimeException("Valeur numérique invalide : " + dto.getValeur());
            }
        } else {
            result.setStatut(dto.getStatut());
        }
        result = resultRepository.save(result);

        if (result.getStatut() == ResultStatus.NOK) {
            point.setBlocking(true);
            controlPointRepository.save(point);
        } else {
            point.setBlocking(false);
            controlPointRepository.save(point);
        }

        if (result.getStatut() == ResultStatus.NOK && point.isBlocking()) {
            bloquerOF(of.getId());
            if (of.getStatut() == StatutOF.EN_COURS) {
                ofService.mettreEnPause(of.getId());
            }
        } else if (result.getStatut() == ResultStatus.OK) {
            verifierEtDebloquerOF(of.getId());
            OrdreFabrication refreshed = ofRepository.findById(of.getId()).orElseThrow();
            if (refreshed.getQualityStatus() == QualityStatus.FREE && refreshed.getStatut() == StatutOF.EN_PAUSE) {
                ofService.demarrerOF(of.getId());
            }
        }
        return modelMapper.map(result, QCResultDTO.class);
    }

    private void bloquerOF(UUID ofId) {
        OrdreFabrication of = ofRepository.findById(ofId).orElseThrow();
        if (of.getQualityStatus() != QualityStatus.BLOCKED) {
            of.setQualityStatus(QualityStatus.BLOCKED);
            ofRepository.save(of);
            try {
                notificationPort.publish(new NotificationEvent(
                        "OF_QC_BLOCKED",
                        ofId,
                        of.getCode(),
                        Map.of("ofCode", of.getCode()),
                        null,
                        null));
            } catch (Exception e) {
                OOSMLogger.warn(QCResultService.class, "Failed to publish OF blocked notification: {}", e.getMessage());
            }
        }
    }

    @Transactional
    public void verifierEtDebloquerOF(UUID ofId) {
        OrdreFabrication of = ofRepository.findById(ofId)
                .orElseThrow(() -> new RuntimeException("OF inconnu"));
        if (of.getQualityStatus() != QualityStatus.BLOCKED) {
            return;
        }
        List<QCResult> allResults = resultRepository.findByOfIdAndTenantIdOrderByDateControleDesc(ofId, TenantContext.getCurrentTenant());
        QCPlan activePlan = qcPlanRepository.findByOfIdAndActifTrue(ofId)
                .orElseThrow(() -> new RuntimeException("Aucun plan actif pour cet OF"));
        List<QCControlPoint> blockingPoints = controlPointRepository.findByPlanIdAndBlockingTrue(activePlan.getId());
        boolean tousControlesOK = true;
        for (QCControlPoint blockingPoint : blockingPoints) {
            boolean pointControle = allResults.stream()
                    .anyMatch(r -> r.getControlPoint().getId().equals(blockingPoint.getId())
                            && r.getStatut() == ResultStatus.OK);

            if (!pointControle) {
                tousControlesOK = false;
                break;
            }
        }
        if (tousControlesOK) {
            of.setQualityStatus(QualityStatus.FREE);
            ofRepository.save(of);
        }
    }

    @Transactional(readOnly = true)
    public List<QCResultDTO> getHistoriqueOF(UUID ofId) {
        return resultRepository.findByOfIdAndTenantIdOrderByDateControleDesc(ofId, TenantContext.getCurrentTenant())
                .stream()
                .map(r -> modelMapper.map(r, QCResultDTO.class))
                .collect(Collectors.toList());
    }
}
