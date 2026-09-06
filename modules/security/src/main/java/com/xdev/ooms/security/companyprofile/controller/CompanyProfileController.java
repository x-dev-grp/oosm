package com.xdev.ooms.security.companyprofile.controller;


import com.xdev.ooms.security.companyprofile.dto.PurgeCompanyRequest;
import com.xdev.ooms.security.companyprofile.dto.UpdateTenantModulesRequest;
import com.xdev.ooms.security.companyprofile.dto.CompanyProfileDTO;
import com.xdev.ooms.security.companyprofile.dto.CompanyUserDTO;
import com.xdev.ooms.security.companyprofile.entity.CompanyProfile;
import com.xdev.ooms.security.companyprofile.service.CompanyProfileService;
import com.xdev.ooms.security.companyprofile.service.TenantLifecycleService;

import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;


@RestController
@RequestMapping("/api/security/company-profile")

public class CompanyProfileController extends BaseControllerImpl<CompanyProfile, CompanyProfileDTO, CompanyProfileDTO> {

    private final CompanyProfileService companyProfileService;
    private final TenantLifecycleService tenantLifecycleService;

    public CompanyProfileController(
            BaseService<CompanyProfile, CompanyProfileDTO, CompanyProfileDTO> baseService,
            ModelMapper modelMapper,
            CompanyProfileService companyProfileService,
            TenantLifecycleService tenantLifecycleService
    ) {
        super(baseService, modelMapper);
        this.companyProfileService = companyProfileService;
        this.tenantLifecycleService = tenantLifecycleService;
    }

    @PostMapping("/save")
    @PreAuthorize("authentication.tokenAttributes['role'] == 'OOSMADMIN' or hasAnyAuthority('OOSMADMIN', 'ROLE_OOSMADMIN')")
    public ResponseEntity<?> saveCompany(@RequestBody CompanyUserDTO userDTO) {
        try {
            CompanyUserDTO companyUser = companyProfileService.save(userDTO);
            return ResponseEntity.ok(companyUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Message error: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateCompanyProfile(@RequestBody CompanyProfileDTO companyProfileDTO) {
        try {
            CompanyProfileDTO updatedProfile = companyProfileService.update(companyProfileDTO);
            return ResponseEntity.ok(updatedProfile);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Message error: " + e.getMessage());
        }
    }

    @PutMapping("/{tenantId}/modules")
    @PreAuthorize("authentication.tokenAttributes['role'] == 'OOSMADMIN' or hasAnyAuthority('OOSMADMIN', 'ROLE_OOSMADMIN')")
    public ResponseEntity<?> updateEnabledModules(@PathVariable UUID tenantId,
                                                  @RequestBody UpdateTenantModulesRequest request) {
        try {
            CompanyProfileDTO updated = companyProfileService.updateEnabledModules(
                    tenantId, request.getEnabledModules());
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Message error: " + e.getMessage());
        }
    }

    @PostMapping("/{tenantId}/deactivate")
    @PreAuthorize("authentication.tokenAttributes['role'] == 'OOSMADMIN' or hasAnyAuthority('OOSMADMIN', 'ROLE_OOSMADMIN')")
    public ResponseEntity<?> deactivate(@PathVariable UUID tenantId) {
        try {
            return ResponseEntity.ok(tenantLifecycleService.deactivate(tenantId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Message error: " + e.getMessage());
        }
    }

    @PostMapping("/{tenantId}/reactivate")
    @PreAuthorize("authentication.tokenAttributes['role'] == 'OOSMADMIN' or hasAnyAuthority('OOSMADMIN', 'ROLE_OOSMADMIN')")
    public ResponseEntity<?> reactivate(@PathVariable UUID tenantId) {
        try {
            return ResponseEntity.ok(tenantLifecycleService.reactivate(tenantId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Message error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{tenantId}/purge")
    @PreAuthorize("authentication.tokenAttributes['role'] == 'OOSMADMIN' or hasAnyAuthority('OOSMADMIN', 'ROLE_OOSMADMIN')")
    public ResponseEntity<?> purge(@PathVariable UUID tenantId, @RequestBody PurgeCompanyRequest request) {
        try {
            String confirmation = request != null ? request.getConfirmationName() : null;
            tenantLifecycleService.purge(tenantId, confirmation);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Message error: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> remove(UUID id) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body("Use DELETE /api/security/company-profile/{tenantId}/purge with confirmationName");
    }

    @Override
    public ResponseEntity<?> delete(UUID id) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body("Use POST /api/security/company-profile/{tenantId}/deactivate");
    }

    @GetMapping("/by-tenant/{tenantId}")
    public ResponseEntity<?> getByTenantId(@PathVariable UUID tenantId) {
        try {
            CompanyProfileDTO profile = companyProfileService.findById(tenantId);
            return ResponseEntity.ok(profile);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Message error: " + e.getMessage());
        }
    }

    @Override
    protected String getResourceName() {
        return "CompanyProfile";
    }
}
