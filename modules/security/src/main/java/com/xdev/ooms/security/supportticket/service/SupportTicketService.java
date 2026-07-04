package com.xdev.ooms.security.supportticket.service;

import com.xdev.ooms.security.companyprofile.entity.CompanyProfile;
import com.xdev.ooms.security.companyprofile.repository.CompanyProfileRepository;
import com.xdev.ooms.security.supportticket.dto.CreateSupportTicketRequest;
import com.xdev.ooms.security.supportticket.dto.SupportTicketDto;
import com.xdev.ooms.security.supportticket.dto.UpdateSupportTicketRequest;
import com.xdev.ooms.security.supportticket.entity.SupportTicket;
import com.xdev.ooms.security.supportticket.enums.SupportTicketPriority;
import com.xdev.ooms.security.supportticket.enums.SupportTicketStatus;
import com.xdev.ooms.security.supportticket.repository.SupportTicketRepository;
import com.xdev.ooms.security.user.entity.OOSMUser;
import com.xdev.ooms.security.user.repository.UserRepository;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import com.xdev.ooms.sharedkernel.utils.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
public class SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;
    private final UserRepository userRepository;
    private final CompanyProfileRepository companyProfileRepository;

    public SupportTicketService(
            SupportTicketRepository supportTicketRepository,
            UserRepository userRepository,
            CompanyProfileRepository companyProfileRepository) {
        this.supportTicketRepository = supportTicketRepository;
        this.userRepository = userRepository;
        this.companyProfileRepository = companyProfileRepository;
    }

    @Transactional
    public SupportTicketDto create(CreateSupportTicketRequest request) {
        validateCreateRequest(request);

        OOSMUser reporter = requireCurrentUser();
        SupportTicket ticket = new SupportTicket();
        ticket.setSubject(request.getSubject().trim());
        ticket.setDescription(request.getDescription().trim());
        ticket.setPriority(parsePriority(request.getPriority()));
        ticket.setPageUrl(trimToNull(request.getPageUrl(), 500));
        ticket.setReporterUserId(reporter.getId());
        ticket.setReporterUsername(reporter.getUsername());
        ticket.setReporterDisplayName(buildDisplayName(reporter));
        ticket.setTenantName(resolveTenantName(reporter.getTenantId()));
        ticket.setStatus(SupportTicketStatus.OPEN);

        return toDto(supportTicketRepository.save(ticket));
    }

    public Page<SupportTicketDto> listForCurrentUser(int page, int size, String scope) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50));
        String normalizedScope = scope != null ? scope.trim().toLowerCase(Locale.ROOT) : "mine";

        if (SecurityUtils.isOosmAdmin() && "all".equals(normalizedScope)) {
            return supportTicketRepository.findByIsDeletedFalseOrderByCreatedDateDesc(pageable).map(this::toDto);
        }

        if (SecurityUtils.hasElevatedAdminAccess() && "tenant".equals(normalizedScope)) {
            UUID tenantId = requireTenantId();
            return supportTicketRepository
                    .findByTenantIdAndIsDeletedFalseOrderByCreatedDateDesc(tenantId, pageable)
                    .map(this::toDto);
        }

        UUID userId = requireCurrentUserId();
        return supportTicketRepository
                .findByReporterUserIdAndIsDeletedFalseOrderByCreatedDateDesc(userId, pageable)
                .map(this::toDto);
    }

    public SupportTicketDto getById(UUID id) {
        SupportTicket ticket = requireAccessibleTicket(id);
        return toDto(ticket);
    }

    @Transactional
    public SupportTicketDto update(UUID id, UpdateSupportTicketRequest request) {
        SupportTicket ticket = requireAccessibleTicket(id);
        requireManageAccess(ticket);

        if (StringUtils.hasText(request.getStatus())) {
            SupportTicketStatus status = parseStatus(request.getStatus());
            ticket.setStatus(status);
            if (status == SupportTicketStatus.RESOLVED || status == SupportTicketStatus.CLOSED) {
                ticket.setResolvedAt(LocalDateTime.now());
            } else {
                ticket.setResolvedAt(null);
            }
        }

        if (StringUtils.hasText(request.getPriority())) {
            ticket.setPriority(parsePriority(request.getPriority()));
        }

        if (request.getAdminNote() != null) {
            ticket.setAdminNote(trimToNull(request.getAdminNote(), 4000));
        }

        return toDto(supportTicketRepository.save(ticket));
    }

    private SupportTicket requireAccessibleTicket(UUID id) {
        SupportTicket ticket = supportTicketRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Support ticket not found"));

        UUID currentUserId = requireCurrentUserId();
        if (currentUserId.equals(ticket.getReporterUserId())) {
            return ticket;
        }

        if (SecurityUtils.isOosmAdmin()) {
            return ticket;
        }

        if (SecurityUtils.isTenantAdmin()) {
            UUID tenantId = requireTenantId();
            if (tenantId.equals(ticket.getTenantId())) {
                return ticket;
            }
        }

        throw new EntityNotFoundException("Support ticket not found");
    }

    private void requireManageAccess(SupportTicket ticket) {
        if (SecurityUtils.isOosmAdmin()) {
            return;
        }

        if (SecurityUtils.isTenantAdmin()) {
            UUID tenantId = requireTenantId();
            if (tenantId.equals(ticket.getTenantId())) {
                return;
            }
        }

        throw new IllegalArgumentException("Only platform or tenant admins can update tickets");
    }

    private void validateCreateRequest(CreateSupportTicketRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        if (!StringUtils.hasText(request.getSubject())) {
            throw new IllegalArgumentException("Subject is required");
        }
        if (!StringUtils.hasText(request.getDescription())) {
            throw new IllegalArgumentException("Description is required");
        }
        if (request.getSubject().trim().length() > 160) {
            throw new IllegalArgumentException("Subject is too long");
        }
    }

    private OOSMUser requireCurrentUser() {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user not found"));
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user not found"));
    }

    private UUID requireCurrentUserId() {
        return SecurityUtils.getCurrentUserId()
                .orElseGet(() -> requireCurrentUser().getId());
    }

    private UUID requireTenantId() {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            tenantId = requireCurrentUser().getTenantId();
        }
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant context is required");
        }
        return tenantId;
    }

    private String resolveTenantName(UUID tenantId) {
        if (tenantId == null) {
            return null;
        }
        return companyProfileRepository.findById(tenantId)
                .map(CompanyProfile::getLegalName)
                .filter(StringUtils::hasText)
                .orElse(null);
    }

    private String buildDisplayName(OOSMUser user) {
        String first = user.getFirstName() != null ? user.getFirstName().trim() : "";
        String last = user.getLastName() != null ? user.getLastName().trim() : "";
        String full = (first + " " + last).trim();
        return full.isBlank() ? user.getUsername() : full;
    }

    private SupportTicketPriority parsePriority(String value) {
        if (!StringUtils.hasText(value)) {
            return SupportTicketPriority.NORMAL;
        }
        try {
            return SupportTicketPriority.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid priority");
        }
    }

    private SupportTicketStatus parseStatus(String value) {
        try {
            return SupportTicketStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid status");
        }
    }

    private String trimToNull(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() > maxLength ? trimmed.substring(0, maxLength) : trimmed;
    }

    private SupportTicketDto toDto(SupportTicket ticket) {
        SupportTicketDto dto = new SupportTicketDto();
        dto.setId(ticket.getId());
        dto.setTenantId(ticket.getTenantId());
        dto.setDeleted(ticket.getDeleted());
        dto.setCreatedDate(ticket.getCreatedDate());
        dto.setCreatedBy(ticket.getCreatedBy());
        dto.setLastModifiedDate(ticket.getLastModifiedDate());
        dto.setLastModifiedBy(ticket.getLastModifiedBy());
        dto.setSubject(ticket.getSubject());
        dto.setDescription(ticket.getDescription());
        dto.setStatus(ticket.getStatus() != null ? ticket.getStatus().name() : null);
        dto.setPriority(ticket.getPriority() != null ? ticket.getPriority().name() : null);
        dto.setPageUrl(ticket.getPageUrl());
        dto.setReporterUserId(ticket.getReporterUserId());
        dto.setReporterUsername(ticket.getReporterUsername());
        dto.setReporterDisplayName(ticket.getReporterDisplayName());
        dto.setTenantName(ticket.getTenantName());
        dto.setAdminNote(ticket.getAdminNote());
        dto.setResolvedAt(ticket.getResolvedAt());
        return dto;
    }
}
