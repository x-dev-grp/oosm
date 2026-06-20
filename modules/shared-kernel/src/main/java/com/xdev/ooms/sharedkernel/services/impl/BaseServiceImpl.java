package com.xdev.ooms.sharedkernel.services.impl;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.xdev.ooms.sharedkernel.communicator.models.common.dtos.apiDTOs.models.SearchResponse;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import com.xdev.ooms.sharedkernel.models.*;
import com.xdev.ooms.sharedkernel.qr.CodeGenerator;
import com.xdev.ooms.sharedkernel.qr.Component.QrConfig;
import com.xdev.ooms.sharedkernel.qr.model.QrCodeInfo;
import com.xdev.ooms.sharedkernel.qr.model.QrResolveResponse;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.BaseService;
import com.xdev.ooms.sharedkernel.services.GlobalCodeSearchContributor;
import com.xdev.ooms.sharedkernel.services.utils.SearchSpecificationBuilder;
import com.xdev.ooms.sharedkernel.utils.AuditHelper;
import com.xdev.ooms.sharedkernel.utils.BusinessCodeGenerator;
import com.xdev.ooms.sharedkernel.utils.OSMLogger;
import jakarta.persistence.EntityNotFoundException;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.*;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.history.Revision;
import org.springframework.data.history.Revisions;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@SuppressWarnings("unchecked")
public abstract class BaseServiceImpl<E extends BaseEntity, INDTO extends BaseDto<E>, OUTDTO extends BaseDto<E>> implements BaseService<E, INDTO, OUTDTO>, GlobalCodeSearchContributor {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter OFFSET_DATE_TIME_FORMATTER1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssXXX");
    public static final DateTimeFormatter ZONED_DATE_TIME_FORMATTER1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
    private static final int MAX_RECORDS_PER_DOCUMENT = 1000;
    protected final BaseRepository<E> repository;
    protected final ModelMapper modelMapper;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    protected Class<E> entityClass;
    protected Class<INDTO> inDTOClass;
    protected Class<OUTDTO> outDTOClass;
    @Autowired
    private SearchSpecificationBuilder<E> specificationBuilder;

    private ExportDetails currentExportDetails;
    @Autowired(required = false)
    private CodeGenerator codeGenerator;

    @Autowired(required = false)
    private QrConfig qrConfig;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    @Autowired(required = false)
    private BusinessCodeGenerator businessCodeGenerator;

    // Constructor now only needs repository and modelMapper


    // Optional: keep a convenience constructor with all dependencies (for services that want to inject them explicitly)
    protected BaseServiceImpl(BaseRepository<E> repository, CodeGenerator codeGenerator, ModelMapper modelMapper) {
        this(repository, modelMapper);
        this.codeGenerator = codeGenerator;
    }

    protected BaseServiceImpl(BaseRepository<E> repository, CodeGenerator codeGenerator, QrConfig qrConfig, ModelMapper modelMapper) {
        this(repository, modelMapper);
        this.codeGenerator = codeGenerator;
        this.qrConfig = qrConfig;
    }

