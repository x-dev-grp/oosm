package com.xdev.ooms.security.companyprofile.service;

import com.xdev.ooms.security.permission.repository.PermissionRepository;
import com.xdev.ooms.security.role.repository.RoleRepository;
import com.xdev.ooms.security.companyprofile.dto.CompanyProfileDTO;
import com.xdev.ooms.security.companyprofile.dto.CompanyUserDTO;
import com.xdev.ooms.security.tenantmodule.service.TenantModuleService;
import com.xdev.ooms.security.user.dto.OOSMUserOUTDTO;
import com.xdev.ooms.security.role.dto.RoleDTO;
import com.xdev.ooms.security.companyprofile.entity.CompanyProfile;
import com.xdev.ooms.security.permission.entity.Permission;
import com.xdev.ooms.security.role.entity.Role;
import com.xdev.ooms.security.user.service.UserService;
import com.xdev.ooms.sharedkernel.events.TenantCreatedEvent;
import com.xdev.ooms.sharedkernel.communicator.models.common.dtos.apiDTOs.models.SearchResponse;
import com.xdev.ooms.sharedkernel.models.SearchData;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import com.xdev.ooms.sharedkernel.services.utils.SearchSpecificationBuilder;
import com.xdev.ooms.sharedkernel.utils.AuditHelper;
import com.xdev.ooms.sharedkernel.utils.OOSMLogger;
import com.xdev.ooms.sharedkernel.utils.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.*;


@Service
public class CompanyProfileService extends BaseServiceImpl<CompanyProfile, CompanyProfileDTO, CompanyProfileDTO> {
    private final UserService userService;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final SearchSpecificationBuilder<CompanyProfile> specificationBuilder;
    private final ApplicationEventPublisher eventPublisher;
    private final TenantModuleService tenantModuleService;

    public CompanyProfileService(BaseRepository<CompanyProfile> repository, ModelMapper modelMapper, UserService userService, RoleRepository roleRepository, PermissionRepository permissionRepository, SearchSpecificationBuilder<CompanyProfile> specificationBuilder, ApplicationEventPublisher eventPublisher, TenantModuleService tenantModuleService) {
        super(repository, modelMapper);
        this.userService = userService;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.specificationBuilder = specificationBuilder;
        this.eventPublisher = eventPublisher;
        this.tenantModuleService = tenantModuleService;
    }


