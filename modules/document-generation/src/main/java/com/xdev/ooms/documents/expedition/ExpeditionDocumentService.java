package com.xdev.ooms.documents.expedition;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xdev.ooms.conditioning.expedition.entity.Expedition;
import com.xdev.ooms.conditioning.expedition.entity.ExpeditionArticle;
import com.xdev.ooms.conditioning.expedition.repository.ExpeditionRepository;
import com.xdev.ooms.conditioning.expedition.service.TraceabilityService;
import com.xdev.ooms.conditioning.projet.entity.Client;
import com.xdev.ooms.conditioning.projet.entity.Projet;
import com.xdev.ooms.conditioning.projet.repository.ProjetRepository;
import com.xdev.ooms.documents.GeneratedDocument;
import com.xdev.ooms.documents.form.dto.FormPdfDocument;
import com.xdev.ooms.sharedkernel.ports.CompanyProfileReadPort;
import com.xdev.ooms.sharedkernel.ports.CompanyProfileSnapshot;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class ExpeditionDocumentService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRENCH);

    private final ExpeditionRepository expeditionRepository;
    private final ProjetRepository projetRepository;
    private final TraceabilityService traceabilityService;
    private final ExpeditionPdfGeneratorService expeditionPdfGeneratorService;
    private final CompanyProfileReadPort companyProfileReadPort;
    private final ObjectMapper objectMapper;

    public ExpeditionDocumentService(
            ExpeditionRepository expeditionRepository,
            ProjetRepository projetRepository,
            TraceabilityService traceabilityService,
            ExpeditionPdfGeneratorService expeditionPdfGeneratorService,
            CompanyProfileReadPort companyProfileReadPort,
            ObjectMapper objectMapper) {
        this.expeditionRepository = expeditionRepository;
        this.projetRepository = projetRepository;
        this.traceabilityService = traceabilityService;
        this.expeditionPdfGeneratorService = expeditionPdfGeneratorService;
        this.companyProfileReadPort = companyProfileReadPort;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public GeneratedDocument generateExpeditionPdf(UUID expeditionId) {
        Expedition expedition = expeditionRepository.findByIdForPdf(expeditionId)
                .orElseThrow(() -> new EntityNotFoundException("Expedition not found: " + expeditionId));

        Map<String, Object> traceability = resolveTraceability(expedition);
        ExpeditionPdfConfig config = buildExpeditionConfig(expedition, traceability);
        FormPdfDocument document = expeditionPdfGeneratorService.generate(config);
        return GeneratedDocument.fromForm(document.fileName(), document.content());
    }

    @Transactional(readOnly = true)
    public GeneratedDocument generateProjectTraceabilityPdf(UUID projectId) {
        Projet projet = projetRepository.findByIdForPdf(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + projectId));

        Map<String, Object> traceability = traceabilityService.getLiveProjectTraceability(projectId);
        ExpeditionPdfConfig config = buildProjectTraceabilityConfig(projet, traceability);
        FormPdfDocument document = expeditionPdfGeneratorService.generate(config);
        return GeneratedDocument.fromForm(document.fileName(), document.content());
    }

    private Map<String, Object> resolveTraceability(Expedition expedition) {
        if (expedition.getTraceabilitySnapshotJson() != null
                && !expedition.getTraceabilitySnapshotJson().isBlank()) {
            try {
                return objectMapper.readValue(
                        expedition.getTraceabilitySnapshotJson(),
                        new TypeReference<LinkedHashMap<String, Object>>() {});
            } catch (Exception ignored) {
                // fall through to live traceability
            }
        }
        return traceabilityService.getExpeditionTraceability(expedition);
    }

    private ExpeditionPdfConfig buildExpeditionConfig(Expedition expedition, Map<String, Object> traceability) {
        Projet projet = expedition.getProjet();
        Client client = projet == null ? null : projet.getClient();

        ExpeditionPdfConfig config = new ExpeditionPdfConfig();
        config.setTitle(ExpeditionPdfLabels.EXPEDITION_DELIVERY_TITLE);
        config.setReference(expedition.getExpeditionNumber());
        config.setDate(expedition.getCreatedDate() == null ? "" : expedition.getCreatedDate().format(DATE_FMT));
        config.setClientName(client == null ? null : client.getNom());
        config.setClientAddress(client == null ? null : client.getAdresse());
        config.setClientPhone(client == null ? null : client.getTelephone());
        config.setDestination(expedition.getDestination());
        config.setCarrier(expedition.getCarrierName());
        config.setDriver(expedition.getDriverName());
        config.setTruck(expedition.getTruckNumber());
        config.setIncoterm(expedition.getIncoterm());
        config.setCompanyAddress(companyAddress());
        config.setLines(mapLines(expedition.getLines()));
        config.setTraceability(traceability);
        return config;
    }

    private ExpeditionPdfConfig buildProjectTraceabilityConfig(Projet projet, Map<String, Object> traceability) {
        Client client = projet.getClient();

        ExpeditionPdfConfig config = new ExpeditionPdfConfig();
        config.setTitle(ExpeditionPdfLabels.PROJECT_TRACEABILITY_TITLE);
        config.setReference(projet.getCode());
        config.setDate(DATE_FMT.format(java.time.LocalDate.now()));
        config.setClientName(client == null ? null : client.getNom());
        config.setClientAddress(client == null ? null : client.getAdresse());
        config.setCompanyAddress(companyAddress());
        config.setLines(mapProjectLines(traceability));
        config.setTraceability(traceability);
        return config;
    }

    private List<ExpeditionPdfConfig.Line> mapLines(List<ExpeditionArticle> articles) {
        List<ExpeditionPdfConfig.Line> lines = new ArrayList<>();
        if (articles == null) {
            return lines;
        }
        for (ExpeditionArticle article : articles) {
            ExpeditionPdfConfig.Line line = new ExpeditionPdfConfig.Line();
            line.setOfCode(article.getOfCode());
            line.setArticleName(firstNonBlank(article.getArticleName(), article.getArticleNameSnapshot()));
            line.setQuantity(article.getQuantity());
            line.setUnit(article.getUnit());
            line.setLotNumber(article.getLotNumber());
            lines.add(line);
        }
        return lines;
    }

    @SuppressWarnings("unchecked")
    private List<ExpeditionPdfConfig.Line> mapProjectLines(Map<String, Object> traceability) {
        Object ofDetails = traceability.get("ofDetails");
        if (!(ofDetails instanceof Map<?, ?> detailsMap)) {
            return List.of();
        }
        List<ExpeditionPdfConfig.Line> lines = new ArrayList<>();
        for (Object value : detailsMap.values()) {
            if (!(value instanceof Map<?, ?> ofMap)) {
                continue;
            }
            Map<String, Object> of = (Map<String, Object>) ofMap;
            ExpeditionPdfConfig.Line line = new ExpeditionPdfConfig.Line();
            line.setOfCode(asString(of.get("code")));
            line.setArticleName(asString(of.get("articleName")));
            line.setQuantity(asInt(of.get("quantityGood"), 0));
            line.setUnit("UNIT");
            line.setLotNumber(firstNonBlank(
                    asString(of.get("traceabilityLotId")),
                    asString(of.get("lotVracId"))));
            lines.add(line);
        }
        return lines;
    }

    private String companyAddress() {
        return companyProfileReadPort.findCurrentTenantProfile()
                .map(CompanyProfileSnapshot::address)
                .orElse(null);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private int asInt(Object value, int fallback) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return value == null ? fallback : Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
