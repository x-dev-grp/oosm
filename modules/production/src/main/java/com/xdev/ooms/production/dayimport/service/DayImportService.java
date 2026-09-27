package com.xdev.ooms.production.dayimport.service;

import com.xdev.ooms.production.dayimport.dto.*;
import com.xdev.ooms.production.dayimport.model.DayImportWorkbook;
import com.xdev.ooms.production.dayimport.model.DayImportWorkbook.*;
import com.xdev.ooms.production.oilcontainer.dto.OilContainerDTO;
import com.xdev.ooms.production.oilcontainer.entity.OilContainer;
import com.xdev.ooms.production.oilcontainer.repository.OilContainerRepository;
import com.xdev.ooms.production.oilcontainer.service.OilContainerService;
import com.xdev.ooms.production.oilcontainersale.entity.OilContainerSale;
import com.xdev.ooms.production.oilsale.dto.OilSaleCreateRequest;
import com.xdev.ooms.production.oilsale.dto.OilSaleDTO;
import com.xdev.ooms.production.oilsale.entity.OilSale;
import com.xdev.ooms.production.oilsale.repository.OilSaleRepository;
import com.xdev.ooms.production.oilsale.service.OilSaleService;
import com.xdev.ooms.production.oiltransaction.dto.OilTransactionDTO;
import com.xdev.ooms.production.oiltransaction.repository.OilTransactionRepository;
import com.xdev.ooms.production.oiltransaction.service.OilTransactionService;
import com.xdev.ooms.production.storageunit.dto.StorageUnitDto;
import com.xdev.ooms.production.storageunit.entity.StorageUnit;
import com.xdev.ooms.production.storageunit.repository.StorageUnitRepo;
import com.xdev.ooms.production.supplier.dto.SupplierDto;
import com.xdev.ooms.production.supplier.entity.Supplier;
import com.xdev.ooms.production.supplier.repository.SupplierRepository;
import com.xdev.ooms.production.supplier.service.SupplierTypeService;
import com.xdev.ooms.production.unifieddelivery.dto.PaymentDTO;
import com.xdev.ooms.production.unifieddelivery.dto.UnifiedDeliveryDTO;
import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;
import com.xdev.ooms.production.unifieddelivery.repository.DeliveryRepository;
import com.xdev.ooms.production.unifieddelivery.service.UnifiedDeliveryService;
import com.xdev.ooms.production.qualitycontrol.dto.QualityControlResultDto;
import com.xdev.ooms.production.qualitycontrol.dto.QualityControlRuleDto;
import com.xdev.ooms.production.qualitycontrol.entity.QualityControlRule;
import com.xdev.ooms.production.qualitycontrol.repository.QualityControlResultRepository;
import com.xdev.ooms.production.qualitycontrol.repository.QualityControlRuleRepository;
import com.xdev.ooms.production.qualitycontrol.service.QualityControlResultService;
import com.xdev.ooms.production.qualitycontrol.service.QualityControlRuleService;
import com.xdev.ooms.sharedkernel.Enum.Currency;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import com.xdev.ooms.sharedkernel.Enum.*;
import com.xdev.ooms.sharedkernel.basetype.dto.BaseTypeDto;
import com.xdev.ooms.sharedkernel.basetype.service.GenericTypeService;
import com.xdev.ooms.sharedkernel.ports.ExpensePort;
import com.xdev.ooms.sharedkernel.ports.ExpenseRecordCommand;
import com.xdev.ooms.sharedkernel.utils.OOSMLogger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DayImportService {

    public static final String IMP_PREFIX = "[IMP:";
    public static final String SALE_IMP_PREFIX = "[IMP:sale:";

    private final DayImportLedger ledger;
    private final DayImportWorkbookReader reader;
    private final DayImportTemplateFactory templateFactory;
    private final GenericTypeService genericTypeService;
    private final SupplierTypeService supplierTypeService;
    private final SupplierRepository supplierRepository;
    private final OilContainerService oilContainerService;
    private final OilContainerRepository oilContainerRepository;
    private final UnifiedDeliveryService unifiedDeliveryService;
    private final DeliveryRepository deliveryRepository;
    private final OilTransactionService oilTransactionService;
    private final OilTransactionRepository oilTransactionRepository;
    private final StorageUnitRepo storageUnitRepo;
    private final OilSaleService oilSaleService;
    private final OilSaleRepository oilSaleRepository;
    private final ExpensePort expensePort;
    private final QualityControlRuleService qualityControlRuleService;
    private final QualityControlRuleRepository qualityControlRuleRepository;
    private final QualityControlResultService qualityControlResultService;
    private final QualityControlResultRepository qualityControlResultRepository;

    public DayImportService(
            DayImportLedger ledger,
            DayImportWorkbookReader reader,
            DayImportTemplateFactory templateFactory,
            GenericTypeService genericTypeService,
            SupplierTypeService supplierTypeService,
            SupplierRepository supplierRepository,
            OilContainerService oilContainerService,
            OilContainerRepository oilContainerRepository,
            UnifiedDeliveryService unifiedDeliveryService,
            DeliveryRepository deliveryRepository,
            OilTransactionService oilTransactionService,
            OilTransactionRepository oilTransactionRepository,
            StorageUnitRepo storageUnitRepo,
            OilSaleService oilSaleService,
            OilSaleRepository oilSaleRepository,
            ExpensePort expensePort,
            QualityControlRuleService qualityControlRuleService,
            QualityControlRuleRepository qualityControlRuleRepository,
            QualityControlResultService qualityControlResultService,
            QualityControlResultRepository qualityControlResultRepository) {
        this.ledger = ledger;
        this.reader = reader;
        this.templateFactory = templateFactory;
        this.genericTypeService = genericTypeService;
        this.supplierTypeService = supplierTypeService;
        this.supplierRepository = supplierRepository;
        this.oilContainerService = oilContainerService;
        this.oilContainerRepository = oilContainerRepository;
        this.unifiedDeliveryService = unifiedDeliveryService;
        this.deliveryRepository = deliveryRepository;
        this.oilTransactionService = oilTransactionService;
        this.oilTransactionRepository = oilTransactionRepository;
        this.storageUnitRepo = storageUnitRepo;
        this.oilSaleService = oilSaleService;
        this.oilSaleRepository = oilSaleRepository;
        this.expensePort = expensePort;
        this.qualityControlRuleService = qualityControlRuleService;
        this.qualityControlRuleRepository = qualityControlRuleRepository;
        this.qualityControlResultService = qualityControlResultService;
        this.qualityControlResultRepository = qualityControlResultRepository;
    }

    public byte[] blankTemplate() throws Exception {
        return blankTemplate("fr");
    }

    public byte[] blankTemplate(String language) throws Exception {
        DayImportAccess.requireImport();
        long start = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(getClass(), "blankTemplate");
        byte[] bytes = templateFactory.blankTemplate(language);
        OOSMLogger.logMethodExit(getClass(), "blankTemplate", bytes != null ? bytes.length + " bytes" : null);
        OOSMLogger.logPerformance(getClass(), "blankTemplate", start, System.currentTimeMillis());
        return bytes;
    }

    public byte[] sampleTemplate() throws Exception {
        return sampleTemplate("fr");
    }

    public byte[] sampleTemplate(String language) throws Exception {
        DayImportAccess.requireImport();
        long start = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(getClass(), "sampleTemplate");
        byte[] bytes = templateFactory.sampleTemplate(LocalDate.now(), language);
        OOSMLogger.logMethodExit(getClass(), "sampleTemplate", bytes != null ? bytes.length + " bytes" : null);
        OOSMLogger.logPerformance(getClass(), "sampleTemplate", start, System.currentTimeMillis());
        return bytes;
    }

    public void authorize(byte[] bytes) throws Exception {
        DayImportAccess.requireWorkbook(reader.read(new java.io.ByteArrayInputStream(bytes)));
    }

    @Transactional(readOnly = true)
    public DayImportReportDto dryRun(MultipartFile file) throws Exception {
        long start = System.currentTimeMillis();
        String name = file != null ? file.getOriginalFilename() : null;
        long size = file != null ? file.getSize() : -1;
        OOSMLogger.logMethodEntry(getClass(), "dryRun", name, size);
        try {
            try (InputStream in = file.getInputStream()) {
                DayImportReportDto report = dryRun(in);
                OOSMLogger.logBusinessEvent(getClass(), "DAY_IMPORT_DRY_RUN", summarize(report));
                OOSMLogger.logMethodExit(getClass(), "dryRun", summarize(report));
                OOSMLogger.logPerformance(getClass(), "dryRun", start, System.currentTimeMillis());
                return report;
            }
        } catch (Exception e) {
            OOSMLogger.logException(getClass(), "dryRun failed for file=" + name, e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public DayImportReportDto dryRun(InputStream in) throws Exception {
        DayImportWorkbook workbook = reader.read(in);
        return analyze(workbook, false);
    }

    @Transactional(readOnly = true)
    public DayImportReportDto dryRun(byte[] bytes) throws Exception {
        long start = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(getClass(), "dryRun(bytes)", bytes != null ? bytes.length : 0);
        try {
            try (InputStream in = new java.io.ByteArrayInputStream(bytes)) {
                DayImportReportDto report = dryRun(in);
                OOSMLogger.logBusinessEvent(getClass(), "DAY_IMPORT_DRY_RUN", summarize(report));
                OOSMLogger.logPerformance(getClass(), "dryRun(bytes)", start, System.currentTimeMillis());
                return report;
            }
        } catch (Exception e) {
            OOSMLogger.logException(getClass(), "dryRun(bytes) failed", e);
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public DayImportReportDto commit(MultipartFile file) throws Exception {
        long start = System.currentTimeMillis();
        String name = file != null ? file.getOriginalFilename() : null;
        long size = file != null ? file.getSize() : -1;
        OOSMLogger.logMethodEntry(getClass(), "commit", name, size);
        try {
            try (InputStream in = file.getInputStream()) {
                DayImportReportDto report = commit(in);
                OOSMLogger.logBusinessEvent(getClass(), "DAY_IMPORT_COMMIT", summarize(report));
                OOSMLogger.logMethodExit(getClass(), "commit", summarize(report));
                OOSMLogger.logPerformance(getClass(), "commit", start, System.currentTimeMillis());
                return report;
            }
        } catch (Exception e) {
            OOSMLogger.logException(getClass(), "commit failed for file=" + name, e);
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public DayImportReportDto commit(byte[] bytes) throws Exception {
        long start = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(getClass(), "commit(bytes)", bytes != null ? bytes.length : 0);
        try {
            try (InputStream in = new java.io.ByteArrayInputStream(bytes)) {
                DayImportReportDto report = commit(in);
                OOSMLogger.logBusinessEvent(getClass(), "DAY_IMPORT_COMMIT", summarize(report));
                OOSMLogger.logPerformance(getClass(), "commit(bytes)", start, System.currentTimeMillis());
                return report;
            }
        } catch (Exception e) {
            OOSMLogger.logException(getClass(), "commit(bytes) failed", e);
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public DayImportReportDto commit(InputStream in) throws Exception {
        DayImportAccess.requireImport();
        ledger.lockTenant();
        // Multipart streams may not be rewindable: buffer once.
        byte[] buffered = in.readAllBytes();
        DayImportWorkbook workbook = reader.read(new java.io.ByteArrayInputStream(buffered));
        DayImportReportDto report = analyze(workbook, false);
        if (!report.isCanCommit()) {
            OOSMLogger.warn(getClass(),
                    "[commit] blocked businessDate={} invalidCount={} statusCounts={}",
                    report.getBusinessDate(), report.getInvalidCount(), report.getStatusCounts());
            throw new DayImportRejectedException("Commit blocked: correct the reported errors", report);
        }
        DayImportReportDto result = analyze(reader.read(new java.io.ByteArrayInputStream(buffered)), true);
        if (!result.isCanCommit()) throw new DayImportRejectedException("Import rolled back: commit validation failed", result);
        return result;
    }

    private DayImportReportDto analyze(DayImportWorkbook wb, boolean commit) {
        DayImportAccess.requireWorkbook(wb);
        long start = System.currentTimeMillis();
        OOSMLogger.info(getClass(), "[analyze] mode={} businessDate={} tenant={}",
                commit ? "COMMIT" : "DRY_RUN",
                wb.getBusinessDate(),
                TenantContext.getCurrentTenant());

        DayImportReportDto report = new DayImportReportDto();
        report.setBusinessDate(wb.getBusinessDate());

        if (wb.getBusinessDate() == null) {
            report.addRow(ImportRowResultDto.of("ImportMeta", 1, "businessDate", ImportRowStatus.ERROR, "businessDate is required (yyyy-MM-dd)"));
            report.finalizeReport();
            OOSMLogger.warn(getClass(), "[analyze] missing businessDate");
            return report;
        }

        DayImportValidation.validate(wb, report);
        if (report.getInvalidCount() > 0) { report.finalizeReport(); return report; }
        Map<UUID, Double> tankBalances = new HashMap<>();
        Map<String, UUID> typeCache = new HashMap<>();
        Map<String, UUID> supplierCache = new HashMap<>();
        Map<String, UUID> containerCache = new HashMap<>();
        Map<UUID, Integer> containerPlannedStock = new HashMap<>();
        Map<String, UUID> receptionCache = new HashMap<>();
        Map<String, UUID> storageCache = new HashMap<>();
        Map<String, UUID> qcRuleCache = new HashMap<>();

        processNamed(wb.getRegions(), TypeCategory.REGION, "Regions", report, typeCache, commit);
        processNamed(wb.getParcels(), TypeCategory.PARCEL, "Parcels", report, typeCache, commit);
        processNamed(wb.getSupplierTypes(), TypeCategory.SUPPLIER_TYPE, "SupplierTypes", report, typeCache, commit);
        processQcRules(wb, report, qcRuleCache, commit);
        processSuppliers(wb, report, typeCache, supplierCache, commit);
        processContainers(wb, report, containerCache, containerPlannedStock, commit);
        processReceptions(wb, report, typeCache, supplierCache, receptionCache, storageCache, tankBalances, commit);
        processQcResults(wb, report, receptionCache, qcRuleCache, commit);
        processPayments(wb, report, receptionCache, commit);
        processOilSales(wb, report, supplierCache, containerCache, containerPlannedStock, storageCache, tankBalances, commit);
        processExpenses(wb, report, commit);
        if (commit) {
            Set<String> created = report.getRows().stream()
                    .filter(row -> "Receptions".equals(row.getSheet()) && row.getStatus() == ImportRowStatus.CREATE)
                    .map(row -> normalize(row.getBusinessKey())).collect(Collectors.toSet());
            finalizeImportedReceptionStatuses(wb, receptionCache, created);
        }

        report.finalizeReport();
        logAnalyzeSummary(report, commit);
        OOSMLogger.logPerformance(getClass(), commit ? "analyze(COMMIT)" : "analyze(DRY_RUN)", start, System.currentTimeMillis());
        return report;
    }

    private void logAnalyzeSummary(DayImportReportDto report, boolean commit) {
        OOSMLogger.info(getClass(),
                "[analyze] done mode={} {}",
                commit ? "COMMIT" : "DRY_RUN",
                summarize(report));
        int logged = 0;
        for (ImportRowResultDto row : report.getRows()) {
            if (row.getStatus() != ImportRowStatus.ERROR) {
                continue;
            }
            OOSMLogger.warn(getClass(), "[analyze] ERROR sheet={} row={} key={} message={}",
                    row.getSheet(), row.getRowNumber(), row.getBusinessKey(), row.getMessage());
            if (++logged >= 20) {
                OOSMLogger.warn(getClass(), "[analyze] ... {} more ERROR rows omitted",
                        report.getInvalidCount() - logged);
                break;
            }
        }
    }

    private String summarize(DayImportReportDto report) {
        if (report == null) {
            return "report=null";
        }
        return String.format(
                "businessDate=%s canCommit=%s total=%d valid=%d invalid=%d stockIn=%.3f stockOut=%.3f expense=%.2f statuses=%s",
                report.getBusinessDate(),
                report.isCanCommit(),
                report.getTotalRows(),
                report.getValidCount(),
                report.getInvalidCount(),
                report.getStockInTotal(),
                report.getStockOutTotal(),
                report.getExpenseTotal(),
                report.getStatusCounts());
    }

    private void processQcRules(DayImportWorkbook wb, DayImportReportDto report,
                                Map<String, UUID> qcRuleCache, boolean commit) {
        UUID tenantId = TenantContext.getCurrentTenant();
        for (QcRuleRow row : wb.getQcRules()) {
            if (blank(row.ruleKey)) {
                report.addRow(ImportRowResultDto.of("QcRules", row.rowNumber, "", ImportRowStatus.ERROR, "ruleKey required"));
                continue;
            }
            boolean oilQc = row.oilQc == null || row.oilQc;
            String cacheKey = normalize(row.ruleKey) + "|" + oilQc;
            Optional<QualityControlRule> existing = Optional.empty();
            if (tenantId != null) {
                existing = qualityControlRuleRepository
                        .findFirstByTenantIdAndRuleKeyIgnoreCaseAndOilQcAndIsDeletedFalse(tenantId, row.ruleKey.trim(), oilQc);
            }
            if (existing.isPresent()) {
                qcRuleCache.put(cacheKey, existing.get().getId());
                report.addRow(ImportRowResultDto.of("QcRules", row.rowNumber, row.ruleKey, ImportRowStatus.LINK_EXISTING,
                        "Exists — link only (min/max/text not updated)"));
                continue;
            }
            report.addRow(ImportRowResultDto.of("QcRules", row.rowNumber, row.ruleKey, ImportRowStatus.CREATE, "Will create QC rule"));
            if (commit) {
                QualityControlRuleDto dto = new QualityControlRuleDto();
                dto.setRuleKey(row.ruleKey.trim());
                dto.setRuleName(!blank(row.ruleName) ? row.ruleName.trim() : row.ruleKey.trim());
                dto.setOilQc(oilQc);
                dto.setDescription(row.description);
                dto.setMinValue(row.minValue);
                dto.setMaxValue(row.maxValue);
                dto.setRuleTextValue(row.ruleTextValue);
                dto.setRuleType(resolveRuleType(row.ruleType));
                QualityControlRuleDto saved = qualityControlRuleService.save(dto);
                qcRuleCache.put(cacheKey, saved.getId());
            }
        }
    }

    private RuleType resolveRuleType(String raw) {
        if (blank(raw)) {
            return RuleType.NUMERIC;
        }
        String t = raw.trim().toUpperCase(Locale.ROOT);
        if ("TEXT".equals(t) || "STRING".equals(t) || "LIST".equals(t)) {
            return RuleType.STRING;
        }
        if ("BOOL".equals(t) || "BOOLEAN".equals(t)) {
            return RuleType.BOOLEAN;
        }
        if ("NUMBER".equals(t) || "NUMERIC".equals(t) || "FLOAT".equals(t) || "DOUBLE".equals(t)) {
            return RuleType.NUMERIC;
        }
        try {
            return RuleType.valueOf(t);
        } catch (Exception e) {
            OOSMLogger.warn(getClass(), "[QcRules] unknown ruleType '{}' — defaulting to NUMERIC", raw);
            return RuleType.NUMERIC;
        }
    }

    private void processQcResults(DayImportWorkbook wb, DayImportReportDto report,
                                  Map<String, UUID> receptionCache, Map<String, UUID> qcRuleCache, boolean commit) {
        Map<UUID, List<QualityControlResultDto>> pendingByDelivery = new LinkedHashMap<>();
        for (QcResultRow row : wb.getQcResults()) {
            if (blank(row.receptionExternalRef) || blank(row.ruleKey) || blank(row.value)) {
                report.addRow(ImportRowResultDto.of("QcResults", row.rowNumber, row.ruleKey, ImportRowStatus.ERROR,
                        "receptionExternalRef, ruleKey and value required"));
                continue;
            }
            UUID deliveryId = receptionCache.get(normalize(row.receptionExternalRef));
            if (deliveryId == null) {
                String stamp = IMP_PREFIX + row.receptionExternalRef.trim() + "]";
                deliveryId = deliveryRepository.findFirstByTenantIdAndDescriptionContainingIgnoreCaseAndIsDeletedFalse(tenant(), stamp)
                        .map(UnifiedDelivery::getId).orElse(null);
            }
            ReceptionRow planned = wb.getReceptions().stream()
                    .filter(r -> normalize(r.externalRef).equals(normalize(row.receptionExternalRef))).findFirst().orElse(null);
            if (deliveryId == null && (commit || planned == null)) {
                error(report, "QcResults", row.rowNumber, row.ruleKey, "Reception not found for QC result"); continue;
            }
            Boolean oilQcHint = row.oilQc != null ? row.oilQc : planned == null ? null : "OIL".equalsIgnoreCase(planned.deliveryType);
            QualityControlRule rule = resolveQcRule(row.ruleKey, oilQcHint, qcRuleCache);
            if (rule == null && !commit) {
                QcRuleRow plannedRule = wb.getQcRules().stream().filter(r -> normalize(r.ruleKey).equals(normalize(row.ruleKey))
                        && (oilQcHint == null || oilQcHint == (r.oilQc == null || r.oilQc))).findFirst().orElse(null);
                if (plannedRule != null) {
                    rule = new QualityControlRule(); rule.setRuleKey(plannedRule.ruleKey);
                    rule.setRuleType(resolveRuleType(plannedRule.ruleType)); rule.setRuleTextValue(plannedRule.ruleTextValue);
                    rule.setMinValue(plannedRule.minValue); rule.setMaxValue(plannedRule.maxValue);
                }
            }
            if (rule == null) { error(report, "QcResults", row.rowNumber, row.ruleKey, "Unknown ruleKey"); continue; }

            String measured = row.value.trim();
            String typeMismatch = validateQcMeasuredValue(measured, rule);
            if (typeMismatch != null) {
                report.addRow(ImportRowResultDto.of("QcResults", row.rowNumber, row.ruleKey, ImportRowStatus.ERROR,
                        typeMismatch));
                continue;
            }

            report.getValidationContext().put("qc:" + normalize(row.ruleKey) + ":" + oilQcHint,
                    rule.getId() + ":" + rule.getRuleType() + ":" + rule.getMinValue() + ":" + rule.getMaxValue() + ":" + rule.getRuleTextValue());
            UUID ruleId = rule.getId();
            if (deliveryId != null && ruleId != null && qualityControlResultRepository.findByDeliveryIdWithRule(deliveryId).stream()
                    .anyMatch(r -> r.getRule() != null && ruleId.equals(r.getRule().getId()) && !measured.equals(r.getMeasuredValue()))) {
                error(report, "QcResults", row.rowNumber, row.ruleKey, "Existing QC result differs; use the QC correction workflow"); continue;
            }
            boolean already = deliveryId != null && ruleId != null && qualityControlResultRepository.findByDeliveryIdWithRule(deliveryId).stream()
                    .anyMatch(r -> r.getRule() != null && (
                            ruleId.equals(r.getRule().getId())
                                    || row.ruleKey.equalsIgnoreCase(r.getRule().getRuleKey())));
            if (already) {
                report.addRow(ImportRowResultDto.of("QcResults", row.rowNumber, row.ruleKey, ImportRowStatus.SKIP_DUPLICATE,
                        "QC result already exists for rule"));
                continue;
            }

            report.addRow(ImportRowResultDto.of("QcResults", row.rowNumber, row.ruleKey, ImportRowStatus.CREATE,
                    "Will create QC result"));
            if (commit) {
                QualityControlResultDto dto = new QualityControlResultDto();
                dto.setDeliveryId(deliveryId);
                dto.setMeasuredValue(measured);
                QualityControlRuleDto ruleDto = new QualityControlRuleDto();
                ruleDto.setId(rule.getId());
                dto.setRule(ruleDto);
                pendingByDelivery.computeIfAbsent(deliveryId, k -> new ArrayList<>()).add(dto);
            }
        }
        if (commit) {
            for (Map.Entry<UUID, List<QualityControlResultDto>> entry : pendingByDelivery.entrySet()) {
                try {
                    qualityControlResultService.saveAll(entry.getValue());
                } catch (Exception e) {
                    OOSMLogger.logException(getClass(),
                            "QC saveAll failed for delivery=" + entry.getKey(), e);
                    throw new IllegalStateException(
                            "QcResults commit failed for delivery " + entry.getKey() + ": " + e.getMessage(), e);
                }
            }
        }
    }

    /** Returns error message if measured value is incompatible with the rule type; null if OK. */
    private String validateQcMeasuredValue(String measured, QualityControlRule rule) {
        RuleType type = rule.getRuleType() != null ? rule.getRuleType() : RuleType.NUMERIC;
        switch (type) {
            case NUMERIC -> {
                try {
                    double value = Double.parseDouble(measured);
                    if (!Double.isFinite(value)) throw new NumberFormatException();
                    if (rule.getMinValue() != null && value < rule.getMinValue() - 1e-6 || rule.getMaxValue() != null && value > rule.getMaxValue() + 1e-6)
                        return "QC value is outside the rule range";
                } catch (NumberFormatException e) {
                    return "Value '" + measured + "' is not numeric but rule '" + rule.getRuleKey()
                            + "' is NUMERIC (id=" + rule.getId() + "). Use a number, or recreate the rule as STRING.";
                }
            }
            case BOOLEAN -> {
                if (!"true".equalsIgnoreCase(measured) && !"false".equalsIgnoreCase(measured)) {
                    return "Value '" + measured + "' is not boolean for rule '" + rule.getRuleKey() + "'";
                }
            }
            case STRING, RAW_STRING -> {
                if (!blank(rule.getRuleTextValue()) && Arrays.stream(rule.getRuleTextValue().split(",")).map(String::trim).noneMatch(measured::equals))
                    return "QC value is not in the rule's allowed values";
            }
            default -> {
                return "Unsupported ruleType " + type + " for rule '" + rule.getRuleKey() + "'";
            }
        }
        return null;
    }

    private QualityControlRule resolveQcRule(String ruleKey, Boolean oilQcHint, Map<String, UUID> qcRuleCache) {
        if (oilQcHint != null) {
            String cacheKey = normalize(ruleKey) + "|" + oilQcHint;
            UUID id = qcRuleCache.get(cacheKey);
            if (id != null) {
                return qualityControlRuleRepository.findByIdAndTenantIdAndIsDeletedFalse(id, tenant()).orElse(null);
            }
            Optional<QualityControlRule> found = qualityControlRuleRepository
                    .findFirstByTenantIdAndRuleKeyIgnoreCaseAndOilQcAndIsDeletedFalse(tenant(), ruleKey.trim(), oilQcHint);
            found.ifPresent(r -> qcRuleCache.put(cacheKey, r.getId()));
            return found.orElse(null);
        }
        for (boolean oilQc : new boolean[]{true, false}) {
            String cacheKey = normalize(ruleKey) + "|" + oilQc;
            UUID id = qcRuleCache.get(cacheKey);
            if (id != null) {
                return qualityControlRuleRepository.findByIdAndTenantIdAndIsDeletedFalse(id, tenant()).orElse(null);
            }
            Optional<QualityControlRule> found = qualityControlRuleRepository
                    .findFirstByTenantIdAndRuleKeyIgnoreCaseAndOilQcAndIsDeletedFalse(tenant(), ruleKey.trim(), oilQc);
            if (found.isPresent()) {
                qcRuleCache.put(cacheKey, found.get().getId());
                return found.get();
            }
        }
        return null;
    }

    private void processNamed(List<NamedRow> rows, TypeCategory category, String sheet,
                              DayImportReportDto report, Map<String, UUID> typeCache, boolean commit) {
        for (NamedRow row : rows) {
            if (blank(row.name)) {
                report.addRow(ImportRowResultDto.of(sheet, row.rowNumber, "", ImportRowStatus.ERROR, "name is required"));
                continue;
            }
            String key = category.name() + "|" + normalize(row.name);
            BaseTypeDto existing = findType(category, row.name);
            if (existing != null) {
                typeCache.put(key, existing.getId());
                report.addRow(ImportRowResultDto.of(sheet, row.rowNumber, row.name, ImportRowStatus.LINK_EXISTING,
                        "Exists — link only, fields not updated"));
                continue;
            }
            DayImportAccess.requireAdmin();
            report.addRow(ImportRowResultDto.of(sheet, row.rowNumber, row.name, ImportRowStatus.CREATE, "Will create"));
            if (commit) {
                BaseTypeDto dto = new BaseTypeDto();
                dto.setType(category);
                dto.setName(row.name.trim());
                dto.setDescription(row.description);
                BaseTypeDto saved = genericTypeService.save(dto);
                typeCache.put(key, saved.getId());
            }
        }
    }

    private void processSuppliers(DayImportWorkbook wb, DayImportReportDto report,
                                  Map<String, UUID> typeCache, Map<String, UUID> supplierCache, boolean commit) {
        for (SupplierRow row : wb.getSuppliers()) {
            String key = !blank(row.supplierKey) ? row.supplierKey.trim()
                    : (!blank(row.matriculeFiscal) ? row.matriculeFiscal.trim()
                    : (safe(row.phone) + "|" + safe(row.name) + "|" + safe(row.lastname)));
            if (blank(key) || (blank(row.name) && blank(row.supplierKey))) {
                report.addRow(ImportRowResultDto.of("Suppliers", row.rowNumber, key, ImportRowStatus.ERROR, "supplierKey or name required"));
                continue;
            }
            Supplier existing = findSupplier(row);
            if (existing != null) {
                supplierCache.put(normalize(key), existing.getId());
                if (!blank(row.supplierKey)) {
                    supplierCache.put(normalize(row.supplierKey), existing.getId());
                }
                report.addRow(ImportRowResultDto.of("Suppliers", row.rowNumber, key, ImportRowStatus.LINK_EXISTING,
                        "Exists — link only"));
                continue;
            }
            if (blank(row.name)) { error(report, "Suppliers", row.rowNumber, key, "Name is required for a new supplier"); continue; }
            report.addRow(ImportRowResultDto.of("Suppliers", row.rowNumber, key, ImportRowStatus.CREATE, "Will create supplier"));
            if (commit) {
                SupplierDto dto = new SupplierDto();
                dto.setName(row.name);
                dto.setLastname(row.lastname);
                dto.setPhone(row.phone);
                dto.setMatriculeFiscal(row.matriculeFiscal);
                if (!blank(row.regionName)) {
                    BaseTypeDto region = ensureType(TypeCategory.REGION, row.regionName, typeCache, commit);
                    dto.setRegion(region);
                }
                if (!blank(row.supplierTypeName)) {
                    BaseTypeDto type = ensureType(TypeCategory.SUPPLIER_TYPE, row.supplierTypeName, typeCache, commit);
                    dto.setGenericSupplierType(type);
                }
                SupplierDto saved = supplierTypeService.save(dto);
                supplierCache.put(normalize(key), saved.getId());
                if (!blank(row.supplierKey)) {
                    supplierCache.put(normalize(row.supplierKey), saved.getId());
                }
            }
        }
    }

    private void processContainers(DayImportWorkbook wb, DayImportReportDto report,
                                   Map<String, UUID> containerCache,
                                   Map<UUID, Integer> containerPlannedStock,
                                   boolean commit) {
        for (ContainerRow row : wb.getContainers()) {
            String name = !blank(row.name) ? row.name : row.containerKey;
            if (blank(name)) {
                report.addRow(ImportRowResultDto.of("OilContainers", row.rowNumber, "", ImportRowStatus.ERROR, "name/containerKey required"));
                continue;
            }
            OilContainer existing = oilContainerRepository.findFirstByTenantIdAndNameIgnoreCaseAndIsDeletedFalse(tenant(), name.trim()).orElse(null);
            if (existing != null) {
                containerCache.put(normalize(name), existing.getId());
                if (!blank(row.containerKey)) {
                    containerCache.put(normalize(row.containerKey), existing.getId());
                }
                report.addRow(ImportRowResultDto.of("OilContainers", row.rowNumber, name, ImportRowStatus.LINK_EXISTING,
                        "Exists — link only (stock/price not overwritten)"));
                continue;
            }
            report.addRow(ImportRowResultDto.of("OilContainers", row.rowNumber, name, ImportRowStatus.CREATE, "Will create container"));
            // Dry-run: still register keys so same-file OilSaleContainers can resolve.
            UUID pendingId = UUID.randomUUID();
            containerCache.put(normalize(name), pendingId);
            if (!blank(row.containerKey)) {
                containerCache.put(normalize(row.containerKey), pendingId);
            }
            if (commit) {
                OilContainerDTO dto = new OilContainerDTO();
                dto.setName(name.trim());
                dto.setDescription(row.containerKey);
                dto.setCapacityInLiters(row.capacityInLiters != null ? BigDecimal.valueOf(row.capacityInLiters) : BigDecimal.ZERO);
                dto.setStockQuantity(row.stockQuantity != null ? row.stockQuantity : 0);
                dto.setBuyPrice(row.buyPrice != null ? BigDecimal.valueOf(row.buyPrice) : BigDecimal.ZERO);
                dto.setSellingPrice(row.sellingPrice != null ? BigDecimal.valueOf(row.sellingPrice) : BigDecimal.ZERO);
                dto.setActive(true);
                OilContainerDTO saved = oilContainerService.save(dto);
                containerCache.put(normalize(name), saved.getId());
                if (!blank(row.containerKey)) {
                    containerCache.put(normalize(row.containerKey), saved.getId());
                }
            } else {
                // Planned stock for dry-run container-line checks (keyed by cache UUID).
                containerPlannedStock.put(pendingId, row.stockQuantity != null ? row.stockQuantity : 0);
            }
        }
    }

    private void processReceptions(DayImportWorkbook wb, DayImportReportDto report,
                                   Map<String, UUID> typeCache, Map<String, UUID> supplierCache,
                                   Map<String, UUID> receptionCache, Map<String, UUID> storageCache, Map<UUID, Double> tankBalances, boolean commit) {
        LocalDate day = wb.getBusinessDate();
        for (ReceptionRow row : wb.getReceptions()) {
            if (blank(row.externalRef)) {
                report.addRow(ImportRowResultDto.of("Receptions", row.rowNumber, "", ImportRowStatus.ERROR, "externalRef required"));
                continue;
            }
            String payload = ledger.payload(row, wb.getBusinessDate());
            if (identityConflict("RECEPTION", row.externalRef, payload, "Receptions", row.rowNumber, report)) continue;
            String stamp = IMP_PREFIX + row.externalRef.trim() + "]";
            UUID importedId = ledger.entity("RECEPTION", row.externalRef);
            Optional<UnifiedDelivery> existing = importedId == null
                    ? deliveryRepository.findFirstByTenantIdAndDescriptionContainingIgnoreCaseAndIsDeletedFalse(tenant(), stamp)
                    : deliveryRepository.findByIdAndTenantIdAndIsDeletedFalse(importedId, tenant());
            if (importedId != null && existing.isEmpty()) { error(report, "Receptions", row.rowNumber, row.externalRef, "Imported reception is unavailable; reconcile before retrying"); continue; }
            if (existing.isPresent()) {
                if (ledger.operation("RECEPTION", row.externalRef) == null) {
                    error(report, "Receptions", row.rowNumber, row.externalRef, "Historical import reference requires reconciliation"); continue;
                }
                receptionCache.put(normalize(row.externalRef), existing.get().getId());
                ImportRowResultDto skip = ImportRowResultDto.of("Receptions", row.rowNumber, row.externalRef,
                        ImportRowStatus.SKIP_DUPLICATE, "Already imported — skipped");
                report.addRow(skip);
                continue;
            }

            if (!supplierKnown(row.supplierKey, wb, supplierCache)) {
                error(report, "Receptions", row.rowNumber, row.externalRef, "Unknown supplierKey"); continue;
            }
            DeliveryType deliveryType;
            try {
                deliveryType = DeliveryType.valueOf(safe(row.deliveryType).toUpperCase(Locale.ROOT));
            } catch (Exception e) {
                report.addRow(ImportRowResultDto.of("Receptions", row.rowNumber, row.externalRef, ImportRowStatus.ERROR,
                        "Invalid deliveryType (OLIVE|OIL)"));
                continue;
            }

            Olive_Oil_Type oliveOilType;
            try {
                oliveOilType = Olive_Oil_Type.from(row.oliveOilType);
            } catch (Exception e) {
                oliveOilType = null;
            }
            if (oliveOilType == null) {
                report.addRow(ImportRowResultDto.of("Receptions", row.rowNumber, row.externalRef, ImportRowStatus.ERROR,
                        "oliveOilType required (OC|OB, HC|HB accepted) — needed for lot number"));
                continue;
            }

            if (blank(row.regionName)) {
                report.addRow(ImportRowResultDto.of("Receptions", row.rowNumber, row.externalRef, ImportRowStatus.ERROR,
                        "regionName required"));
                continue;
            }

            StorageUnit storage = null;
            if (!blank(row.storageUnitKey)) {
                storage = resolveStorage(row.storageUnitKey, storageCache);
                if (storage == null) {
                    report.addRow(ImportRowResultDto.of("Receptions", row.rowNumber, row.externalRef, ImportRowStatus.ERROR,
                            "Unknown storageUnitKey: " + row.storageUnitKey));
                    continue;
                }
            }

            if (storage != null) report.getValidationContext().put("tank:" + storage.getId(), String.valueOf(volume(storage)) + ":" + storage.getMaxCapacity());
            boolean stockIn = deliveryType == DeliveryType.OIL
                    && row.oilQuantity != null && row.oilQuantity > 0
                    && row.unitPrice != null && row.unitPrice > 0
                    && storage != null;

            ImportRowResultDto result = ImportRowResultDto.of("Receptions", row.rowNumber, row.externalRef,
                    ImportRowStatus.CREATE, stockIn ? "Create + STOCK_IN" : "Create reception");
            if (stockIn) {
                double projected = tankBalances.computeIfAbsent(storage.getId(), id -> volume(storageUnitRepo.findByIdAndTenantIdAndIsDeletedFalse(id, tenant()).orElseThrow())) + row.oilQuantity;
                if (storage.getMaxCapacity() != null && storage.getMaxCapacity() > 0 && projected > storage.getMaxCapacity() + 1e-9) {
                    error(report, "Receptions", row.rowNumber, row.externalRef, "Tank capacity exceeded"); continue;
                }
                tankBalances.put(storage.getId(), projected);
                result.setStockDelta(row.oilQuantity);
            } else if (deliveryType == DeliveryType.OIL) {
                result.setMessage("Create reception (STOCK_NONE — need oilQuantity, unitPrice, storageUnitKey)");
            }
            report.addRow(result);

            if (commit) {
                UnifiedDeliveryDTO dto = new UnifiedDeliveryDTO();
                dto.setDeliveryType(deliveryType);
                dto.setOperationType(resolveOperationType(deliveryType, row.operationType));
                if (deliveryType == DeliveryType.OLIVE) {
                    dto.setOliveType(oliveOilType);
                } else {
                    dto.setOilType(oliveOilType);
                }
                if (!blank(row.varietyName)) {
                    TypeCategory varietyCategory = deliveryType == DeliveryType.OLIVE
                            ? TypeCategory.OLIVE_VARIETY
                            : TypeCategory.OIL_VARIETY;
                    BaseTypeDto variety = ensureType(varietyCategory, row.varietyName, typeCache, true);
                    if (deliveryType == DeliveryType.OLIVE) {
                        dto.setOliveVariety(variety);
                    } else {
                        dto.setOilVariety(variety);
                    }
                }
                // Tare > 0 so olive lots start as NEW (not WAITING) like interactive forms.
                if (deliveryType == DeliveryType.OLIVE && row.poidsNet != null && row.poidsNet > 0) {
                    dto.setPoidsCamionVide(1d);
                    dto.setPoidsNet(row.poidsNet + 1d);
                }
                dto.setDeliveryDate(LocalDateTime.of(day, LocalTime.of(12, 0)));
                dto.setPoidsNet(row.poidsNet);
                dto.setOilQuantity(row.oilQuantity);
                dto.setUnitPrice(row.unitPrice);
                if (row.oilQuantity != null && row.unitPrice != null) {
                    dto.setPrice(BigDecimal.valueOf(row.oilQuantity).multiply(BigDecimal.valueOf(row.unitPrice)).doubleValue());
                }
                dto.setDescription(stamp + " " + safe(row.description));
                dto.setRegion(ensureType(TypeCategory.REGION, row.regionName, typeCache, true));
                if (!blank(row.parcelName)) {
                    dto.setParcel(ensureType(TypeCategory.PARCEL, row.parcelName, typeCache, true));
                }
                if (!blank(row.supplierKey)) {
                    UUID supplierId = supplierCache.get(normalize(row.supplierKey));
                    if (supplierId == null) {
                        Supplier s = findSupplierByKey(row.supplierKey);
                        if (s != null) supplierId = s.getId();
                    }
                    if (supplierId != null) {
                        SupplierDto sDto = new SupplierDto();
                        sDto.setId(supplierId);
                        dto.setSupplier(sDto);
                    }
                }
                if (storage != null) {
                    StorageUnitDto su = new StorageUnitDto();
                    su.setId(storage.getId());
                    dto.setStorageUnit(su);
                }
                UnifiedDeliveryDTO saved = unifiedDeliveryService.save(dto);
                receptionCache.put(normalize(row.externalRef), saved.getId());
                ledger.record("RECEPTION", row.externalRef, payload, saved.getId());

                if (stockIn) {
                    UnifiedDelivery entity = deliveryRepository.findByIdAndTenantIdAndIsDeletedFalse(saved.getId(), tenant()).orElse(null);
                    if (entity != null) {
                        if (entity.getStorageUnit() == null && storage != null) {
                            entity.setStorageUnit(storage);
                            entity = deliveryRepository.saveAndFlush(entity);
                        }
                        boolean already = oilTransactionRepository
                                .findFirstByReceptionIdAndTransactionTypeAndIsDeletedFalse(entity.getId(), TransactionType.RECEPTION_IN)
                                .isPresent();
                        if (!already) {
                            oilTransactionService.createSingleOilTransactionIn(entity);
                        }
                    }
                }
            }
        }
    }

    /**
     * Day-import rows land at OLIVE_CONTROLLED / OIL_CONTROLLED after QC, but reception history /
     * payment ledgers default-filter COMPLETED / IN_STOCK / STOCK_READY — so finalize visibility.
     */
    private void finalizeImportedReceptionStatuses(DayImportWorkbook wb, Map<String, UUID> receptionCache, Set<String> created) {
        for (ReceptionRow row : wb.getReceptions()) {
            if (blank(row.externalRef) || !created.contains(normalize(row.externalRef))) {
                continue;
            }
            UUID id = receptionCache.get(normalize(row.externalRef));
            if (id == null) {
                String stamp = IMP_PREFIX + row.externalRef.trim() + "]";
                id = deliveryRepository.findFirstByTenantIdAndDescriptionContainingIgnoreCaseAndIsDeletedFalse(tenant(), stamp)
                        .map(UnifiedDelivery::getId).orElse(null);
            }
            if (id == null) {
                continue;
            }
            UnifiedDelivery entity = deliveryRepository.findByIdAndTenantIdAndIsDeletedFalse(id, tenant()).orElse(null);
            if (entity == null || Boolean.TRUE.equals(entity.getDeleted())) {
                continue;
            }

            DeliveryType deliveryType;
            try {
                deliveryType = DeliveryType.valueOf(safe(row.deliveryType).toUpperCase(Locale.ROOT));
            } catch (Exception e) {
                deliveryType = entity.getDeliveryType();
            }
            OperationType op = resolveOperationType(deliveryType, row.operationType);
            if (entity.getOperationType() != op) {
                entity.setOperationType(op);
                deliveryRepository.save(entity);
                OOSMLogger.info(getClass(), "[finalize] corrected operationType={} for {}", op, row.externalRef);
            }

            // QC and stock services own lifecycle transitions. Do not force COMPLETED/PROD_READY.

        }
    }

    private OperationType resolveOperationType(DeliveryType deliveryType, String raw) {
        if (!blank(raw)) return OperationType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        return deliveryType == DeliveryType.OIL ? OperationType.OIL_PURCHASE : OperationType.SIMPLE_RECEPTION;
    }

    private void processPayments(DayImportWorkbook wb, DayImportReportDto report,
                                 Map<String, UUID> receptionCache, boolean commit) {
        Map<String, Double> remaining = new HashMap<>();
        for (PaymentRow row : wb.getPayments()) {
            if (blank(row.externalRef) || blank(row.receptionExternalRef) || row.amount == null || !Double.isFinite(row.amount) || row.amount <= 0) {
                error(report, "Payments", row.rowNumber, row.externalRef, "Payment reference, reception reference and positive amount required"); continue;
            }
            String payload = ledger.payload(row, row.paymentDate == null ? wb.getBusinessDate() : row.paymentDate);
            if (identityConflict("PAYMENT", row.externalRef, payload, "Payments", row.rowNumber, report)) continue;
            if (ledger.operation("PAYMENT", row.externalRef) != null) {
                report.addRow(ImportRowResultDto.of("Payments", row.rowNumber, row.externalRef, ImportRowStatus.SKIP_DUPLICATE, "Payment already imported")); continue;
            }
            String receptionKey = normalize(row.receptionExternalRef);
            UUID deliveryId = receptionCache.get(receptionKey);
            if (deliveryId == null) deliveryId = ledger.entity("RECEPTION", row.receptionExternalRef);
            UnifiedDelivery delivery = deliveryId == null
                    ? deliveryRepository.findFirstByTenantIdAndDescriptionContainingIgnoreCaseAndIsDeletedFalse(tenant(), IMP_PREFIX + row.receptionExternalRef.trim() + "]").orElse(null)
                    : deliveryRepository.findByIdAndTenantIdAndIsDeletedFalse(deliveryId, tenant()).orElse(null);
            ReceptionRow planned = wb.getReceptions().stream().filter(r -> normalize(r.externalRef).equals(receptionKey)).findFirst().orElse(null);
            if (delivery == null && (commit || planned == null)) {
                error(report, "Payments", row.rowNumber, row.externalRef, "Reception not found for payment"); continue;
            }
            if (delivery != null && !ledger.knownEntity("RECEPTION", delivery.getId())) {
                error(report, "Payments", row.rowNumber, row.externalRef, "Historical reception requires payment reconciliation before importing installments"); continue;
            }
            double available;
            if (commit || !remaining.containsKey(receptionKey)) {
                double total = delivery != null ? (delivery.getPrice() == null ? 0d : delivery.getPrice())
                        : planned.oilQuantity != null && planned.unitPrice != null ? BigDecimal.valueOf(planned.oilQuantity).multiply(BigDecimal.valueOf(planned.unitPrice)).doubleValue() : 0d;
                double paid = delivery != null && delivery.getPaidAmount() != null ? delivery.getPaidAmount() : 0d;
                available = Math.max(0, total - paid);
            } else available = remaining.get(receptionKey);
            if (row.amount > available + 0.000001) {
                error(report, "Payments", row.rowNumber, row.externalRef, "Payment exceeds remaining payable balance"); continue;
            }
            if (delivery != null) report.getValidationContext().put("payment:" + receptionKey, delivery.getId() + ":" + available);
            remaining.put(receptionKey, available - row.amount);
            report.addRow(ImportRowResultDto.of("Payments", row.rowNumber, row.externalRef, ImportRowStatus.CREATE, "Will process payment " + row.amount));
            if (commit) {
                PaymentDTO payment = new PaymentDTO();
                payment.setIdOperation(delivery.getId());
                payment.setAmount(row.amount);
                payment.setCurrency(Currency.TND);
                payment.setPaymentDate(row.paymentDate == null ? wb.getBusinessDate() : row.paymentDate);
                payment.setPaymentMethod(blank(row.paymentMethod) ? PaymentMethod.CASH : PaymentMethod.valueOf(row.paymentMethod.trim().toUpperCase(Locale.ROOT)));
                unifiedDeliveryService.processPayment(payment);
                ledger.record("PAYMENT", row.externalRef, payload, delivery.getId());
            }
        }
    }

    private void processOilSales(DayImportWorkbook wb, DayImportReportDto report,
                                 Map<String, UUID> supplierCache, Map<String, UUID> containerCache,
                                 Map<UUID, Integer> containerPlannedStock,
                                 Map<String, UUID> storageCache, Map<UUID, Double> tankBalances, boolean commit) {
        Map<String, List<OilSaleContainerRow>> linesBySale = wb.getOilSaleContainers().stream()
                .filter(l -> !blank(l.saleExternalRef))
                .collect(Collectors.groupingBy(l -> normalize(l.saleExternalRef)));

        for (OilSaleRow row : wb.getOilSales()) {
            String ref = !blank(row.invoiceNumber) ? row.invoiceNumber.trim()
                    : (!blank(row.externalRef) ? row.externalRef.trim() : "");
            if (blank(ref)) {
                report.addRow(ImportRowResultDto.of("OilSales", row.rowNumber, "", ImportRowStatus.ERROR,
                        "externalRef or invoiceNumber required"));
                continue;
            }

            String payload = ledger.payload(row, wb.getBusinessDate(), linesBySale.getOrDefault(normalize(row.externalRef), List.of()));
            if (identityConflict("SALE", ref, payload, "OilSales", row.rowNumber, report)) continue;
            if (!supplierKnown(row.supplierKey, wb, supplierCache)) {
                error(report, "OilSales", row.rowNumber, ref, "Unknown supplierKey"); continue;
            }
            UUID importedSaleId = ledger.entity("SALE", ref);
            Optional<OilSale> existing = importedSaleId == null ? Optional.empty() : oilSaleRepository.findByIdAndTenantIdAndIsDeletedFalse(importedSaleId, tenant());
            if (importedSaleId != null && existing.isEmpty()) { error(report, "OilSales", row.rowNumber, ref, "Imported sale is unavailable; reconcile before retrying"); continue; }
            if (existing.isEmpty() && !blank(row.invoiceNumber)) {
                existing = oilSaleRepository.findFirstByTenantIdAndInvoiceNumberIgnoreCaseAndIsDeletedFalse(tenant(), row.invoiceNumber.trim());
            }
            if (existing.isEmpty() && !blank(row.externalRef)) {
                existing = oilSaleRepository.findFirstByTenantIdAndDescriptionContainingIgnoreCaseAndIsDeletedFalse(tenant(),
                        SALE_IMP_PREFIX + row.externalRef.trim() + "]");
            }
            if (existing.isPresent()) {
                if (ledger.operation("SALE", ref) == null) { error(report, "OilSales", row.rowNumber, ref, "Historical import reference requires reconciliation"); continue; }
                report.addRow(ImportRowResultDto.of("OilSales", row.rowNumber, ref, ImportRowStatus.SKIP_DUPLICATE,
                        "Sale already imported — skipped"));
                continue;
            }

            List<OilSaleContainerRow> lines = linesBySale.getOrDefault(normalize(row.externalRef), List.of());
            boolean hasContainers = !lines.isEmpty();
            double qty = row.quantity != null ? row.quantity : 0d;
            if (qty <= 0 && !hasContainers) {
                report.addRow(ImportRowResultDto.of("OilSales", row.rowNumber, ref, ImportRowStatus.ERROR,
                        "quantity > 0 or container lines required"));
                continue;
            }
            if (blank(row.storageUnitKey)) {
                report.addRow(ImportRowResultDto.of("OilSales", row.rowNumber, ref, ImportRowStatus.ERROR,
                        "storageUnitKey required"));
                continue;
            }
            StorageUnit storage = resolveStorage(row.storageUnitKey, storageCache);
            if (storage == null) {
                report.addRow(ImportRowResultDto.of("OilSales", row.rowNumber, ref, ImportRowStatus.ERROR,
                        "Unknown storageUnitKey: " + row.storageUnitKey));
                continue;
            }
            report.getValidationContext().put("tank:" + storage.getId(), String.valueOf(volume(storage)) + ":" + storage.getMaxCapacity());
            if (qty > 0) {
                double available = tankBalances.computeIfAbsent(storage.getId(), id -> volume(storage));
                if (available + 1e-9 < qty) {
                    report.addRow(ImportRowResultDto.of("OilSales", row.rowNumber, ref, ImportRowStatus.ERROR,
                            String.format("Insufficient tank volume (available=%.3f, required=%.3f)", available, qty)));
                    continue;
                }
            }

            BigDecimal saleTotal = BigDecimal.valueOf(qty).multiply(BigDecimal.valueOf(row.unitPrice == null ? 0 : row.unitPrice));
            boolean containerOk = true;
            for (OilSaleContainerRow line : lines) {
                UUID cid = resolveContainerId(line.containerKey, containerCache);
                if (cid == null) {
                    report.addRow(ImportRowResultDto.of("OilSaleContainers", line.rowNumber, line.containerKey,
                            ImportRowStatus.ERROR, "Unknown containerKey"));
                    containerOk = false;
                    continue;
                }
                OilContainer c = oilContainerRepository.findByIdAndTenantIdAndIsDeletedFalse(cid, tenant()).orElse(null);
                if (c != null) report.getValidationContext().put("container:" + cid, c.getStockQuantity() + ":" + c.getSellingPrice());
                int count = line.count != null ? line.count : 0;
                Integer stock = containerPlannedStock.computeIfAbsent(cid, id -> c != null && c.getStockQuantity() != null ? c.getStockQuantity() : 0);
                if (count <= 0) {
                    report.addRow(ImportRowResultDto.of("OilSaleContainers", line.rowNumber, line.containerKey,
                            ImportRowStatus.ERROR, "Invalid count"));
                    containerOk = false;
                } else if (stock != null && stock < count) {
                    report.addRow(ImportRowResultDto.of("OilSaleContainers", line.rowNumber, line.containerKey,
                            ImportRowStatus.ERROR, "Insufficient container stock"));
                    containerOk = false;
                } else if (c == null && !containerPlannedStock.containsKey(cid)) {
                    report.addRow(ImportRowResultDto.of("OilSaleContainers", line.rowNumber, line.containerKey,
                            ImportRowStatus.ERROR, "Unknown containerKey"));
                    containerOk = false;
                } else {
                    ImportRowResultDto lr = ImportRowResultDto.of("OilSaleContainers", line.rowNumber, line.containerKey,
                            ImportRowStatus.CREATE, "CONTAINER_OUT x" + count);
                    report.addRow(lr);
                    containerPlannedStock.put(cid, stock - count);
                    BigDecimal price = c == null ? wb.getContainers().stream()
                            .filter(r -> cid.equals(containerCache.get(normalize(r.containerKey))) || cid.equals(containerCache.get(normalize(r.name))))
                            .map(r -> BigDecimal.valueOf(r.sellingPrice == null ? 0 : r.sellingPrice)).findFirst().orElse(BigDecimal.ZERO)
                            : c.getSellingPrice() == null ? BigDecimal.ZERO : c.getSellingPrice();
                    saleTotal = saleTotal.add(price.multiply(BigDecimal.valueOf(count)));
                }
            }
            if (!containerOk) {
                continue;
            }
            if (row.paidAmount != null && BigDecimal.valueOf(row.paidAmount).compareTo(saleTotal) > 0) {
                error(report, "OilSales", row.rowNumber, ref, "Paid amount exceeds sale total"); continue;
            }

            ImportRowResultDto saleRow = ImportRowResultDto.of("OilSales", row.rowNumber, ref, ImportRowStatus.CREATE,
                    qty > 0 ? "Create sale + STOCK_OUT" : "Create container-only sale");
            if (qty > 0) {
                saleRow.setStockDelta(-qty);
            }
            report.addRow(saleRow);
            if (qty > 0) tankBalances.put(storage.getId(), tankBalances.get(storage.getId()) - qty);

            if (commit) {
                OilSaleCreateRequest req = new OilSaleCreateRequest();
                req.setStorageUnit(storage.getId().toString());
                req.setQuantity(BigDecimal.valueOf(Math.max(qty, hasContainers && qty <= 0 ? 0d : qty)));
                if (req.getQuantity().signum() <= 0 && hasContainers) {
                    req.setQuantity(BigDecimal.ZERO);
                }
                req.setUnitPrice(BigDecimal.valueOf(row.unitPrice != null ? row.unitPrice : 0d));
                try {
                    req.setCurrency(Currency.valueOf(safe(row.currency).isBlank() ? "TND" : row.currency.trim().toUpperCase(Locale.ROOT)));
                } catch (Exception e) {
                    req.setCurrency(Currency.TND);
                }
                try {
                    req.setPaymentMethod(PaymentMethod.valueOf(safe(row.paymentMethod).isBlank() ? "CASH" : row.paymentMethod.trim().toUpperCase(Locale.ROOT)));
                } catch (Exception e) {
                    req.setPaymentMethod(PaymentMethod.CASH);
                }
                try {
                    req.setQualityGrade(QualityGrades.valueOf(safe(row.qualityGrade).isBlank() ? "EXTRA_VIRGIN" : row.qualityGrade.trim().toUpperCase(Locale.ROOT)));
                } catch (Exception e) {
                    req.setQualityGrade(QualityGrades.EXTRA_VIRGIN);
                }
                req.setSaleDate(LocalDateTime.of(wb.getBusinessDate(), LocalTime.of(12, 0)));
                req.setInvoiceNumber(!blank(row.invoiceNumber) ? row.invoiceNumber.trim() : null);
                req.setDescription(SALE_IMP_PREFIX + safe(row.externalRef) + "] " + safe(row.description));
                req.setPaidAmount(row.paidAmount);
                if (!blank(row.supplierKey)) {
                    UUID sid = supplierCache.get(normalize(row.supplierKey));
                    if (sid == null) {
                        Supplier s = findSupplierByKey(row.supplierKey);
                        if (s != null) sid = s.getId();
                    }
                    if (sid != null) {
                        req.setSupplier(sid.toString());
                    }
                }
                if (hasContainers) {
                    List<OilContainerSale> cs = new ArrayList<>();
                    for (OilSaleContainerRow line : lines) {
                        UUID cid = resolveContainerId(line.containerKey, containerCache);
                        OilContainerSale ocs = new OilContainerSale();
                        ocs.setId(cid);
                        ocs.setCount(line.count);
                        cs.add(ocs);
                    }
                    req.setContainerSales(cs);
                }

                OilSaleDTO saved = oilSaleService.createWithContainers(req);
                ledger.record("SALE", ref, payload, saved.getId());
                if (qty > 0 && saved.getId() != null) {
                    oilTransactionRepository.findByOilSaleIdAndIsDeletedFalse(saved.getId()).ifPresent(tx -> {
                        OilTransactionDTO approve = new OilTransactionDTO();
                        approve.setId(tx.getId());
                        StorageUnitDto src = new StorageUnitDto();
                        src.setId(storage.getId());
                        approve.setStorageUnitSource(src);
                        oilTransactionService.approveOilTransaction2(approve);
                    });
                }
            }
        }
    }

    private void processExpenses(DayImportWorkbook wb, DayImportReportDto report, boolean commit) {
        double expenseTotal = report.getExpenseTotal();
        for (ExpenseRow row : wb.getExpenses()) {
            if (blank(row.externalRef) || row.amount == null || row.amount <= 0) {
                report.addRow(ImportRowResultDto.of("Expenses", row.rowNumber, row.externalRef, ImportRowStatus.ERROR,
                        "externalRef and positive amount required"));
                continue;
            }
            String payload = ledger.payload(row, wb.getBusinessDate());
            if (identityConflict("EXPENSE", row.externalRef, payload, "Expenses", row.rowNumber, report)) continue;
            if (ledger.operation("EXPENSE", row.externalRef) == null && expensePort.existsByExternalReference(row.externalRef.trim())) {
                error(report, "Expenses", row.rowNumber, row.externalRef, "Historical import reference requires reconciliation"); continue;
            }
            if (ledger.operation("EXPENSE", row.externalRef) != null) {
                report.addRow(ImportRowResultDto.of("Expenses", row.rowNumber, row.externalRef, ImportRowStatus.SKIP_DUPLICATE,
                        "Expense already imported"));
                continue;
            }
            expenseTotal += row.amount;
            report.addRow(ImportRowResultDto.of("Expenses", row.rowNumber, row.externalRef, ImportRowStatus.CREATE,
                    "Will create expense " + row.amount));
            if (commit) {
                ExpenseCategory category = ExpenseCategory.OTHER;
                try {
                    if (!blank(row.category)) {
                        category = ExpenseCategory.valueOf(row.category.trim().toUpperCase(Locale.ROOT));
                    }
                } catch (Exception ignored) {
                }
                PaymentMethod pm = PaymentMethod.CASH;
                try {
                    if (!blank(row.paymentMethod)) {
                        pm = PaymentMethod.valueOf(row.paymentMethod.trim().toUpperCase(Locale.ROOT));
                    }
                } catch (Exception ignored) {
                }
                expensePort.record(new ExpenseRecordCommand(
                        row.vendor,
                        row.amount,
                        category,
                        pm,
                        row.object,
                        row.purchaseNature,
                        row.notes,
                        row.externalRef.trim(),
                        wb.getBusinessDate()
                ));
                ledger.record("EXPENSE", row.externalRef, payload, null);
            }
        }
        report.setExpenseTotal(expenseTotal);
    }

    private BaseTypeDto findType(TypeCategory category, String name) {
        return genericTypeService.getAllTypes(category).stream()
                .filter(t -> tenant().equals(t.getTenantId()) && !Boolean.TRUE.equals(t.getDeleted()))
                .filter(t -> t.getName() != null && t.getName().equalsIgnoreCase(name.trim()))
                .findFirst()
                .map(t -> {
                    BaseTypeDto dto = new BaseTypeDto();
                    dto.setId(t.getId());
                    dto.setName(t.getName());
                    dto.setType(t.getType());
                    return dto;
                })
                .orElse(null);
    }

    private BaseTypeDto ensureType(TypeCategory category, String name, Map<String, UUID> typeCache, boolean commit) {
        String key = category.name() + "|" + normalize(name);
        if (typeCache.containsKey(key)) {
            BaseTypeDto dto = new BaseTypeDto();
            dto.setId(typeCache.get(key));
            dto.setName(name);
            dto.setType(category);
            return dto;
        }
        BaseTypeDto existing = findType(category, name);
        if (existing != null) {
            typeCache.put(key, existing.getId());
            return existing;
        }
        if (!commit) {
            return null;
        }
        DayImportAccess.requireAdmin();
        BaseTypeDto dto = new BaseTypeDto();
        dto.setType(category);
        dto.setName(name.trim());
        BaseTypeDto saved = genericTypeService.save(dto);
        typeCache.put(key, saved.getId());
        return saved;
    }

    private Supplier findSupplier(SupplierRow row) {
        if (!blank(row.matriculeFiscal)) {
            return supplierRepository.findFirstByTenantIdAndMatriculeFiscalIgnoreCaseAndIsDeletedFalse(tenant(), row.matriculeFiscal.trim()).orElse(null);
        }
        if (!blank(row.phone) && !blank(row.name)) {
            return supplierRepository.findFirstByTenantIdAndPhoneAndNameIgnoreCaseAndLastnameIgnoreCaseAndIsDeletedFalse(tenant(),
                    row.phone.trim(), safe(row.name), safe(row.lastname)).orElse(null);
        }
        return findSupplierByKey(row.supplierKey);
    }

    private Supplier findSupplierByKey(String supplierKey) {
        if (blank(supplierKey)) return null;
        // supplierKey stored only in import context — match phone or name fallback
        return supplierRepository.findAllByTenantIdAndIsDeletedFalse(tenant()).stream()
                .filter(s -> !Boolean.TRUE.equals(s.getDeleted()))
                .filter(s -> supplierKey.equalsIgnoreCase(s.getPhone())
                        || supplierKey.equalsIgnoreCase(s.getMatriculeFiscal())
                        || supplierKey.equalsIgnoreCase(s.getFullName())
                        || supplierKey.equalsIgnoreCase(safe(s.getName()) + "-" + safe(s.getLastname())))
                .findFirst()
                .orElse(null);
    }

    private StorageUnit resolveStorage(String key, Map<String, UUID> storageCache) {
        if (blank(key)) return null;
        String n = normalize(key);
        if (storageCache.containsKey(n)) {
            return storageUnitRepo.findByIdAndTenantIdAndIsDeletedFalse(storageCache.get(n), tenant()).orElse(null);
        }
        try {
            UUID id = UUID.fromString(key.trim());
            StorageUnit su = storageUnitRepo.findByIdAndTenantIdAndIsDeletedFalse(id, tenant()).orElse(null);
            if (su != null) {
                storageCache.put(n, su.getId());
            }
            return su;
        } catch (Exception ignored) {
        }
        StorageUnit su = storageUnitRepo.findFirstByTenantIdAndNameIgnoreCaseAndIsDeletedFalse(tenant(), key.trim()).orElse(null);
        if (su != null) {
            storageCache.put(n, su.getId());
        }
        return su;
    }

    private UUID resolveContainerId(String key, Map<String, UUID> containerCache) {
        if (blank(key)) return null;
        String n = normalize(key);
        if (containerCache.containsKey(n)) {
            return containerCache.get(n);
        }
        return oilContainerRepository.findFirstByTenantIdAndNameIgnoreCaseAndIsDeletedFalse(tenant(), key.trim())
                .map(c -> {
                    containerCache.put(n, c.getId());
                    return c.getId();
                })
                .orElse(null);
    }

    private void error(DayImportReportDto report, String sheet, int row, String key, String message) {
        report.addRow(ImportRowResultDto.of(sheet, row, key, ImportRowStatus.ERROR, message));
    }

    private boolean identityConflict(String kind, String ref, String payload, String sheet, int row, DayImportReportDto report) {
        String existing = ledger.operation(kind, ref);
        if (existing != null && !existing.equals(payload)) {
            error(report, sheet, row, ref, "External reference was already used with different values");
            return true;
        }
        return false;
    }

    private boolean supplierKnown(String key, DayImportWorkbook wb, Map<String, UUID> cache) {
        return blank(key) || cache.containsKey(normalize(key)) || findSupplierByKey(key) != null
                || wb.getSuppliers().stream().anyMatch(row -> normalize(row.supplierKey).equals(normalize(key)));
    }

    private double volume(StorageUnit tank) { return tank.getCurrentVolume() == null ? 0d : tank.getCurrentVolume(); }

    private UUID tenant() {
        return DayImportAccess.tenant();
    }

    private boolean blank(String s) {
        return s == null || s.isBlank();
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private String normalize(String s) {
        return safe(s).toLowerCase(Locale.ROOT);
    }
}