    protected BaseServiceImpl(BaseRepository<E> repository, ModelMapper modelMapper) {
        this.repository = repository;
        this.modelMapper = modelMapper;
        this.entityClass = (Class<E>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        this.inDTOClass = (Class<INDTO>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[1];
        this.outDTOClass = (Class<OUTDTO>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[2];
    }

    private static double toDouble(Object v) {
        if (v == null) return 0.0;
        if (v instanceof Number n) {
            return n.doubleValue();
        }
        return 0.0;
    }

    @Override
    public Class<E> getEntityClass() {
        return this.entityClass;
    }

    protected String generateBusinessCode(String codeFieldName) {
        return generateBusinessCode(codeFieldName, entityClass.getSimpleName());
    }

    protected String generateBusinessCode(String codeFieldName, String prefixSource) {
        if (businessCodeGenerator != null) {
            return businessCodeGenerator.generate(entityClass, codeFieldName, prefixSource);
        }

        String prefix = prefixSource == null ? entityClass.getSimpleName() : prefixSource;
        String cleanedPrefix = prefix.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
        if (cleanedPrefix.length() < 2) {
            cleanedPrefix = entityClass.getSimpleName().toUpperCase(Locale.ROOT).substring(0, 2);
        } else {
            cleanedPrefix = cleanedPrefix.substring(0, 2);
        }

        int yearSuffix = Year.now().getValue() % 100;
        long nextSequence = repository.count() + 1;
        return String.format(Locale.ROOT, "%s-%02d-%03d", cleanedPrefix, yearSuffix, nextSequence);
    }

    @Override
    public Class<INDTO> getInDTOClass() {
        return this.inDTOClass;
    }

    @Override
    public Class<OUTDTO> getOutDTOClass() {
        return this.outDTOClass;
    }

    @Transactional(readOnly = true)
    @Override
    public OUTDTO findById(UUID id) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "findById", id);

        try {
            Optional<E> data = repository.findByIdAndIsDeletedFalse(id);
            if (data.isEmpty()) {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.WARN, "Entity not found with ID: {}", id);
                throw new EntityNotFoundException("Entity not found with this id " + id);
            } else {
                OUTDTO result = modelMapper.map(data.get(), outDTOClass);
                OSMLogger.logMethodExit(this.getClass(), "findById", result);
                OSMLogger.logPerformance(this.getClass(), "findById", startTime, System.currentTimeMillis());
                OSMLogger.logDataAccess(this.getClass(), "READ", entityClass.getSimpleName());
                return result;
            }
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error finding entity by ID: " + id, e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<OUTDTO> findAll() {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "findAll");

        try {
            UUID tenantId = TenantContext.getCurrentTenant();
            List<E> data = repository.findAllByTenantIdAndIsDeletedFalse(tenantId);
            List<OUTDTO> result = data.stream().map(item -> modelMapper.map(item, outDTOClass)).toList();
            OSMLogger.logMethodExit(this.getClass(), "findAll", "Found " + result.size() + " entities");
            OSMLogger.logPerformance(this.getClass(), "findAll", startTime, System.currentTimeMillis());
            OSMLogger.logDataAccess(this.getClass(), "READ_ALL", entityClass.getSimpleName());
            return result;
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error finding all entities", e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Page<OUTDTO> findAll(int page, int size, String sort, String direction) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "findAll", page, size, sort, direction);

        try {
            UUID tenantId = TenantContext.getCurrentTenant();
            Sort.Direction sortDirection = Sort.Direction.fromString(direction);  // "ASC" or "DESC"
            Sort sortObject = Sort.by(sortDirection, sort);  // Sort by the field and direction
            Pageable pageable = PageRequest.of(page, size, sortObject);
            Page<E> data = repository.findAllByTenantIdAndIsDeletedFalse(tenantId, pageable);

            Page<OUTDTO> result = data.map(item -> modelMapper.map(item, outDTOClass));
            OSMLogger.logMethodExit(this.getClass(), "findAll", "Page " + page + " with " + result.getContent().size() + " entities");
            OSMLogger.logPerformance(this.getClass(), "findAll", startTime, System.currentTimeMillis());
            OSMLogger.logDataAccess(this.getClass(), "READ_PAGEABLE", entityClass.getSimpleName());
            return result;
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error finding entities with pagination", e);
            throw e;
        }
    }

    @Override
    public OUTDTO save(INDTO request) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "save", request);

        try {
            if (request == null) {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.WARN, "Save request is null");
                return null;
            } else {
                E entity = this.modelMapper.map(request, this.entityClass);
                resolveEntityRelations(entity);
                AuditHelper.applyAuditOnCreate(entity);

                E savedEntity = this.repository.save(entity);
                savedEntity = ensureQrCodeIfSupported(savedEntity);
                OUTDTO result = this.modelMapper.map(savedEntity, this.outDTOClass);

                OSMLogger.logMethodExit(this.getClass(), "save", result);
                OSMLogger.logPerformance(this.getClass(), "save", startTime, System.currentTimeMillis());
                OSMLogger.logDataAccess(this.getClass(), "CREATE", entityClass.getSimpleName());
                OSMLogger.logBusinessEvent(this.getClass(), "ENTITY_SAVED", "Saved entity with ID: " + savedEntity.getId());

                return result;
            }
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error saving entity", e);
            throw e;
        }
    }

    @Override
    public List<OUTDTO> save(List<INDTO> request) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "save", "List with " + (request != null ? request.size() : 0) + " items");

        try {
            if (request != null && !request.isEmpty()) {
                List<E> entities = request.stream().map((item) -> this.modelMapper.map(item, this.entityClass)).toList();

                entities.forEach(this::resolveEntityRelations);

                entities = this.repository.saveAll(entities);
                List<OUTDTO> result = entities.stream().map(item -> {
                    AuditHelper.applyAuditOnCreate(item);  // side effect
                    return this.modelMapper.map(item, this.outDTOClass); // return value
                }).toList();

                OSMLogger.logMethodExit(this.getClass(), "save", "Saved " + result.size() + " entities");
                OSMLogger.logPerformance(this.getClass(), "save", startTime, System.currentTimeMillis());
                OSMLogger.logDataAccess(this.getClass(), "CREATE_BATCH", entityClass.getSimpleName());
                OSMLogger.logBusinessEvent(this.getClass(), "ENTITIES_SAVED", "Saved " + result.size() + " entities");

                return result;
            } else {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.WARN, "Save request list is empty or null");
                return Collections.emptyList();
            }
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error saving entities", e);
            throw e;
        }
    }

    @Override
    public OUTDTO update(INDTO request) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "update", request);

        try {
            if (request != null && request.getId() != null) {
                Optional<E> existedOptEntity = this.repository.findById(request.getId());
                if (existedOptEntity.isEmpty()) {
                    OSMLogger.log(this.getClass(), OSMLogger.LogLevel.WARN, "Entity with ID {} not found for update", request.getId());
                    return null;
                } else {
                    E existedEntity = existedOptEntity.get();
                    AuditHelper.applyAuditOnCreate(existedEntity);
                    UUID externalId = existedEntity.getExternalId();// side effect
                    this.modelMapper.map(request, existedEntity);
                    existedEntity.setExternalId(externalId);
                    resolveEntityRelations(existedEntity);

                    E updatedEntity = this.repository.save(existedEntity);
                    OUTDTO result = this.modelMapper.map(updatedEntity, this.outDTOClass);

                    OSMLogger.logMethodExit(this.getClass(), "update", result);
                    OSMLogger.logPerformance(this.getClass(), "update", startTime, System.currentTimeMillis());
                    OSMLogger.logDataAccess(this.getClass(), "UPDATE", entityClass.getSimpleName());
                    OSMLogger.logBusinessEvent(this.getClass(), "ENTITY_UPDATED", "Updated entity with ID: " + updatedEntity.getId());

                    return result;
                }
            } else {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.WARN, "Update request or ID is null: {}", request);
                return null;
            }
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error updating entity", e);
            throw e;
        }
    }

    @Override
    public void resolveEntityRelations(E entity) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "resolveEntityRelations", entity);

        try {
            // This method is meant to be overridden by subclasses
            // Default implementation does nothing
            OSMLogger.logMethodExit(this.getClass(), "resolveEntityRelations");
            OSMLogger.logPerformance(this.getClass(), "resolveEntityRelations", startTime, System.currentTimeMillis());
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error resolving entity relations", e);
            throw e;
        }
    }

    @Override
    public void remove(UUID id) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "remove", id);

        try {
            if (id != null) {
                repository.deleteById(id);
                OSMLogger.logMethodExit(this.getClass(), "remove");
                OSMLogger.logPerformance(this.getClass(), "remove", startTime, System.currentTimeMillis());
                OSMLogger.logDataAccess(this.getClass(), "REMOVE", entityClass.getSimpleName());
                OSMLogger.logBusinessEvent(this.getClass(), "ENTITY_REMOVED", "Removed entity with ID: " + id);
            } else {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.WARN, "Remove ID is null");
            }
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error removing entity with ID: " + id, e);
            throw e;
        }
    }

    @Override
    public OUTDTO delete(UUID id) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "delete", id);

        try {
            if (id == null) {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.WARN, "Delete ID is null: {}", id);
                return null;
            }
            E entity = repository.findById(id).orElse(null);
            if (entity == null) {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.WARN, "Entity with ID {} not found for deletion", id);
                return null;
            }

            entity.setDeleted(true);
            E updatedEntity = repository.save(entity);
            OUTDTO result = modelMapper.map(updatedEntity, outDTOClass);

            OSMLogger.logMethodExit(this.getClass(), "delete", result);
            OSMLogger.logPerformance(this.getClass(), "delete", startTime, System.currentTimeMillis());
            OSMLogger.logDataAccess(this.getClass(), "DELETE", entityClass.getSimpleName());
            OSMLogger.logBusinessEvent(this.getClass(), "ENTITY_DELETED", "Deleted entity with ID: " + id);

            return result;
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error deleting entity with ID: " + id, e);
            throw e;
        }
    }

    @Override
    public void removeAll(Collection<INDTO> entities) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "removeAll", "Collection with " + (entities != null ? entities.size() : 0) + " items");

        try {
            if (entities != null) {
                List<E> entitiesToDelete = entities.stream().map(item -> modelMapper.map(item, entityClass)).toList();
                repository.deleteAll(entitiesToDelete);
                OSMLogger.logMethodExit(this.getClass(), "removeAll");
                OSMLogger.logPerformance(this.getClass(), "removeAll", startTime, System.currentTimeMillis());
                OSMLogger.logDataAccess(this.getClass(), "REMOVE_ALL", entityClass.getSimpleName());
                OSMLogger.logBusinessEvent(this.getClass(), "ENTITIES_REMOVED", "Removed " + entitiesToDelete.size() + " entities");
            } else {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.WARN, "Entities to delete are null");
            }
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error removing entities", e);
            throw e;
        }
    }





    @Override
    public SearchResponse<E, OUTDTO> search(SearchData searchData) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "search", searchData);

        try {
            int page = (searchData.getPage() != null && searchData.getPage() >= 0) ? searchData.getPage() : 0;
            int size = (searchData.getSize() != null && searchData.getSize() > 0) ? searchData.getSize() : 10;
            Sort.Direction direction = ("DESC".equalsIgnoreCase(searchData.getOrder())) ? Sort.Direction.DESC : Sort.Direction.ASC;
            String sort = (searchData.getSort() != null && !searchData.getSort().isBlank()) ? searchData.getSort() : "createdDate";
            Pageable pageable = PageRequest.of(page, size, direction, sort);
            if (searchData.isFilterTenant()) {
                SearchDetails details = new SearchDetails();
                details.setEqualValue(TenantContext.getCurrentTenant());
                if (searchData.getSearchData() != null) {
                    if (searchData.getSearchData().getSearch() == null) {
                        searchData.getSearchData().setSearch(new HashMap<>());
                    }
                    searchData.getSearchData().getSearch().put("tenantId", details);
                }
            }
            Specification<E> spec = null;
            if (searchData.getSearchData() != null) {
                spec = specificationBuilder.buildSpecification(searchData.getSearchData());
            }
            Page<E> result;
            Page<E> totalPagesResult;
            boolean needTotals = searchData.getToCalculateTotal() != null && !searchData.getToCalculateTotal().isEmpty();

            if (spec != null) {
                result = repository.findAll(spec, pageable);

                if (needTotals) {
                    // ✅ size must be >= 1
                    int pgbSize = (int) Math.max(1L, Math.min(Integer.MAX_VALUE, result.getTotalElements()));
                    Pageable pgb = PageRequest.of(0, pgbSize, direction, sort);
                    totalPagesResult = repository.findAll(spec, pgb);
                } else {
                    totalPagesResult = null;
                }
            } else {
                if (searchData.isFilterTenant()) {
                    result = repository.findAllByTenantIdAndIsDeletedFalse(TenantContext.getCurrentTenant(), pageable);
                    if (needTotals) {
                        int pgbSize = (int) Math.max(1L, Math.min(Integer.MAX_VALUE, result.getTotalElements()));
                        Pageable pgb = PageRequest.of(0, pgbSize, direction, sort);
                        totalPagesResult = repository.findAllByTenantIdAndIsDeletedFalse(TenantContext.getCurrentTenant(), pgb);
                    } else {
                        totalPagesResult = null;
                    }
                } else {
                    result = repository.findAllByIsDeletedFalse(pageable);
                    if (needTotals) {
                        int pgbSize = (int) Math.max(1L, Math.min(Integer.MAX_VALUE, result.getTotalElements()));
                        Pageable pgb = PageRequest.of(0, pgbSize, direction, sort);
                        totalPagesResult = repository.findAllByIsDeletedFalse(pgb);
                    } else {
                        totalPagesResult = null;
                    }
                }
            }

            List<OUTDTO> dtos = result.getContent().stream().map(element -> modelMapper.map(element, outDTOClass)).toList();

            Map<String, Double> totals = new HashMap<>();
            if (needTotals && totalPagesResult != null) {
                searchData.getToCalculateTotal().forEach(field -> {
                    Double total = totalPagesResult.getContent().stream().mapToDouble(item -> {
                        try {
                            return toDouble(getNestedFieldValue(item, field));
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }).sum();
                    totals.put(field, total);
                });
            }

            SearchResponse<E, OUTDTO> response = new SearchResponse<>(result.getTotalElements(), dtos, result.getTotalPages(), result.getNumber() + 1, totals);

            OSMLogger.logMethodExit(this.getClass(), "search", "Found " + dtos.size() + " entities out of " + result.getTotalElements());
            OSMLogger.logPerformance(this.getClass(), "search", startTime, System.currentTimeMillis());
            OSMLogger.logDataAccess(this.getClass(), "SEARCH", entityClass.getSimpleName());

            return response;
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error during search operation", e);
            throw e instanceof RuntimeException runtime ? runtime : new IllegalStateException("Search operation failed", e);
        }
    }

    public byte[] exportToPdf(ExportDetails exportDetails) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "exportToPdf", exportDetails);

        try {
            this.currentExportDetails = exportDetails; // Store for dynamic field processing

            if (exportDetails.getSearchData().isFilterTenant()) {
                SearchDetails details = new SearchDetails();
                details.setEqualValue(TenantContext.getCurrentTenant());
                if (exportDetails.getSearchData().getSearchData() != null) {
                    exportDetails.getSearchData().getSearchData().getSearch().put("tenantId", details);
                }
            }

            // Get total count first to determine if pagination is needed
            SearchData countData = cloneSearchDataForCount(exportDetails.getSearchData());
            SearchResponse<E, OUTDTO> countResponse = search(countData);
            long totalRecords = countResponse.getTotal();

            // Get sample data to process dynamic columns
            SearchData sampleData = cloneSearchDataForCount(exportDetails.getSearchData());
            sampleData.setSize(Math.min(100, (int) totalRecords)); // Sample first 100 records
            SearchResponse<E, OUTDTO> sampleResponse = search(sampleData);

            // Process collection fields and generate complete field list
            List<FieldDetails> allFields = processCollectionFields(exportDetails, sampleResponse.getData());
            exportDetails.setFieldDetails(allFields);

            byte[] result;
            if (totalRecords > MAX_RECORDS_PER_DOCUMENT) {
                result = createMultiplePdfs(exportDetails.getSearchData(), totalRecords, allFields, exportDetails.getFileName());
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "Created multiple PDFs for {} records", totalRecords);
            } else {
                result = createSinglePdf(exportDetails.getSearchData(), allFields, exportDetails.getFileName());
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "Created single PDF for {} records", totalRecords);
            }

            OSMLogger.logMethodExit(this.getClass(), "exportToPdf", "Generated " + result.length + " bytes");
            OSMLogger.logPerformance(this.getClass(), "exportToPdf", startTime, System.currentTimeMillis());
            OSMLogger.logBusinessEvent(this.getClass(), "PDF_GENERATED", "PDF generated for " + totalRecords + " records (" + result.length + " bytes)");

            return result;
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error generating PDF export", e);
            throw e;
        } finally {
            this.currentExportDetails = null; // Clear after use
        }
    }


