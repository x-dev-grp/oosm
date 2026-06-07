package com.xdev.ooms.sharedkernel.controllers.impl;

import com.xdev.ooms.sharedkernel.apiDTOs.ApiResponse;
import com.xdev.ooms.sharedkernel.apiDTOs.ApiSingleResponse;
import com.xdev.ooms.sharedkernel.apiDTOs.SearchResponse;
import com.xdev.ooms.sharedkernel.controllers.BaseController;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import com.xdev.ooms.sharedkernel.dtos.RevisionDto;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.models.ExportDetails;
import com.xdev.ooms.sharedkernel.models.SearchData;
import com.xdev.ooms.sharedkernel.qr.model.QrCodeInfo;
import com.xdev.ooms.sharedkernel.qr.model.QrResolveResponse;
import com.xdev.ooms.sharedkernel.services.BaseService;
import com.xdev.ooms.sharedkernel.utils.ExceptionHandler;
import com.xdev.ooms.sharedkernel.utils.OSMLogger;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.history.Revision;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public abstract class BaseControllerImpl<E extends BaseEntity, INDTO extends BaseDto<E>, OUTDTO extends BaseDto<E>> implements BaseController<E, INDTO, OUTDTO> {
    protected final BaseService<E, INDTO, OUTDTO> baseService;
    protected final ModelMapper modelMapper;


    public BaseControllerImpl(BaseService<E, INDTO, OUTDTO> baseService, ModelMapper modelMapper) {
        this.baseService = baseService;
        this.modelMapper = modelMapper;
    }

    @Override
    public ModelMapper getModelMapper() {
        return modelMapper;
    }

    @Override
    public BaseService<E, INDTO, OUTDTO> getBaseService() {
        return baseService;
    }

    @Override
    public ResponseEntity<ApiSingleResponse<E, OUTDTO>> findDtoByUuid(@PathVariable UUID id) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "findDtoByUuid", id);

        try {
            OUTDTO result = baseService.findById(id);
            attachPermittedActions(result, currentAuthentication());
            OSMLogger.logMethodExit(this.getClass(), "findDtoByUuid", result);
            OSMLogger.logPerformance(this.getClass(), "findDtoByUuid", startTime, System.currentTimeMillis());
            OSMLogger.logDataAccess(this.getClass(), "READ", this.getClass().getSimpleName());

            return ResponseEntity.ok(new ApiSingleResponse<E, OUTDTO>(true, "Entity found successfully", result));
        } catch (Exception e) {
            return ExceptionHandler.handleSingleException(this.getClass(), "findDtoByUuid", e);
        }
    }
    @Transactional(readOnly = true)
    @Override
    public ResponseEntity<ApiResponse<E, OUTDTO>> fetchAll() {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "fetchAll");

        try {
            List<OUTDTO> list = baseService.findAll();
            attachPermittedActions(list, currentAuthentication());
            OSMLogger.logMethodExit(this.getClass(), "fetchAll", "Found " + list.size() + " entities");
            OSMLogger.logPerformance(this.getClass(), "fetchAll", startTime, System.currentTimeMillis());
            OSMLogger.logDataAccess(this.getClass(), "READ_ALL", this.getClass().getSimpleName());

            return ResponseEntity.ok(new ApiResponse<E, OUTDTO>(true, "Retrieved " + list.size() + " entities successfully", list));
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "fetchAll", e);
        }
    }
    @Transactional(readOnly = true)
    @Override
    public ResponseEntity<ApiResponse<E, OUTDTO>> fetchAllPageable(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(required = false, defaultValue = "createdDate") String sort, @RequestParam(required = false, defaultValue = "DESC") String direction

    ) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "fetchAllPageable", page, size, sort, direction);

        try {
            Page<OUTDTO> pageResult = baseService.findAll(page, size, sort, direction);
            List<OUTDTO> content = attachPermittedActions(pageResult.toList(), currentAuthentication());
            OSMLogger.logMethodExit(this.getClass(), "fetchAllPageable", "Page " + page + " with " + pageResult.getContent().size() + " entities");
            OSMLogger.logPerformance(this.getClass(), "fetchAllPageable", startTime, System.currentTimeMillis());
            OSMLogger.logDataAccess(this.getClass(), "READ_PAGEABLE", this.getClass().getSimpleName());

            return ResponseEntity.ok(new ApiResponse<E, OUTDTO>(true, "Retrieved page " + page + " successfully", content));
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "fetchAllPageable", e);
        }
    }

    @Override
    public ResponseEntity<ApiSingleResponse<E, OUTDTO>> create(
            @RequestBody INDTO dto
    ) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "create", dto);

        try {
            OUTDTO savedEntity = baseService.save(dto);
            attachPermittedActions(savedEntity, currentAuthentication());
            OSMLogger.logMethodExit(this.getClass(), "create", savedEntity);
            OSMLogger.logPerformance(this.getClass(), "create", startTime, System.currentTimeMillis());
            OSMLogger.logDataAccess(this.getClass(), "CREATE", this.getClass().getSimpleName());
            OSMLogger.logBusinessEvent(this.getClass(), "ENTITY_CREATED", "Created entity with ID: " + savedEntity.getId());

            return ResponseEntity.ok(new ApiSingleResponse<E, OUTDTO>(true, "Entity created successfully", savedEntity));
        } catch (Exception e) {
            return ExceptionHandler.handleSingleException(this.getClass(), "create", e);
        }
    }
    @Override
    public ResponseEntity<ApiSingleResponse<E, OUTDTO>> update(
            @RequestBody INDTO dto
    ) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "update", dto);

        try {
            OUTDTO savedEntity = baseService.update(dto);
            attachPermittedActions(savedEntity, currentAuthentication());
            OSMLogger.logMethodExit(this.getClass(), "update", savedEntity);
            OSMLogger.logPerformance(this.getClass(), "update", startTime, System.currentTimeMillis());
            OSMLogger.logDataAccess(this.getClass(), "UPDATE", this.getClass().getSimpleName());
            OSMLogger.logBusinessEvent(this.getClass(), "ENTITY_UPDATED", "Updated entity with ID: " + savedEntity.getId());

            return ResponseEntity.ok(new ApiSingleResponse<E, OUTDTO>(true, "Entity updated successfully", savedEntity));
        } catch (Exception e) {
            return ExceptionHandler.handleSingleException(this.getClass(), "update", e);
        }
    }

    @Override
    public ResponseEntity<?> remove(
            @PathVariable UUID id
    ) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "remove", id);

        try {
            baseService.remove(id);
            OSMLogger.logMethodExit(this.getClass(), "remove");
            OSMLogger.logPerformance(this.getClass(), "remove", startTime, System.currentTimeMillis());
            OSMLogger.logDataAccess(this.getClass(), "REMOVE", this.getClass().getSimpleName());
            OSMLogger.logBusinessEvent(this.getClass(), "ENTITY_REMOVED", "Removed entity with ID: " + id);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "remove", e);
        }
    }

    @Override
    public ResponseEntity<?> delete(UUID id) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "delete", id);

        try {
            OUTDTO deletedEntity = baseService.delete(id);
            OSMLogger.logMethodExit(this.getClass(), "delete", deletedEntity);
            OSMLogger.logPerformance(this.getClass(), "delete", startTime, System.currentTimeMillis());
            OSMLogger.logDataAccess(this.getClass(), "DELETE", this.getClass().getSimpleName());
            OSMLogger.logBusinessEvent(this.getClass(), "ENTITY_DELETED", "Deleted entity with ID: " + id);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "delete", e);
        }
    }

    @Override
    public RevisionDto<E> findLastRevision(@PathVariable UUID id) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "findLastRevision", id);

        try {
            Optional<Revision<Integer, E>> revisionOptional = baseService.findLastRevisionById(id);
            RevisionDto<E> revisionDto = new RevisionDto<E>();
            if (revisionOptional.isPresent()) {
                Revision<Integer, E> revision = revisionOptional.orElse(null);
                E entity = revision.getEntity();
                revisionDto.setRevisionMetadata(revision.getMetadata());
                OUTDTO outDto = modelMapper.map(entity, baseService.getOutDTOClass());
                revisionDto.setData(outDto);

                OSMLogger.logMethodExit(this.getClass(), "findLastRevision", "Found revision for entity: " + entity.getId());
                OSMLogger.logPerformance(this.getClass(), "findLastRevision", startTime, System.currentTimeMillis());
                OSMLogger.logDataAccess(this.getClass(), "READ_LAST_REVISION", this.getClass().getSimpleName());
            } else {
                OSMLogger.logMethodExit(this.getClass(), "findLastRevision", "No revision found");
                OSMLogger.logPerformance(this.getClass(), "findLastRevision", startTime, System.currentTimeMillis());
                OSMLogger.logDataAccess(this.getClass(), "READ_LAST_REVISION", this.getClass().getSimpleName());
            }
            return revisionDto;
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error finding last revision for ID: " + id, e);
            throw e;
        }
    }

    @Override
    public List<RevisionDto<E>> findAllRevisions(@PathVariable UUID id) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "findAllRevisions", id);

        try {
            List<Revision<Integer, E>> listRevision = baseService.findRevisionsById(id).getContent();
            List<RevisionDto<E>> result = listRevision.stream()
                    .map(ls -> new RevisionDto<E>(ls.getMetadata(), modelMapper.map(ls.getEntity(), baseService.getOutDTOClass())))
                    .toList();

            OSMLogger.logMethodExit(this.getClass(), "findAllRevisions", "Found " + result.size() + " revisions");
            OSMLogger.logPerformance(this.getClass(), "findAllRevisions", startTime, System.currentTimeMillis());
            OSMLogger.logDataAccess(this.getClass(), "READ_ALL_REVISIONS", this.getClass().getSimpleName());

            return result;
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error finding all revisions for ID: " + id, e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    @Override
    public ResponseEntity<SearchResponse<E, OUTDTO>> advancedSearch(@RequestBody SearchData searchData, Authentication authentication) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "advancedSearch", searchData, authentication != null ? authentication.getName() : "anonymous");

        try {
            final String resource = getResourceName();
            Set<String> actions = extractResourcePermissions(authentication, resource);
            String role = extractResourceRole(authentication);

            OSMLogger.logSecurityEvent(this.getClass(), "SEARCH_PERMISSIONS",
                    "User: " + (authentication != null ? authentication.getName() : "anonymous") +
                            ", Role: " + role + ", Resource: " + resource + ", Permissions: " + actions);

            SearchResponse<E, OUTDTO> response = baseService.search(searchData);
            List<OUTDTO> dtos = response.getData().stream()
                    .peek(element -> attachPermittedActions(element, authentication, role, actions))
                    .toList();
            response.setData(dtos);

            OSMLogger.logMethodExit(this.getClass(), "advancedSearch", "Found " + dtos.size() + " entities with filtered actions");
            OSMLogger.logPerformance(this.getClass(), "advancedSearch", startTime, System.currentTimeMillis());
            OSMLogger.logDataAccess(this.getClass(), "ADVANCED_SEARCH", this.getClass().getSimpleName());
            OSMLogger.logBusinessEvent(this.getClass(), "ADVANCED_SEARCH_EXECUTED",
                    "Advanced search executed by " + (authentication != null ? authentication.getName() : "anonymous") +
                            " with " + dtos.size() + " results");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error during advanced search", e);
            throw e;
        }
    }

    private String extractResourceRole(Authentication authentication) {
        if (authentication == null) {
            return "";
        }
        // 1) Try to reflectively call getClaims() on the principal
        Object principal = authentication.getPrincipal();
        Map<String, Object> claims = null;
        if (principal != null) {
            try {
                Method m = principal.getClass().getMethod("getClaims");
                Object maybeClaims = m.invoke(principal);
                if (maybeClaims instanceof Map<?, ?>) {
                    claims = (Map<String, Object>) maybeClaims;
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                // principal does not expose claims; fall back to authorities
            }
        }

        // 2) From the claims map pull out “authorities” if present
        List<String> rawAuthorities = Collections.emptyList();
        if (claims != null) {
            Object role = claims.get("role");
            if (role != null) {
                return role.toString().toUpperCase().replace("ROLE_", "");
            }

        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .map(String::toUpperCase)
                .filter(authority -> authority.equals("ADMIN")
                        || authority.equals("OSMADMIN")
                        || authority.equals("ROLE_ADMIN")
                        || authority.equals("ROLE_OSMADMIN"))
                .map(authority -> authority.replace("ROLE_", ""))
                .findFirst()
                .orElse("");
    }

    private Authentication currentAuthentication() {
        return SecurityContextHolder.getContext() != null ? SecurityContextHolder.getContext().getAuthentication() : null;
    }

    protected List<OUTDTO> attachPermittedActions(List<OUTDTO> dtos) {
        return attachPermittedActions(dtos, currentAuthentication());
    }

    private List<OUTDTO> attachPermittedActions(List<OUTDTO> dtos, Authentication authentication) {
        if (dtos == null || dtos.isEmpty()) {
            return dtos;
        }
        String resource = getResourceName();
        Set<String> actions = extractResourcePermissions(authentication, resource);
        String role = extractResourceRole(authentication);
        dtos.forEach(dto -> attachPermittedActions(dto, authentication, role, actions));
        return dtos;
    }

    protected OUTDTO attachPermittedActions(OUTDTO dto) {
        return attachPermittedActions(dto, currentAuthentication());
    }

    protected <DTO extends BaseDto<?>> DTO attachPermittedActions(DTO dto, String resource, Set<Action> availableActions) {
        if (dto == null) {
            return null;
        }
        Set<Action> filteredActions = filterPermittedActions(resource, availableActions);
        SortedSet<Action> sortedActions = new TreeSet<>(Comparator.comparing(Action::name));
        sortedActions.addAll(filteredActions);
        dto.setActions(sortedActions);
        return dto;
    }

    protected <DTO extends BaseDto<?>> List<DTO> attachPermittedActions(List<DTO> dtos, String resource, Set<Action> availableActions) {
        if (dtos == null || dtos.isEmpty()) {
            return dtos;
        }
        dtos.forEach(dto -> attachPermittedActions(dto, resource, availableActions));
        return dtos;
    }

    private OUTDTO attachPermittedActions(OUTDTO dto, Authentication authentication) {
        if (dto == null) {
            return null;
        }
        String resource = getResourceName();
        Set<String> actions = extractResourcePermissions(authentication, resource);
        String role = extractResourceRole(authentication);
        return attachPermittedActions(dto, authentication, role, actions);
    }

    private OUTDTO attachPermittedActions(OUTDTO dto, Authentication authentication, String role, Set<String> actions) {
        if (dto == null) {
            return null;
        }
        E entity = modelMapper.map(dto, baseService.getEntityClass());
        Set<Action> filteredActions = filterPermittedActions(role, actions, baseService.actionsMapping(entity));
        SortedSet<Action> sortedActions = new TreeSet<>(Comparator.comparing(Action::name));
        sortedActions.addAll(filteredActions);
        dto.setActions(sortedActions);
        return dto;
    }

    private Set<Action> filterPermittedActions(String resource, Set<Action> availableActions) {
        Authentication authentication = currentAuthentication();
        return filterPermittedActions(
                extractResourceRole(authentication),
                extractResourcePermissions(authentication, resource),
                availableActions
        );
    }

    private Set<Action> filterPermittedActions(String role, Set<String> permittedActionNames, Set<Action> availableActions) {
        if (availableActions == null || availableActions.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> adminRoles = Set.of("ADMIN", "OSMADMIN");
        if (adminRoles.contains(role)) {
            return new HashSet<>(availableActions);
        }
        Set<String> actions = permittedActionNames == null ? Collections.emptySet() : permittedActionNames;
        return availableActions.stream()
                .filter(action -> actions.contains(action.name()))
                .collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    private Set<String> extractResourcePermissions(Authentication authentication, String resource) {
        if (authentication == null || resource == null) {
            return Collections.emptySet();
        }

        // 1) Try to reflectively call getClaims() on the principal
        Object principal = authentication.getPrincipal();
        Map<String, Object> claims = null;
        if (principal != null) {
            try {
                Method m = principal.getClass().getMethod("getClaims");
                Object maybeClaims = m.invoke(principal);
                if (maybeClaims instanceof Map<?, ?>) {
                    claims = (Map<String, Object>) maybeClaims;
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                // principal does not expose claims; fall back to authorities
            }
        }

        // 2) From the claims map pull out “authorities” if present
        List<String> rawAuthorities = Collections.emptyList();
        if (claims != null) {
            Object auths = claims.get("authorities");
            if (auths instanceof Collection<?>) {
                rawAuthorities = ((Collection<?>) auths).stream()
                        .map(Object::toString)
                        .collect(Collectors.toList());
            }
        }

        // 3) Fallback to Spring’s built-in GrantedAuthority list if we got nothing
        if (rawAuthorities.isEmpty()) {
            rawAuthorities = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
        }

        // 4) Now filter by “:RESOURCE:” and strip off the resource prefix
        String filter = ":" + resource.toUpperCase() + ":";
        return rawAuthorities.stream()
                .filter(Objects::nonNull)
                .filter(auth -> auth.toUpperCase().contains(filter))
                .map(this::extractPermissionFromAuthority)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private String extractPermissionFromAuthority(String authority) {
        if (authority == null || authority.isEmpty()) {
            return null;
        }
        int lastColon = authority.lastIndexOf(':');
        if (lastColon == -1) {
            return null;
        }
        try {
            return authority.substring(lastColon + 1);
        } catch (StringIndexOutOfBoundsException e) {
            return null;
        }
    }

    protected abstract String getResourceName();

    @Override
    public ResponseEntity<byte[]> exportPdf(@RequestBody ExportDetails exportDetails) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "exportPdf", exportDetails);

        try {
            byte[] content = baseService.exportToPdf(exportDetails);

            // Check if response is zipped (multiple documents)
            boolean isZipped = isZipContent(content);

            HttpHeaders headers = new HttpHeaders();
            String fileName = (exportDetails.getFileName() != null) ? exportDetails.getFileName() : "file" + (isZipped ? "_export.zip" : "_export.pdf");
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
            MediaType mediaType = isZipped ?
                    MediaType.parseMediaType("application/zip") :
                    MediaType.APPLICATION_PDF;

            OSMLogger.logMethodExit(this.getClass(), "exportPdf", "Exported " + content.length + " bytes as " + (isZipped ? "ZIP" : "PDF"));
            OSMLogger.logPerformance(this.getClass(), "exportPdf", startTime, System.currentTimeMillis());
            OSMLogger.logBusinessEvent(this.getClass(), "PDF_EXPORT",
                    "PDF export completed: " + fileName + " (" + content.length + " bytes)");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(mediaType)
                    .body(content);
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error during PDF export", e);
            throw e;
        }
    }

    /**
     * Export search results as CSV
     *
     * @param exportDetails search criteria with optional export fields
     * @return CSV file as byte array (or ZIP file containing multiple CSVs)
     */

    @Override
    public ResponseEntity<byte[]> exportCsv(@RequestBody ExportDetails exportDetails) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "exportCsv", exportDetails);

        try {
            byte[] content = baseService.exportToCsv(exportDetails);

            // Check if response is zipped (multiple documents)
            boolean isZipped = isZipContent(content);

            HttpHeaders headers = new HttpHeaders();
            String fileName = (exportDetails.getFileName() != null) ? exportDetails.getFileName() : "file";
            fileName += (isZipped ? "_export.zip" : "_export.csv");
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
            MediaType mediaType;
            if (isZipped) {
                mediaType = MediaType.parseMediaType("application/zip");
            } else {
                // Use text/csv;charset=UTF-8 to help with encoding detection
                mediaType = MediaType.parseMediaType("text/csv;charset=UTF-8");

                // Make sure to set the content type properly for Excel to recognize it
                headers.set(HttpHeaders.CONTENT_TYPE, "text/csv;charset=UTF-8");
            }

            OSMLogger.logMethodExit(this.getClass(), "exportCsv", "Exported " + content.length + " bytes as " + (isZipped ? "ZIP" : "CSV"));
            OSMLogger.logPerformance(this.getClass(), "exportCsv", startTime, System.currentTimeMillis());
            OSMLogger.logBusinessEvent(this.getClass(), "CSV_EXPORT",
                    "CSV export completed: " + fileName + " (" + content.length + " bytes)");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(mediaType)
                    .body(content);
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error during CSV export", e);
            throw e;
        }
    }

    /**
     * Alternative endpoint for exporting directly to Excel format
     */

    @Override
    public ResponseEntity<byte[]> exportExcel(@RequestBody ExportDetails exportDetails) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "exportExcel", exportDetails);

        try {
            byte[] content = baseService.exportToExcel(exportDetails);

            // Check if response is zipped (multiple documents)
            boolean isZipped = isZipContent(content);

            HttpHeaders headers = new HttpHeaders();
            String fileName = (exportDetails.getFileName() != null) ? exportDetails.getFileName() : "file";
            fileName += (isZipped ? "_export.zip" : "_export.xlsx");
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
            MediaType mediaType;
            if (isZipped) {
                mediaType = MediaType.parseMediaType("application/zip");
            } else {
                // XLSX MIME type
                mediaType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            }

            OSMLogger.logMethodExit(this.getClass(), "exportExcel", "Exported " + content.length + " bytes as " + (isZipped ? "ZIP" : "Excel"));
            OSMLogger.logPerformance(this.getClass(), "exportExcel", startTime, System.currentTimeMillis());
            OSMLogger.logBusinessEvent(this.getClass(), "EXCEL_EXPORT",
                    "Excel export completed: " + fileName + " (" + content.length + " bytes)");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(mediaType)
                    .body(content);
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error during Excel export", e);
            throw e;
        }
    }

    /**
     * Helper method to check if content is a ZIP file
     */
    private boolean isZipContent(byte[] content) {
        if (content == null || content.length < 4) {
            return false;
        }

        // Check for ZIP file signature
        return content[0] == 0x50 && content[1] == 0x4B && content[2] == 0x03 && content[3] == 0x05;
    }


    //----------------QRCode-----------------//
    @Override
    public ResponseEntity<QrCodeInfo> genQr(String entityType, UUID entityId) {

        try {
            QrCodeInfo qrInfo = baseService.generateQrInfo(entityType, entityId);
             return ResponseEntity.ok(qrInfo);
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error generating QR", e);
            throw e;
        }
    }

    @Override
    public ResponseEntity<?> resolve( String publicCode) {
        // entityType is not needed because the service knows its own type
        try {
            QrResolveResponse response = baseService.resolve(publicCode);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error");
        }
    }

    @Override
    public ResponseEntity<?> searchByCode(String code) {
        try {
            if (code == null || code.isBlank()) {
                return ResponseEntity.badRequest().body("code is required");
            }

            Optional<QrResolveResponse> response = baseService.searchByCode(code);
            if (response.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Entity not found for code: " + code);
            }

            return ResponseEntity.ok(response.get());
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error");
        }
    }
    //----------------QRCode-----------------//
}