    @Override
    public Set<Action> actionsMapping(CompanyProfile companyProfile) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "actionsMapping", companyProfile);

        try {
            Set<Action> actions = new HashSet<>();
            actions.addAll(Set.of(Action.UPDATE, Action.DELETE, Action.READ));

            OOSMLogger.logMethodExit(this.getClass(), "actionsMapping", "Actions: " + actions);
            OOSMLogger.logPerformance(this.getClass(), "actionsMapping", startTime, System.currentTimeMillis());

            return actions;

        } catch (Exception e) {
            OOSMLogger.logException(this.getClass(), "Error mapping actions for CompanyProfile: " + companyProfile.getId(), e);
            throw e;
        }
    }

    @Transactional
    public CompanyUserDTO save(CompanyUserDTO dto) throws Exception {
        if (!SecurityUtils.isOosmAdmin()) {
            throw new AccessDeniedException("Only OOSM administrators can create companies");
        }
        if (dto == null || dto.getCompanyUser() == null) return null;
        CompanyProfile company = new CompanyProfile();
        company.setLegalName(dto.getLegalName());
        company.setActive(true);
        CompanyProfile companyProfile = repository.save(company);

        tenantModuleService.setEnabledModules(companyProfile.getId(), dto.getEnabledModules());

        OOSMUserOUTDTO userDto = modelMapper.map(dto.getCompanyUser(), OOSMUserOUTDTO.class);
        Role adminRole = roleRepository.findByRoleName("ADMIN").orElse(null);
        if (adminRole == null) {
            Role role = new Role();
            role.setRoleName("ADMIN");
            List<Permission> permissionList = permissionRepository.findAll();
            Set<Permission> permissions = new HashSet<>(permissionList);
            role.setPermissions(permissions);
            adminRole = roleRepository.save(role);
        }
        userDto.setRole(modelMapper.map(adminRole, RoleDTO.class));
        userDto.setTenantId(companyProfile.getId());
        userDto = userService.addUser(userDto);

        eventPublisher.publishEvent(new TenantCreatedEvent(companyProfile.getId()));

        CompanyUserDTO companyUserDTO = new CompanyUserDTO();
        companyUserDTO.setLegalName(companyProfile.getLegalName());
        companyUserDTO.setCompanyUser(userDto);
        companyUserDTO.setEnabledModules(tenantModuleService.getEnabledModuleNames(companyProfile.getId()));
        return companyUserDTO;
    }

    @Transactional
    public CompanyProfileDTO updateEnabledModules(UUID tenantId, List<String> enabledModules) {
        if (!SecurityUtils.isOosmAdmin()) {
            throw new AccessDeniedException("Only OOSM administrators can manage tenant modules");
        }
        CompanyProfile company = repository.findByIdAndIsDeletedFalse(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Entity not found with this id " + tenantId));
        if (!company.isActive()) {
            throw new IllegalArgumentException("Cannot update modules for a deactivated company");
        }
        tenantModuleService.setEnabledModules(tenantId, enabledModules);
        return findById(tenantId);
    }

    private void enrichWithEnabledModules(CompanyProfileDTO dto) {
        if (dto != null && dto.getId() != null) {
            dto.setEnabledModules(tenantModuleService.getEnabledModuleNames(dto.getId()));
        }
    }

    private void enrichLifecycleFlags(CompanyProfileDTO dto, CompanyProfile entity) {
        if (dto == null || entity == null) {
            return;
        }
        dto.setActive(entity.isActive());
        dto.setDeleted(Boolean.TRUE.equals(entity.getDeleted()));
        enrichWithEnabledModules(dto);
    }

    @Transactional(readOnly = true)
    public CompanyProfileDTO findByIdIncludingDeleted(UUID id) {
        CompanyProfile entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Entity not found with this id " + id));
        CompanyProfileDTO dto = modelMapper.map(entity, outDTOClass);
        enrichLifecycleFlags(dto, entity);
        return dto;
    }

    @Transactional(readOnly = true)
    @Override
    public CompanyProfileDTO findById(UUID id) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "findById", id);

        try {
            if (SecurityUtils.isOosmAdmin()) {
                return findByIdIncludingDeleted(id);
            }
            Optional<CompanyProfile> data = repository.findByIdAndIsDeletedFalse(id);
            if (data.isEmpty()) {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.WARN, "Entity not found with ID: {}", id);
                throw new EntityNotFoundException("Entity not found with this id " + id);
            } else {
                CompanyProfileDTO result = modelMapper.map(data.get(), outDTOClass);
                enrichLifecycleFlags(result, data.get());
                OOSMLogger.logMethodExit(this.getClass(), "findById", result);
                OOSMLogger.logPerformance(this.getClass(), "findById", startTime, System.currentTimeMillis());
                OOSMLogger.logDataAccess(this.getClass(), "READ", entityClass.getSimpleName());
                return result;
            }
        } catch (Exception e) {
            OOSMLogger.logException(this.getClass(), "Error finding entity by ID: " + id, e);
            throw e;
        }
    }

    @Override
    public List<CompanyProfileDTO> findAll() {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "findAll");

        try {
            Collection<CompanyProfile> data = repository.findAllByIsDeletedFalse();
            List<CompanyProfileDTO> result = data.stream().map(item -> {
                CompanyProfileDTO mapped = modelMapper.map(item, outDTOClass);
                enrichWithEnabledModules(mapped);
                return mapped;
            }).toList();
            OOSMLogger.logMethodExit(this.getClass(), "findAll", "Found " + result.size() + " entities");
            OOSMLogger.logPerformance(this.getClass(), "findAll", startTime, System.currentTimeMillis());
            OOSMLogger.logDataAccess(this.getClass(), "READ_ALL", entityClass.getSimpleName());
            return result;
        } catch (Exception e) {
            OOSMLogger.logException(this.getClass(), "Error finding all entities", e);
            throw e;
        }
    }

    @Override
    public Page<CompanyProfileDTO> findAll(int page, int size, String sort, String direction) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "findAll", page, size, sort, direction);

        try {
            Sort.Direction sortDirection = Sort.Direction.fromString(direction);  // "ASC" or "DESC"
            Sort sortObject = Sort.by(sortDirection, sort);  // Sort by the field and direction
            Pageable pageable = PageRequest.of(page, size, sortObject);
            Page<CompanyProfile> data = repository.findAllByIsDeletedFalse(pageable);

            Page<CompanyProfileDTO> result = data.map(item -> {
                CompanyProfileDTO mapped = modelMapper.map(item, outDTOClass);
                enrichWithEnabledModules(mapped);
                return mapped;
            });
            OOSMLogger.logMethodExit(this.getClass(), "findAll", "Page " + page + " with " + result.getContent().size() + " entities");
            OOSMLogger.logPerformance(this.getClass(), "findAll", startTime, System.currentTimeMillis());
            OOSMLogger.logDataAccess(this.getClass(), "READ_PAGEABLE", entityClass.getSimpleName());
            return result;
        } catch (Exception e) {
            OOSMLogger.logException(this.getClass(), "Error finding entities with pagination", e);
            throw e;
        }
    }

    @Override
    public SearchResponse<CompanyProfile, CompanyProfileDTO> search(SearchData searchData) {
        SearchResponse<CompanyProfile, CompanyProfileDTO> response = super.search(searchData);
        if (response.getData() != null) {
            response.getData().forEach(this::enrichWithEnabledModules);
        }
        return response;
    }

    /*

        @Override
        public SearchResponse<CompanyProfile, CompanyProfileDTO> search(SearchData searchData) {
            long startTime = System.currentTimeMillis();
            OOSMLogger.logMethodEntry(this.getClass(), "search", searchData);

            try {
                int page = searchData.getPage() != null ? searchData.getPage() : 0;
                int size = searchData.getSize() != null ? searchData.getSize() : 10;
                Sort.Direction direction = (searchData.getOrder() != null && searchData.getOrder().equalsIgnoreCase("DESC")) ? Sort.Direction.DESC : Sort.Direction.ASC;
                String sort = searchData.getSort() != null ? searchData.getSort() : "createdDate";
                Pageable pageable = PageRequest.of(page, size, direction, sort);

                Specification<CompanyProfile> spec = null;
                if (searchData.getSearchData() != null) {
                    spec = specificationBuilder.buildSpecification(searchData.getSearchData());
                }

                Page<CompanyProfile> result;
                if (spec != null) {
                    result = repository.findAll(spec, pageable);
                } else {
                    result = repository.findAll(pageable);
                }
                List<CompanyProfileDTO> dtos = result.getContent().stream().map(
                        element -> modelMapper.map(element, outDTOClass)
                ).toList();

                SearchResponse<CompanyProfile, CompanyProfileDTO> response = new SearchResponse<>(
                        result.getTotalElements(),
                        dtos,
                        result.getTotalPages(),
                        result.getNumber() + 1
                );

                OOSMLogger.logMethodExit(this.getClass(), "search", "Found " + dtos.size() + " entities out of " + result.getTotalElements());
                OOSMLogger.logPerformance(this.getClass(), "search", startTime, System.currentTimeMillis());
                OOSMLogger.logDataAccess(this.getClass(), "SEARCH", entityClass.getSimpleName());

                return response;
            } catch (Exception e) {
                OOSMLogger.logException(this.getClass(), "Error during search operation", e);
                return new SearchResponse<>(
                        0,
                        null,
                        0,
                        0
                );
            }
        }

    */
    @Override
    @Transactional
    public CompanyProfileDTO update(CompanyProfileDTO dto) {
        if (dto == null || dto.getId() == null) {
            throw new IllegalArgumentException("Company profile id is required");
        }

        CompanyProfile company = repository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Company profile not found for tenantId: " + dto.getId()));

        LocalDateTime preservedCreatedDate = company.getCreatedDate();
        String preservedCreatedBy = company.getCreatedBy();
        UUID preservedTenantId = company.getTenantId();
        Boolean preservedDeleted = company.getDeleted();

        applyDtoToEntity(dto, company);

        company.setCreatedDate(preservedCreatedDate);
        company.setCreatedBy(preservedCreatedBy);
        company.setTenantId(preservedTenantId != null ? preservedTenantId : dto.getId());
        company.setDeleted(preservedDeleted != null ? preservedDeleted : Boolean.FALSE);

        AuditHelper.applyAuditOnUpdate(company);

        CompanyProfile updated = repository.save(company);
        CompanyProfileDTO result = modelMapper.map(updated, CompanyProfileDTO.class);
        enrichWithEnabledModules(result);
        return result;
    }

    private void applyDtoToEntity(CompanyProfileDTO dto, CompanyProfile company) {
        company.setLegalName(dto.getLegalName());
        company.setRegistrationNumber(dto.getRegistrationNumber());
        company.setTaxId(dto.getTaxId());
        company.setCnssNumber(dto.getCnssNumber());
        company.setLegalForm(dto.getLegalForm());
        company.setCapital(dto.getCapital());
        company.setEmail(dto.getEmail());
        company.setPhone(dto.getPhone());
        company.setWebsite(dto.getWebsite());
        company.setAddressLine1(dto.getAddressLine1());
        company.setCity(dto.getCity());
        company.setPostalCode(dto.getPostalCode());
        company.setGovernorate(dto.getGovernorate());
        company.setCampaignStartAt(dto.getCampaignStartAt());
        company.setCampaignEndAt(dto.getCampaignEndAt());
        company.setCampaignStartMonth(dto.getCampaignStartMonth());
        company.setCampaignStartDay(dto.getCampaignStartDay());
        company.setCampaignEndMonth(dto.getCampaignEndMonth());
        company.setCampaignEndDay(dto.getCampaignEndDay());
        company.setCreationDate(dto.getCreationDate());
        company.setInvoiceFooterNote(dto.getInvoiceFooterNote());
        company.setInvoiceLegalMentions(dto.getInvoiceLegalMentions());
        company.setPreferredThemeColor(dto.getPreferredThemeColor());
        company.setDefaultLanguage(dto.getDefaultLanguage());
        company.setTimezone(dto.getTimezone());
        company.setPwaShortName(dto.getPwaShortName());
        company.setInvoiceBankName(dto.getInvoiceBankName());
        company.setInvoiceBankIban(dto.getInvoiceBankIban());
        company.setInvoiceBankSwift(dto.getInvoiceBankSwift());

        if (dto.getLogoData() != null) {
            String logoData = dto.getLogoData().trim();
            if (logoData.contains(",")) {
                logoData = logoData.substring(logoData.indexOf(',') + 1);
            }
            if (logoData.length() > 280_000) {
                throw new IllegalArgumentException("Logo exceeds maximum allowed size (200KB)");
            }
            company.setLogoData(logoData);
            company.setLogoContentType(dto.getLogoContentType());
        }
    }
}