//    public byte[] exportToPdf(ExportDetails exportDetails) {
//        long startTime = System.currentTimeMillis();
//        OSMLogger.logMethodEntry(this.getClass(), "exportToPdf", exportDetails);
//
//        try {
//            if(exportDetails.getSearchData().isFilterTenant()) {
//                SearchDetails details = new SearchDetails();
//                details.setEqualValue(TenantContext.getCurrentTenant());
//                if(exportDetails.getSearchData().getSearchData() != null) {
//                    exportDetails.getSearchData().getSearchData().getSearch().put("tenantId",details);
//                }
//            }
//            // Get total count first to determine if pagination is needed
//            SearchData countData = cloneSearchDataForCount(exportDetails.getSearchData());
//
//            SearchResponse<E, OUTDTO> countResponse = search(countData);
//            long totalRecords = countResponse.getTotal();
//
//            byte[] result;
//            // If total records exceed maximum per document, create multiple PDFs
//            if (totalRecords > MAX_RECORDS_PER_DOCUMENT) {
//                result = createMultiplePdfs(exportDetails.getSearchData(), totalRecords, exportDetails.getFieldDetails(), exportDetails.getFileName());
//                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "Created multiple PDFs for {} records", totalRecords);
//            } else {
//                result = createSinglePdf(exportDetails.getSearchData(), exportDetails.getFieldDetails(), exportDetails.getFileName());
//                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "Created single PDF for {} records", totalRecords);
//            }
//
//            OSMLogger.logMethodExit(this.getClass(), "exportToPdf", "Generated " + result.length + " bytes");
//            OSMLogger.logPerformance(this.getClass(), "exportToPdf", startTime, System.currentTimeMillis());
//            OSMLogger.logBusinessEvent(this.getClass(), "PDF_GENERATED",
//                    "PDF generated for " + totalRecords + " records (" + result.length + " bytes)");
//
//            return result;
//        } catch (Exception e) {
//            OSMLogger.logException(this.getClass(), "Error generating PDF export", e);
//            throw e;
//        }
//    }

    /**
     * Create a single PDF document
     *
     * @param searchData     search criteria
     * @param fieldsToExport fields to include in export
     * @return PDF content as byte array
     */
    private byte[] createSinglePdf(SearchData searchData, List<FieldDetails> fieldsToExport, String fileName) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "createSinglePdf", "fileName: " + fileName + ", fields: " + fieldsToExport.size());

        try {
            // Retrieve all data for export
            searchData.setPage(0);
            searchData.setSize(MAX_RECORDS_PER_DOCUMENT);
            SearchResponse<E, OUTDTO> response = search(searchData);
            List<OUTDTO> data = response.getData();

            OSMLogger.logDataAccess(this.getClass(), "PDF_DATA_RETRIEVED", entityClass.getSimpleName());

            byte[] result = generatePdf(data, 1, 1, fieldsToExport, fileName);

            OSMLogger.logMethodExit(this.getClass(), "createSinglePdf", "Generated " + result.length + " bytes");
            OSMLogger.logPerformance(this.getClass(), "createSinglePdf", startTime, System.currentTimeMillis());

            return result;
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error creating single PDF", e);
            throw e;
        }
    }

    /**
     * Create multiple PDF documents in a ZIP archive
     *
     * @param searchData     search criteria
     * @param totalRecords   total number of records
     * @param fieldsToExport fields to include in export
     * @return ZIP archive as byte array
     */
    private byte[] createMultiplePdfs(SearchData searchData, long totalRecords, List<FieldDetails> fieldsToExport, String fileName) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "createMultiplePdfs", "totalRecords: " + totalRecords + ", fields: " + fieldsToExport.size() + ", fileName: " + fileName);

        try {
            ByteArrayOutputStream zipOutput = new ByteArrayOutputStream();
            int totalPages = (int) Math.ceil((double) totalRecords / MAX_RECORDS_PER_DOCUMENT);

            OSMLogger.logBusinessEvent(this.getClass(), "MULTIPLE_PDF_START", "Creating " + totalPages + " PDF files for " + totalRecords + " records");

            try (ZipOutputStream zipStream = new ZipOutputStream(zipOutput)) {
                for (int pageNum = 0; pageNum < totalPages; pageNum++) {
                    long pageStartTime = System.currentTimeMillis();

                    // Update search criteria for current page
                    searchData.setPage(pageNum);
                    searchData.setSize(MAX_RECORDS_PER_DOCUMENT);

                    // Get data for current page
                    SearchResponse<E, OUTDTO> response = search(searchData);
                    List<OUTDTO> data = response.getData();

                    OSMLogger.logDataAccess(this.getClass(), "PDF_PAGE_DATA_RETRIEVED", entityClass.getSimpleName());

                    // Generate PDF for current page
                    byte[] pdfData = generatePdf(data, pageNum + 1, totalPages, fieldsToExport, fileName);

                    // Add PDF to ZIP archive
                    ZipEntry entry = new ZipEntry(fileName + "_part_" + (pageNum + 1) + "_of_" + totalPages + ".pdf");
                    zipStream.putNextEntry(entry);
                    zipStream.write(pdfData);
                    zipStream.closeEntry();

                    OSMLogger.logPerformance(this.getClass(), "PDF_PAGE_GENERATION", pageStartTime, System.currentTimeMillis());
                    OSMLogger.logBusinessEvent(this.getClass(), "PDF_PAGE_COMPLETED", "Completed PDF page " + (pageNum + 1) + " of " + totalPages);
                }
            }

            byte[] result = zipOutput.toByteArray();
            OSMLogger.logMethodExit(this.getClass(), "createMultiplePdfs", "Generated ZIP with " + result.length + " bytes");
            OSMLogger.logPerformance(this.getClass(), "createMultiplePdfs", startTime, System.currentTimeMillis());
            OSMLogger.logBusinessEvent(this.getClass(), "MULTIPLE_PDF_COMPLETED", "Successfully created " + totalPages + " PDF files in ZIP archive");

            return result;

        } catch (IOException e) {
            OSMLogger.logException(this.getClass(), "Error creating ZIP archive for PDFs", e);
            throw new RuntimeException("Failed to create PDF export", e);
        }
    }

    /**
     * Generate a PDF document for the given data
     *
     * @param data           list of entities
     * @param partNumber     current part number
     * @param totalParts     total number of parts
     * @param fieldsToExport fields to include in export
     * @return PDF content as byte array
     */
    private byte[] generatePdf(List<OUTDTO> data, int partNumber, int totalParts, List<FieldDetails> fieldsToExport, String fileName) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "generatePdf", "dataSize: " + data.size() + ", partNumber: " + partNumber + ", totalParts: " + totalParts + ", fields: " + fieldsToExport.size());

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4.rotate(), 10, 10, 10, 10);

            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Add title
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            Paragraph title = new Paragraph(fileName, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Add pagination info if multiple parts
            if (totalParts > 1) {
                Font infoFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC);
                Paragraph info = new Paragraph("Part " + partNumber + " of " + totalParts, infoFont);
                info.setAlignment(Element.ALIGN_CENTER);
                document.add(info);
            }

            document.add(Chunk.NEWLINE);

            // Create table
            PdfPTable table = new PdfPTable(fieldsToExport.size());
            table.setWidthPercentage(100);

            // Add headers
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
            for (FieldDetails field : fieldsToExport) {
                PdfPCell cell = new PdfPCell(new Phrase(field.getLabel(), headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                table.addCell(cell);
            }

            // Add data
            Font dataFont = new Font(Font.FontFamily.HELVETICA, 8);
            for (OUTDTO entity : data) {
                for (FieldDetails field : fieldsToExport) {
                    String value = getFieldValue(entity, field);
                    PdfPCell cell = new PdfPCell(new Phrase(value, dataFont));
                    table.addCell(cell);
                }
            }

            document.add(table);
            document.close();

            byte[] result = outputStream.toByteArray();
            OSMLogger.logMethodExit(this.getClass(), "generatePdf", "Generated " + result.length + " bytes");
            OSMLogger.logPerformance(this.getClass(), "generatePdf", startTime, System.currentTimeMillis());
            OSMLogger.logBusinessEvent(this.getClass(), "PDF_GENERATED", "Generated PDF with " + data.size() + " records, " + fieldsToExport.size() + " fields");

            return result;
        } catch (DocumentException e) {
            OSMLogger.logException(this.getClass(), "Error creating PDF document", e);
            throw new RuntimeException("Failed to create PDF", e);
        }
    }

    /**
     * Export data to CSV format
     *
     * @param exportDetails search criteria
     * @return CSV content as byte array (zipped if multiple files)
     */
//    public byte[] exportToCsv(ExportDetails exportDetails) {
//        long startTime = System.currentTimeMillis();
//        OSMLogger.logMethodEntry(this.getClass(), "exportToCsv", exportDetails);
//
//        try {
//            if(exportDetails.getSearchData().isFilterTenant()) {
//                SearchDetails details = new SearchDetails();
//                details.setEqualValue(TenantContext.getCurrentTenant());
//                if(exportDetails.getSearchData().getSearchData() != null) {
//                    exportDetails.getSearchData().getSearchData().getSearch().put("tenantId",details);
//                }
//            }
//            // Get total count first to determine if pagination is needed
//            SearchData countData = cloneSearchDataForCount(exportDetails.getSearchData());
//            SearchResponse<E, OUTDTO> countResponse = search(countData);
//            long totalRecords = countResponse.getTotal();
//
//            byte[] result;
//            // If total records exceed maximum per document, create multiple CSVs
//            if (totalRecords > MAX_RECORDS_PER_DOCUMENT) {
//                result = createMultipleCsvs(exportDetails.getSearchData(), totalRecords, exportDetails.getFieldDetails(), exportDetails.getFileName());
//                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "Created multiple CSVs for {} records", totalRecords);
//            } else {
//                result = createSingleCsv(exportDetails.getSearchData(), exportDetails.getFieldDetails());
//                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "Created single CSV for {} records", totalRecords);
//            }
//
//            OSMLogger.logMethodExit(this.getClass(), "exportToCsv", "Generated " + result.length + " bytes");
//            OSMLogger.logPerformance(this.getClass(), "exportToCsv", startTime, System.currentTimeMillis());
//            OSMLogger.logBusinessEvent(this.getClass(), "CSV_GENERATED",
//                    "CSV generated for " + totalRecords + " records (" + result.length + " bytes)");
//
//            return result;
//        } catch (Exception e) {
//            OSMLogger.logException(this.getClass(), "Error generating CSV export", e);
//            throw e;
//        }
//    }
    public byte[] exportToCsv(ExportDetails exportDetails) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "exportToCsv", exportDetails);

        try {
            this.currentExportDetails = exportDetails; // Store for dynamic field processing

            if (exportDetails.getSearchData().isFilterTenant()) {
                SearchDetails details = new SearchDetails();
                details.setEqualValue(TenantContext.getCurrentTenant());
                if (exportDetails.getSearchData().getSearchData() != null) {
                    exportDetails.getSearchData().getSearchData().getSearch().put("tenantId", details);
                }
            }

            // Get total count first to determine if pagination is needed
            SearchData countData = cloneSearchDataForCount(exportDetails.getSearchData());
            SearchResponse<E, OUTDTO> countResponse = search(countData);
            long totalRecords = countResponse.getTotal();

            // Get sample data to process dynamic columns
            SearchData sampleData = cloneSearchDataForCount(exportDetails.getSearchData());
            sampleData.setSize(Math.min(100, (int) totalRecords)); // Sample first 100 records
            SearchResponse<E, OUTDTO> sampleResponse = search(sampleData);

            // Process collection fields and generate complete field list
            List<FieldDetails> allFields = processCollectionFields(exportDetails, sampleResponse.getData());
            exportDetails.setFieldDetails(allFields);

            byte[] result;
            if (totalRecords > MAX_RECORDS_PER_DOCUMENT) {
                result = createMultipleCsvs(exportDetails.getSearchData(), totalRecords, allFields, exportDetails.getFileName());
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "Created multiple CSVs for {} records", totalRecords);
            } else {
                result = createSingleCsv(exportDetails.getSearchData(), allFields);
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "Created single CSV for {} records", totalRecords);
            }

            OSMLogger.logMethodExit(this.getClass(), "exportToCsv", "Generated " + result.length + " bytes");
            OSMLogger.logPerformance(this.getClass(), "exportToCsv", startTime, System.currentTimeMillis());
            OSMLogger.logBusinessEvent(this.getClass(), "CSV_GENERATED", "CSV generated for " + totalRecords + " records (" + result.length + " bytes)");

            return result;
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error generating CSV export", e);
            throw e;
        } finally {
            this.currentExportDetails = null; // Clear after use
        }
    }

    /**
     * Create a single CSV document
     *
     * @param searchData     search criteria
     * @param fieldsToExport fields to include in export
     * @return CSV content as byte array
     */
    private byte[] createSingleCsv(SearchData searchData, List<FieldDetails> fieldsToExport) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "createSingleCsv", "fields: " + fieldsToExport.size());

        try {
            // Retrieve all data for export
            searchData.setPage(0);
            searchData.setSize(MAX_RECORDS_PER_DOCUMENT);
            SearchResponse<E, OUTDTO> response = search(searchData);
            List<OUTDTO> data = response.getData();

            OSMLogger.logDataAccess(this.getClass(), "CSV_DATA_RETRIEVED", entityClass.getSimpleName());

            byte[] result = generateCsv(data, fieldsToExport);

            OSMLogger.logMethodExit(this.getClass(), "createSingleCsv", "Generated " + result.length + " bytes");
            OSMLogger.logPerformance(this.getClass(), "createSingleCsv", startTime, System.currentTimeMillis());

            return result;
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error creating single CSV", e);
            throw e;
        }
    }

    /**
     * Create multiple CSV documents in a ZIP archive
     *
     * @param searchData     search criteria
     * @param totalRecords   total number of records
     * @param fieldsToExport fields to include in export
     * @return ZIP archive as byte array
     */
    private byte[] createMultipleCsvs(SearchData searchData, long totalRecords, List<FieldDetails> fieldsToExport, String fileName) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "createMultipleCsvs", "totalRecords: " + totalRecords + ", fields: " + fieldsToExport.size() + ", fileName: " + fileName);

        try {
            ByteArrayOutputStream zipOutput = new ByteArrayOutputStream();
            int totalPages = (int) Math.ceil((double) totalRecords / MAX_RECORDS_PER_DOCUMENT);

            OSMLogger.logBusinessEvent(this.getClass(), "MULTIPLE_CSV_START", "Creating " + totalPages + " CSV files for " + totalRecords + " records");

            try (ZipOutputStream zipStream = new ZipOutputStream(zipOutput)) {
                for (int pageNum = 0; pageNum < totalPages; pageNum++) {
                    long pageStartTime = System.currentTimeMillis();

                    // Update search criteria for current page
                    searchData.setPage(pageNum);
                    searchData.setSize(MAX_RECORDS_PER_DOCUMENT);

                    // Get data for current page
                    SearchResponse<E, OUTDTO> response = search(searchData);
                    List<OUTDTO> data = response.getData();

                    OSMLogger.logDataAccess(this.getClass(), "CSV_PAGE_DATA_RETRIEVED", entityClass.getSimpleName());

                    // Generate CSV for current page
                    byte[] csvData = generateCsv(data, fieldsToExport);

                    // Add CSV to ZIP archive
                    String safeFileName = (fileName != null && !fileName.isEmpty()) ? fileName : "file";
                    ZipEntry entry = new ZipEntry(safeFileName + "_part_" + (pageNum + 1) + "_of_" + totalPages + ".csv");
                    zipStream.putNextEntry(entry);
                    zipStream.write(csvData);
                    zipStream.closeEntry();

                    OSMLogger.logPerformance(this.getClass(), "CSV_PAGE_GENERATION", pageStartTime, System.currentTimeMillis());
                    OSMLogger.logBusinessEvent(this.getClass(), "CSV_PAGE_COMPLETED", "Completed CSV page " + (pageNum + 1) + " of " + totalPages);
                }
            }

            byte[] result = zipOutput.toByteArray();
            OSMLogger.logMethodExit(this.getClass(), "createMultipleCsvs", "Generated ZIP with " + result.length + " bytes");
            OSMLogger.logPerformance(this.getClass(), "createMultipleCsvs", startTime, System.currentTimeMillis());
            OSMLogger.logBusinessEvent(this.getClass(), "MULTIPLE_CSV_COMPLETED", "Successfully created " + totalPages + " CSV files in ZIP archive");

            return result;

        } catch (IOException e) {
            OSMLogger.logException(this.getClass(), "Error creating ZIP archive for CSVs", e);
            throw new RuntimeException("Failed to create CSV export", e);
        }
    }

    /**
     * Generate a CSV document for the given data
     *
     * @param data           list of entities
     * @param fieldsToExport fields to include in export
     * @return CSV content as byte array
     */
    private byte[] generateCsv(List<OUTDTO> data, List<FieldDetails> fieldsToExport) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "generateCsv", "dataSize: " + data.size() + ", fields: " + fieldsToExport.size());

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // First, add UTF-8 BOM to help Excel detect encoding correctly
            byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
            outputStream.write(bom);

            // Create Excel-friendly CSV with semicolon delimiter since some locales use comma as decimal separator
            StringBuilder csv = new StringBuilder();

            // Add headers
            for (int i = 0; i < fieldsToExport.size(); i++) {
                // Enclose header in quotes and make it stand out
                csv.append("\"").append(fieldsToExport.get(i).getLabel()).append("\"");
                if (i < fieldsToExport.size() - 1) {
                    csv.append(";");
                }
            }
            csv.append("\r\n"); // Windows-style line endings for better Excel compatibility

            // Add data rows
            for (OUTDTO entity : data) {
                for (int i = 0; i < fieldsToExport.size(); i++) {
                    String value = getFieldValue(entity, fieldsToExport.get(i));
                    // Properly escape quotes and wrap values in quotes
                    value = value.replace("\"", "\"\"");
                    csv.append("\"").append(value).append("\"");
                    if (i < fieldsToExport.size() - 1) {
                        csv.append(";");
                    }
                }
                csv.append("\r\n");
            }

            // Write the CSV content to the output stream
            outputStream.write(csv.toString().getBytes(StandardCharsets.UTF_8));
            byte[] result = outputStream.toByteArray();

            OSMLogger.logMethodExit(this.getClass(), "generateCsv", "Generated " + result.length + " bytes");
            OSMLogger.logPerformance(this.getClass(), "generateCsv", startTime, System.currentTimeMillis());
            OSMLogger.logBusinessEvent(this.getClass(), "CSV_GENERATED", "Generated CSV with " + data.size() + " records, " + fieldsToExport.size() + " fields");

            return result;

        } catch (IOException e) {
            OSMLogger.logException(this.getClass(), "Error creating CSV document", e);
            throw new RuntimeException("Failed to create CSV", e);
        }
    }


    /**
     * Clone search data for count query
     *
     * @param original original search data
     * @return cloned search data with page=0 and size=1
     */
    private SearchData cloneSearchDataForCount(SearchData original) {
        SearchData clone = new SearchData();
        clone.setPage(0);
        clone.setSize(Integer.MAX_VALUE);
        clone.setSort(original.getSort());
        clone.setOrder(original.getOrder());
        clone.setSearchData(original.getSearchData());
        return clone;
    }


    /**
     * Get value of a field from an entity
     *
     * @param cls       entity object
     * @param fieldName fieldDetails
     * @return field value as string
     */
    private Field getFieldFromClass(Class<?> cls, String fieldName) {
        Class<?> currentClass = cls;

        while (currentClass != null) {
            try {
                return currentClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                // Field not found in current class, check the superclass
                currentClass = currentClass.getSuperclass();
            }
        }

        return null; // Field not found in class hierarchy
    }


//    protected String getFieldValue(OUTDTO entity, FieldDetails fieldDetails) {
//        if (entity == null || fieldDetails == null || fieldDetails.getName() == null || fieldDetails.getName().isEmpty()) {
//            return "";
//        }
//
//        // Handle nested properties by splitting the field name by dots
//        String[] fieldPath = fieldDetails.getName().split("\\.");
//
//        try {
//            Object currentObject = entity;
//            Class<?> currentClass = outDTOClass;
//
//            // Traverse the object hierarchy
//            for (String currentField : fieldPath) {
//                if (currentObject == null) {
//                    return "";
//                }
//
//                // Get the field from the current class
//                Field field = getFieldFromClass(currentClass, currentField);
//                if (field == null) {
//                    return "";
//                }
//
//                field.setAccessible(true);
//                currentObject = field.get(currentObject);
//
//                // Update the class for the next iteration if we have more fields to traverse
//                if (currentObject != null) {
//                    currentClass = currentObject.getClass();
//                }
//            }
//            if (currentObject != null) {
//                if (fieldDetails.isEnumValue() &&
//                        fieldDetails.getEnumValues() != null &&
//                        !fieldDetails.getEnumValues().isEmpty() && fieldDetails.getEnumValues().get(currentObject.toString())!=null) {
//                    return fieldDetails.getEnumValues().get(currentObject.toString());
//                }
//                return switch (currentObject) {
//                    case LocalDate localDate -> localDate.format(DATE_TIME_FORMATTER);
//                    case LocalDateTime localDateTime -> localDateTime.format(TIME_FORMATTER);
//                    case OffsetDateTime offsetDateTime -> offsetDateTime.format(OFFSET_DATE_TIME_FORMATTER1);
//                    case ZonedDateTime zonedDateTime -> zonedDateTime.format(ZONED_DATE_TIME_FORMATTER1);
//                    case Instant instant -> TIME_FORMATTER.withZone(ZoneId.systemDefault()).format(instant);
//                    default -> currentObject.toString();
//                };
//            }
//            return "";
//        } catch (IllegalAccessException e) {
//            // Log the error if needed
//            LOGGER.error("Error accessing field " + fieldDetails.getName(), e);
//            return "";
//        }
//    }

    //    public byte[] exportToExcel(ExportDetails exportDetails) {
//        long startTime = System.currentTimeMillis();
//        OSMLogger.logMethodEntry(this.getClass(), "exportToExcel", exportDetails);
//
//        try {
//            // Get total count to determine if pagination is needed
//            SearchData countData = cloneSearchDataForCount(exportDetails.getSearchData());
//            SearchResponse<E, OUTDTO> countResponse = search(countData);
//            long totalRecords = countResponse.getTotal();
//
//            byte[] result;
//            // Only create multiple Excel files if total records exceed maximum per document
//            if (totalRecords > MAX_RECORDS_PER_DOCUMENT) {
//                result = createMultipleExcelFiles(
//                        exportDetails.getSearchData(),
//                        totalRecords,
//                        exportDetails.getFieldDetails(),
//                        exportDetails.getFileName()
//                );
//                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "Created multiple Excel files for {} records", totalRecords);
//            } else {
//                result = createSingleExcelFile(
//                        exportDetails.getSearchData(),
//                        exportDetails.getFieldDetails(),
//                        exportDetails.getFileName()
//                );
//                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "Created single Excel file for {} records", totalRecords);
//            }
//
//            OSMLogger.logMethodExit(this.getClass(), "exportToExcel", "Generated " + result.length + " bytes");
//            OSMLogger.logPerformance(this.getClass(), "exportToExcel", startTime, System.currentTimeMillis());
//            OSMLogger.logBusinessEvent(this.getClass(), "EXCEL_GENERATED",
//                    "Excel file generated for " + totalRecords + " records (" + result.length + " bytes)");
//
//            return result;
//        } catch (Exception e) {
//            OSMLogger.logException(this.getClass(), "Error generating Excel export", e);
//            throw e;
//        }
//    }
    public byte[] exportToExcel(ExportDetails exportDetails) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "exportToExcel", exportDetails);

        try {
            this.currentExportDetails = exportDetails; // Store for dynamic field processing

            if (exportDetails.getSearchData().isFilterTenant()) {
                SearchDetails details = new SearchDetails();
                details.setEqualValue(TenantContext.getCurrentTenant());
                if (exportDetails.getSearchData().getSearchData() != null) {
                    exportDetails.getSearchData().getSearchData().getSearch().put("tenantId", details);
                }
            }

            // Get total count to determine if pagination is needed
            SearchData countData = cloneSearchDataForCount(exportDetails.getSearchData());
            SearchResponse<E, OUTDTO> countResponse = search(countData);
            long totalRecords = countResponse.getTotal();

            // Get sample data to process dynamic columns
            SearchData sampleData = cloneSearchDataForCount(exportDetails.getSearchData());
            sampleData.setSize(Math.min(100, (int) totalRecords)); // Sample first 100 records
            SearchResponse<E, OUTDTO> sampleResponse = search(sampleData);

            // Process collection fields and generate complete field list
            List<FieldDetails> allFields = processCollectionFields(exportDetails, sampleResponse.getData());
            exportDetails.setFieldDetails(allFields);

            byte[] result;
            if (totalRecords > MAX_RECORDS_PER_DOCUMENT) {
                result = createMultipleExcelFiles(exportDetails.getSearchData(), totalRecords, allFields, exportDetails.getFileName());
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "Created multiple Excel files for {} records", totalRecords);
            } else {
                result = createSingleExcelFile(exportDetails.getSearchData(), allFields, exportDetails.getFileName());
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "Created single Excel file for {} records", totalRecords);
            }

            OSMLogger.logMethodExit(this.getClass(), "exportToExcel", "Generated " + result.length + " bytes");
            OSMLogger.logPerformance(this.getClass(), "exportToExcel", startTime, System.currentTimeMillis());
            OSMLogger.logBusinessEvent(this.getClass(), "EXCEL_GENERATED", "Excel file generated for " + totalRecords + " records (" + result.length + " bytes)");

            return result;
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error generating Excel export", e);
            throw e;
        } finally {
            this.currentExportDetails = null; // Clear after use
        }
    }

    private byte[] createSingleExcelFile(SearchData searchData, List<FieldDetails> fieldsToExport, String fileName) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "createSingleExcelFile", "fileName: " + fileName + ", fields: " + fieldsToExport.size());

        try {
            // Retrieve all data for export
            searchData.setPage(0);
            searchData.setSize(MAX_RECORDS_PER_DOCUMENT);
            SearchResponse<E, OUTDTO> response = search(searchData);
            List<OUTDTO> data = response.getData();

            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.DEBUG, "Retrieved {} records for Excel export", data.size());
            OSMLogger.logDataAccess(this.getClass(), "EXCEL_DATA_RETRIEVED", entityClass.getSimpleName());

            // Generate single Excel file
            byte[] result = generateExcelFile(data, fieldsToExport, fileName);

            OSMLogger.logMethodExit(this.getClass(), "createSingleExcelFile", "Generated " + result.length + " bytes");
            OSMLogger.logPerformance(this.getClass(), "createSingleExcelFile", startTime, System.currentTimeMillis());

            return result;
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error creating single Excel file", e);
            throw new RuntimeException("Failed to create Excel export", e);
        }
    }

    private byte[] createMultipleExcelFiles(SearchData searchData, long totalRecords, List<FieldDetails> fieldsToExport, String fileName) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "createMultipleExcelFiles", "totalRecords: " + totalRecords + ", fields: " + fieldsToExport.size() + ", fileName: " + fileName);

        try {
            ByteArrayOutputStream zipOutput = new ByteArrayOutputStream();
            int totalPages = (int) Math.ceil((double) totalRecords / MAX_RECORDS_PER_DOCUMENT);

            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.DEBUG, "Creating {} Excel files for {} total records", totalPages, totalRecords);
            OSMLogger.logBusinessEvent(this.getClass(), "MULTIPLE_EXCEL_START", "Creating " + totalPages + " Excel files for " + totalRecords + " records");

            try (ZipOutputStream zipStream = new ZipOutputStream(zipOutput)) {
                for (int pageNum = 0; pageNum < totalPages; pageNum++) {
                    long pageStartTime = System.currentTimeMillis();

                    // Update search criteria for current page
                    searchData.setPage(pageNum);
                    searchData.setSize(MAX_RECORDS_PER_DOCUMENT);

                    // Get data for current page
                    SearchResponse<E, OUTDTO> response = search(searchData);
                    List<OUTDTO> data = response.getData();

                    OSMLogger.log(this.getClass(), OSMLogger.LogLevel.DEBUG, "Processing page {} with {} records", pageNum + 1, data.size());
                    OSMLogger.logDataAccess(this.getClass(), "EXCEL_PAGE_DATA_RETRIEVED", entityClass.getSimpleName());

                    // Generate Excel for current page
                    String safeFileName = (fileName != null && !fileName.isEmpty()) ? fileName : "file";
                    String pageSuffix = "_part_" + (pageNum + 1) + "_of_" + totalPages;
                    byte[] excelData = generateExcelFile(data, fieldsToExport, safeFileName + pageSuffix);

                    // Add Excel to ZIP archive
                    ZipEntry entry = new ZipEntry(safeFileName + pageSuffix + ".xlsx");
                    zipStream.putNextEntry(entry);
                    zipStream.write(excelData);
                    zipStream.closeEntry();

                    OSMLogger.logPerformance(this.getClass(), "EXCEL_PAGE_GENERATION", pageStartTime, System.currentTimeMillis());
                    OSMLogger.logBusinessEvent(this.getClass(), "EXCEL_PAGE_COMPLETED", "Completed Excel page " + (pageNum + 1) + " of " + totalPages);
                }
            }

            byte[] result = zipOutput.toByteArray();
            OSMLogger.logMethodExit(this.getClass(), "createMultipleExcelFiles", "Generated ZIP with " + result.length + " bytes");
            OSMLogger.logPerformance(this.getClass(), "createMultipleExcelFiles", startTime, System.currentTimeMillis());
            OSMLogger.logBusinessEvent(this.getClass(), "MULTIPLE_EXCEL_COMPLETED", "Successfully created " + totalPages + " Excel files in ZIP archive");

            return result;

        } catch (IOException e) {
            OSMLogger.logException(this.getClass(), "Error creating ZIP archive for Excel files", e);
            throw new RuntimeException("Failed to create Excel export", e);
        }
    }

    private byte[] generateExcelFile(List<OUTDTO> data, List<FieldDetails> fieldsToExport, String sheetName) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "generateExcelFile", "dataSize: " + data.size() + ", fields: " + fieldsToExport.size() + ", sheetName: " + sheetName);

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // Validate input

            if (fieldsToExport.isEmpty()) {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.WARN, "No fields specified for Excel export");
                fieldsToExport = Collections.emptyList();
            }

            // Create sheet with safe name (Excel has 31 char limit and prohibits certain chars)
            String safeName = sheetName != null ? sheetName : "Data Export";
            safeName = safeName.replaceAll("[\\\\/:*?\"<>|\\[\\]]", "_");
            if (safeName.length() > 31) {
                safeName = safeName.substring(0, 31);
            }
            XSSFSheet sheet = workbook.createSheet(safeName);

            // Create header row and style
            XSSFRow headerRow = sheet.createRow(0);
            XSSFCellStyle headerStyle = workbook.createCellStyle();
            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Add headers
            for (int i = 0; i < fieldsToExport.size(); i++) {
                XSSFCell cell = headerRow.createCell(i);
                cell.setCellValue(fieldsToExport.get(i).getLabel());
                cell.setCellStyle(headerStyle);
            }

            // Create data styles
            XSSFCellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            XSSFCellStyle altRowStyle = workbook.createCellStyle();
            altRowStyle.cloneStyleFrom(dataStyle);
            altRowStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            altRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Add data rows
            for (int rowNum = 0; rowNum < data.size(); rowNum++) {
                XSSFRow row = sheet.createRow(rowNum + 1); // +1 because header is at 0
                OUTDTO entity = data.get(rowNum);

                // Select style based on row number (striped rows)
                XSSFCellStyle rowStyle = (rowNum % 2 == 0) ? dataStyle : altRowStyle;

                for (int colNum = 0; colNum < fieldsToExport.size(); colNum++) {
                    XSSFCell cell = row.createCell(colNum);
                    String value = getFieldValue(entity, fieldsToExport.get(colNum));
                    cell.setCellValue(value);
                    cell.setCellStyle(rowStyle);
                }
            }

            // Auto-size columns for better readability
            for (int i = 0; i < fieldsToExport.size(); i++) {
                sheet.autoSizeColumn(i);
                // Cap column width at 50 characters to prevent excessive width
                if (sheet.getColumnWidth(i) > 15000) {
                    sheet.setColumnWidth(i, 15000);
                }
            }

            // Freeze the header row
            sheet.createFreezePane(0, 1);

            // Write the workbook to the output stream
            workbook.write(outputStream);
            byte[] result = outputStream.toByteArray();

            OSMLogger.logMethodExit(this.getClass(), "generateExcelFile", "Generated " + result.length + " bytes");
            OSMLogger.logPerformance(this.getClass(), "generateExcelFile", startTime, System.currentTimeMillis());
            OSMLogger.logBusinessEvent(this.getClass(), "EXCEL_GENERATED", "Generated Excel file with " + data.size() + " records, " + fieldsToExport.size() + " fields");

            return result;

        } catch (IOException e) {
            OSMLogger.logException(this.getClass(), "Error creating Excel document", e);
            throw new RuntimeException("Failed to create Excel export", e);
        }
    }

    private List<FieldDetails> processCollectionFields(ExportDetails exportDetails, List<OUTDTO> sampleData) {
        List<FieldDetails> allFields = new ArrayList<>(exportDetails.getFieldDetails());

        if (exportDetails.getCollectionFields() == null || exportDetails.getCollectionFields().isEmpty()) {
            return allFields;
        }

        // Use a sample of data to determine dynamic columns
        Set<String> dynamicColumns = new HashSet<>();

        for (CollectionFieldDetails collectionField : exportDetails.getCollectionFields()) {
            Set<String> columnNames = extractColumnNamesFromCollection(sampleData, collectionField);

            for (String columnName : columnNames) {
                String prefixedName = collectionField.getColumnPrefix() != null ? collectionField.getColumnPrefix() + columnName : columnName;

                if (!dynamicColumns.contains(prefixedName)) {
                    FieldDetails dynamicField = new FieldDetails();
                    dynamicField.setName(collectionField.getCollectionPath() + "." + columnName);
                    dynamicField.setLabel(prefixedName);
                    dynamicField.setDynamicColumn(true);
                    dynamicField.setSourceCollection(collectionField.getCollectionPath());

                    allFields.add(dynamicField);
                    dynamicColumns.add(prefixedName);
                }
            }
        }

        return allFields;
    }

    private Set<String> extractColumnNamesFromCollection(List<OUTDTO> data, CollectionFieldDetails collectionField) {
        Set<String> columnNames = new HashSet<>();

        for (OUTDTO entity : data) {
            try {
                Object collection = getNestedFieldValue(entity, collectionField.getCollectionPath());
                if (collection instanceof Collection) {
                    for (Object item : (Collection<?>) collection) {
                        String columnName = getNestedFieldValueAsString(item, collectionField.getNameField());
                        if (columnName != null && !columnName.isEmpty()) {
                            columnNames.add(columnName);
                        }
                    }
                }
            } catch (Exception e) {
                OSMLogger.logException(this.getClass(), "Error extracting column names from collection", e);
            }
        }

        return columnNames;
    }

    private Object getNestedFieldValue(Object entity, String fieldPath) throws IllegalAccessException {
        if (entity == null || fieldPath == null || fieldPath.isEmpty()) {
            return null;
        }

        String[] pathParts = fieldPath.split("\\.");
        Object currentObject = entity;
        Class<?> currentClass = entity.getClass();

        for (String fieldName : pathParts) {
            if (currentObject == null) {
                return null;
            }

            Field field = getFieldFromClass(currentClass, fieldName);
            if (field == null) {
                return null;
            }

            field.setAccessible(true);
            currentObject = field.get(currentObject);

            if (currentObject != null) {
                currentClass = currentObject.getClass();
            }
        }

        return currentObject;
    }

    private String getNestedFieldValueAsString(Object entity, String fieldPath) {
        try {
            Object value = getNestedFieldValue(entity, fieldPath);
            return value != null ? value.toString() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private String getDynamicCollectionFieldValue(OUTDTO entity, FieldDetails fieldDetails) {
        try {
            // Find the collection field configuration
            CollectionFieldDetails collectionConfig = findCollectionConfig(fieldDetails.getSourceCollection());
            if (collectionConfig == null) {
                return "";
            }

            // Get the collection
            Object collection = getNestedFieldValue(entity, collectionConfig.getCollectionPath());
            if (!(collection instanceof Collection)) {
                return "";
            }

            // Extract the column name from the field details
            String targetColumnName = fieldDetails.getLabel();
            if (collectionConfig.getColumnPrefix() != null) {
                targetColumnName = targetColumnName.replace(collectionConfig.getColumnPrefix(), "");
            }

            // Find the matching item in the collection
            for (Object item : (Collection<?>) collection) {
                String itemColumnName = getNestedFieldValueAsString(item, collectionConfig.getNameField());
                if (targetColumnName.equals(itemColumnName)) {
                    return getNestedFieldValueAsString(item, collectionConfig.getValueField());
                }
            }

            return "";
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error getting dynamic collection field value", e);
            return "";
        }
    }

    protected String getFieldValue(OUTDTO entity, FieldDetails fieldDetails) {
        if (entity == null || fieldDetails == null || fieldDetails.getName() == null || fieldDetails.getName().isEmpty()) {
            return "";
        }

        // Handle dynamic columns from collections
        if (fieldDetails.isDynamicColumn() && fieldDetails.getSourceCollection() != null) {
            return getDynamicCollectionFieldValue(entity, fieldDetails);
        }

        // Handle regular fields (existing logic)
        String[] fieldPath = fieldDetails.getName().split("\\.");

        try {
            Object currentObject = entity;
            Class<?> currentClass = outDTOClass;

            for (String currentField : fieldPath) {
                if (currentObject == null) {
                    return "";
                }

                Field field = getFieldFromClass(currentClass, currentField);
                if (field == null) {
                    return "";
                }

                field.setAccessible(true);
                currentObject = field.get(currentObject);

                if (currentObject != null) {
                    currentClass = currentObject.getClass();
                }
            }

            if (currentObject != null) {
                if (fieldDetails.isEnumValue() && fieldDetails.getEnumValues() != null && !fieldDetails.getEnumValues().isEmpty() && fieldDetails.getEnumValues().get(currentObject.toString()) != null) {
                    return fieldDetails.getEnumValues().get(currentObject.toString());
                }
                return formatFieldValue(currentObject);
            }
            return "";
        } catch (IllegalAccessException e) {
            LOGGER.error("Error accessing field " + fieldDetails.getName(), e);
            return "";
        }
    }

    private CollectionFieldDetails findCollectionConfig(String collectionPath) {
        if (currentExportDetails != null && currentExportDetails.getCollectionFields() != null) {
            return currentExportDetails.getCollectionFields().stream().filter(config -> collectionPath.equals(config.getCollectionPath())).findFirst().orElse(null);
        }
        return null;
    }

    private String formatFieldValue(Object value) {
        return switch (value) {
            case LocalDate localDate -> localDate.format(DATE_TIME_FORMATTER);
            case LocalDateTime localDateTime -> localDateTime.format(TIME_FORMATTER);
            case OffsetDateTime offsetDateTime -> offsetDateTime.format(OFFSET_DATE_TIME_FORMATTER1);
            case ZonedDateTime zonedDateTime -> zonedDateTime.format(ZONED_DATE_TIME_FORMATTER1);
            case Instant instant -> TIME_FORMATTER.withZone(ZoneId.systemDefault()).format(instant);
            default -> value.toString();
        };
    }

    protected String getQrUrlForPublicCode(String publicCode) {
        if (publicCode == null || publicCode.isBlank()) {
            return null;
        }

        requireQrSupport();
        return buildQrUrl(getEntityType(), publicCode);
    }

    //------QRCode----------//
    private String resolveQrEntityType(String entityType) {
        try {
            return getEntityType();
        } catch (UnsupportedOperationException ex) {
            if (entityType == null || entityType.isBlank()) {
                throw ex;
            }
            return entityType.toUpperCase(Locale.ROOT);
        }
    }  //genere un QRCode pour chaque entite (code unique et imag)

    protected boolean supportsQrGeneration() {
        if (codeGenerator == null || qrConfig == null) {
            return false;
        }
        try {
            getEntityType();
            return true;
        } catch (UnsupportedOperationException ex) {
            return false;
        }
    }

    protected E ensureQrCodeIfSupported(E entity) {
        if (entity == null || entity.getId() == null || !supportsQrGeneration()) {
            return entity;
        }
        String existingCode = entity.getQrHex();
        if (existingCode != null && !existingCode.isBlank()) {
            return entity;
        }
        generateQrInfo(getEntityType(), entity.getId());
        return repository.findById(entity.getId()).orElse(entity);
    }

    @Transactional
    public QrCodeInfo generateQrInfo(String entityType, UUID entityId) {
        requireQrSupport();

        E entity = repository.findById(entityId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Entity not found with id: " + entityId));

        String existingCode = entity.getQrHex();
        if (existingCode != null && !existingCode.isBlank()) {
            String publicCode = existingCode.trim();
            String qrUrl = buildQrUrl(resolveQrEntityType(entityType), publicCode);
            String imageBase64 = entity.getQrImageBase64();

            if (imageBase64 == null || imageBase64.isBlank()) {
                Object payload = buildQrPayload(entity);
                byte[] imageBytes = generateQrImageBytes(payload);
                imageBase64 = encodeBase64(imageBytes);
                entity.setQrImageBase64(imageBase64);
                repository.save(entity);
            }

            return new QrCodeInfo(publicCode, qrUrl, imageBase64);
        }

        String publicCode = codeGenerator.generateUnique(repository::existsByQrHex);

        entity.setQrHex(publicCode);

        // prevent recursive QR image embedding
        entity.setQrImageBase64(null);

        String qrUrl = buildQrUrl(resolveQrEntityType(entityType), publicCode);

        // clean payload
        Object payload = buildQrPayload(entity);

        byte[] imageBytes = generateQrImageBytes(payload);

        String imageBase64 = encodeBase64(imageBytes);

        entity.setQrImageBase64(imageBase64);

        repository.save(entity);

        return new QrCodeInfo(publicCode, qrUrl, imageBase64);
    }
    protected byte[] generateQrImageBytes(Object payload) {
        try {
            String json = getQrObjectMapper().writeValueAsString(payload);
            return renderQrContent(json);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize QR payload", e);
        }
    }
    protected Object buildQrPayload(E entity) {
        ObjectMapper mapper = getQrObjectMapper().copy();

        // remove null fields
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // Serialize the DTO graph instead of JPA entities to avoid Hibernate proxy errors.
        OUTDTO dto = modelMapper.map(entity, outDTOClass);
        ObjectNode root = mapper.valueToTree(dto);

        root.remove("qrImageBase64");
        cleanRootEntity(root);

        return root;
    }
    private void cleanRootEntity(ObjectNode root) {
        root.remove("qrImageBase64");

        Iterator<Map.Entry<String, JsonNode>> fields = root.fields();

        List<String> fieldsToRemove = new ArrayList<>();

        while (fields.hasNext()) {

            Map.Entry<String, JsonNode> field = fields.next();

            JsonNode value = field.getValue();

            // remove null values
            if (value == null || value.isNull()) {
                fieldsToRemove.add(field.getKey());
                continue;
            }

            // remove nested entities / objects completely
            if (value.isObject()) {
                fieldsToRemove.add(field.getKey());
                continue;
            }

            // remove arrays containing objects/entities
            if (value.isArray()) {

                boolean containsObjects = false;

                for (JsonNode item : value) {
                    if (item.isObject()) {
                        containsObjects = true;
                        break;
                    }
                }

                if (containsObjects) {
                    fieldsToRemove.add(field.getKey());
                }
            }
        }

        fieldsToRemove.forEach(root::remove);
    }

    private boolean isEntityNode(JsonNode node) {

        if (!(node instanceof ObjectNode objectNode)) {
            return false;
        }

        // Detect JPA entity-like object
        return objectNode.has("id");
    }



    private String buildQrUrl(String entityType, String publicCode) {
        return qrConfig.getBaseUrl() + "/" + entityType.toUpperCase(Locale.ROOT) + "/" + publicCode;
    }

    //genere l'image a partir code public
    @Transactional
    public byte[] generateQrImage(String publicCode) {
        E entity = repository.findByQrHex(publicCode)
                .orElseThrow(() -> new EntityNotFoundException("Entity not found for code: " + publicCode));

        byte[] imageBytes = generateQrImageBytesFromEntity(entity);
        persistQrImageBase64(entity, imageBytes);
        return imageBytes;
    }

    //chercher l'antite par id
    public E getEntityById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Entity not found with id: " + id));
    }

    //transforme une entite metier e n image QR
    protected byte[] generateQrImageBytesFromEntity(E entity) {
        return generateQrImageBytes(buildQrPayload(entity));
    }



    //Genere  l’image à partir d’une entité déjà chargée.
    @Transactional
    public byte[] generateQrImageFromEntity(E entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }

        E entityToEncode = entity;
        if (entity.getId() != null) {
            entityToEncode = repository.findById(entity.getId()).orElse(entity);
        }

        byte[] imageBytes = generateQrImageBytesFromEntity(entityToEncode);
        if (entityToEncode.getId() != null) {
            persistQrImageBase64(entityToEncode, imageBytes);
        }
        return imageBytes;
    }

    @Override
    @Transactional(readOnly = true)
    public QrResolveResponse resolve(String publicCode) {
        String normalizedCode = normalizeSearchCode(publicCode);
        E entity = findByCodeGeneric(normalizedCode)
                .orElseThrow(() -> new EntityNotFoundException("Entity not found for code: " + normalizedCode));

        return buildResolveResponse(normalizedCode, entity);
    }

    private Optional<E> findByCodeGeneric(String normalizedCode) {
        UUID tenantId = TenantContext.getCurrentTenant();
        
        // 1) Try tenant-aware case-insensitive search
        if (tenantId != null) {
            Optional<E> tenantMatch = repository.findByQrHexIgnoreCaseAndTenantIdAndIsDeletedFalse(normalizedCode, tenantId);
            if (tenantMatch.isPresent()) return tenantMatch;
        }

        // 2) Try global case-insensitive search (as fallback or if no tenant)
        Optional<E> globalMatch = repository.findByQrHexIgnoreCaseAndIsDeletedFalse(normalizedCode);
        if (globalMatch.isPresent()) return globalMatch;

        // 3) Legacy exact match fallback
        return repository.findByQrHex(normalizedCode);
    }


    //Recherche par code
    @Override
    @Transactional(readOnly = true)
    public Optional<QrResolveResponse> searchByCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }

        try {
            String normalizedCode = normalizeSearchCode(code);
            return findByCodeGeneric(normalizedCode)
                    .map(entity -> buildResolveResponse(normalizedCode, entity));
        } catch (UnsupportedOperationException ex) {
            return Optional.empty();
        } catch (Exception ex) {
            OSMLogger.logException(this.getClass(), "Global code search contributor failed", ex);
            return Optional.empty();
        }
    }

    // Abstract methods to be implemented by each concrete service
    protected String getEntityType() {
        throw new UnsupportedOperationException("This service does not support QR resolution.");
    }

    protected String getLabel(E entity) {
        throw new UnsupportedOperationException("This service does not support QR resolution.");
    }

    protected String getStatus(E entity) {
        throw new UnsupportedOperationException("This service does not support QR resolution.");
    }

    protected String getMobileRoute() {
        throw new UnsupportedOperationException("This service does not support QR resolution.");
    }

    protected String getWebRoute(E entity) {
        return getMobileRoute();
    }

    // Optional: override to provide extra data
    protected Object getData(E entity) {
        return null;
    }

    private String normalizeSearchCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    protected QrResolveResponse buildResolveResponse(String publicCode, E entity) {
        QrResolveResponse response = new QrResolveResponse();
        response.setEntityType(getEntityType());
        response.setPublicCode(publicCode);
        response.setEntityId(entity.getId().toString());
        response.setLabel(getLabel(entity));
        response.setStatus(getStatus(entity));
        response.setMobileRoute(getMobileRoute());
        response.setWebRoute(getWebRoute(entity));
        response.setData(getData(entity));
        return response;
    }






    private byte[] renderQrContent(String content) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 300, 300, hints);
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
            return baos.toByteArray();
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Failed to generate QR image", e);
        }
    }

    private ObjectMapper getQrObjectMapper() {
        ObjectMapper mapper = objectMapper != null ? objectMapper.copy() : new ObjectMapper().findAndRegisterModules();
        mapper.addMixIn(BaseEntity.class, BaseEntityQrMixin.class);
        return mapper;
    }

    private void persistQrImageBase64(E entity, byte[] imageBytes) {
        String imageBase64 = encodeBase64(imageBytes);
        if (Objects.equals(entity.getQrImageBase64(), imageBase64)) {
            return;
        }

        entity.setQrImageBase64(imageBase64);
        repository.save(entity);
    }


    /**
     * Override this method if a service needs a custom QR payload.
     * By default we serialize the full entity graph once.
     */
    protected Object getQrPayload(E entity) {
        return entity;
    }


    private void requireQrSupport() {
        if (codeGenerator == null || qrConfig == null) {
            throw new UnsupportedOperationException("QR support is not configured for this service.");
        }
    }


    private byte[] renderQrPayload(Object payload) {
        try {
            String json = getQrObjectMapper().writeValueAsString(payload);
            return renderQrContent(json, 300, 300);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize QR payload", e);
        }
    }

    private byte[] renderQrContent(String content, int width, int height) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
            return baos.toByteArray();
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Failed to generate QR image", e);
        }
    }

    private QrCodeInfo persistQrInfo(E entity, String publicCode, String qrUrl, byte[] imageBytes) {
        String imageBase64 = encodeBase64(imageBytes);
        entity.setQrHex(publicCode);
        entity.setQrImageBase64(imageBase64);
        repository.save(entity);
        return new QrCodeInfo(publicCode, qrUrl, imageBase64);
    }

    private String encodeBase64(byte[] imageBytes) {
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIgnoreProperties("qrImageBase64")
    private abstract static class BaseEntityQrMixin {
    }
    //------QRCode----------//
}
