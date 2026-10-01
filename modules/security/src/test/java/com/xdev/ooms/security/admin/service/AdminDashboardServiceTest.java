package com.xdev.ooms.security.admin.service;

import com.xdev.ooms.security.admin.dto.AdminCompanyOverviewDTO;
import com.xdev.ooms.security.admin.dto.AdminDashboardStatsDTO;
import com.xdev.ooms.security.admin.dto.AdminModuleAdoptionDTO;
import com.xdev.ooms.security.authorization.repository.AuthorizationRepository;
import com.xdev.ooms.security.companyprofile.entity.CompanyProfile;
import com.xdev.ooms.security.companyprofile.repository.CompanyProfileRepository;
import com.xdev.ooms.security.supportticket.enums.SupportTicketStatus;
import com.xdev.ooms.security.supportticket.repository.SupportTicketRepository;
import com.xdev.ooms.security.tenantmodule.entity.TenantEnabledModule;
import com.xdev.ooms.security.tenantmodule.repository.TenantEnabledModuleRepository;
import com.xdev.ooms.security.user.repository.UserRepository;
import com.xdev.ooms.sharedkernel.models.OOSMModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminDashboardServiceTest {
    private UserRepository userRepository;
    private CompanyProfileRepository companyProfileRepository;
    private TenantEnabledModuleRepository tenantEnabledModuleRepository;
    private SupportTicketRepository supportTicketRepository;
    private AuthorizationRepository authorizationRepository;
    private AdminDashboardService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        companyProfileRepository = mock(CompanyProfileRepository.class);
        tenantEnabledModuleRepository = mock(TenantEnabledModuleRepository.class);
        supportTicketRepository = mock(SupportTicketRepository.class);
        authorizationRepository = mock(AuthorizationRepository.class);
        service = new AdminDashboardService(
                userRepository,
                companyProfileRepository,
                tenantEnabledModuleRepository,
                supportTicketRepository,
                authorizationRepository
        );
    }

    @Test
    void aggregatesCompaniesModulesActivityAndSupportTickets() {
        UUID olderId = UUID.randomUUID();
        UUID newerId = UUID.randomUUID();
        CompanyProfile older = company(olderId, "Older Mill", true, LocalDateTime.now().minusDays(40));
        CompanyProfile newer = company(newerId, "Newer Mill", false, LocalDateTime.now().minusDays(2));
        Instant lastSeen = Instant.now().minusSeconds(3600);

        when(companyProfileRepository.findAllByIsDeletedFalse()).thenReturn(List.of(older, newer));
        when(userRepository.countUsersGroupedByTenantId()).thenReturn(rows(new Object[]{olderId, 4L}));
        when(authorizationRepository.countActiveUsersGroupedByTenantIdSince(any()))
                .thenReturn(rows(new Object[]{olderId, 3L}, new Object[]{null, 1L}));
        when(authorizationRepository.findLastActivityGroupedByTenantId()).thenReturn(rows(new Object[]{olderId, lastSeen}));
        when(tenantEnabledModuleRepository.findByIsDeletedFalse()).thenReturn(List.of(
                module(olderId, OOSMModule.RECEPTION),
                module(olderId, OOSMModule.FINANCE),
                module(newerId, OOSMModule.RECEPTION)
        ));
        when(supportTicketRepository.countGroupedByStatus()).thenReturn(rows(
                new Object[]{SupportTicketStatus.OPEN, 2L},
                new Object[]{SupportTicketStatus.IN_PROGRESS, 1L},
                new Object[]{SupportTicketStatus.CLOSED, 5L}
        ));

        AdminDashboardStatsDTO stats = service.getStats();

        assertThat(stats.getActiveUsersLast7Days()).isEqualTo(4L);
        assertThat(stats.getCompanies()).extracting(AdminCompanyOverviewDTO::getTenantName)
                .containsExactly("Newer Mill", "Older Mill");

        AdminCompanyOverviewDTO olderOverview = stats.getCompanies().get(1);
        assertThat(olderOverview.getUserCount()).isEqualTo(4L);
        assertThat(olderOverview.getActiveUsersLast7Days()).isEqualTo(3L);
        assertThat(olderOverview.getEnabledModules()).containsExactly("RECEPTION", "FINANCE");
        assertThat(olderOverview.getLastActivityAt()).isNotNull();

        AdminCompanyOverviewDTO newerOverview = stats.getCompanies().get(0);
        assertThat(newerOverview.isActive()).isFalse();
        assertThat(newerOverview.getUserCount()).isZero();
        assertThat(newerOverview.getLastActivityAt()).isNull();

        Map<String, Long> adoption = stats.getModuleAdoption().stream()
                .collect(Collectors.toMap(AdminModuleAdoptionDTO::getModule, AdminModuleAdoptionDTO::getTenantCount));
        assertThat(adoption).hasSize(OOSMModule.values().length);
        assertThat(adoption).containsEntry("RECEPTION", 2L).containsEntry("FINANCE", 1L).containsEntry("HR", 0L);

        assertThat(stats.getSupportTickets().getOpen()).isEqualTo(2L);
        assertThat(stats.getSupportTickets().getInProgress()).isEqualTo(1L);
        assertThat(stats.getSupportTickets().getResolved()).isZero();
        assertThat(stats.getSupportTickets().getClosed()).isEqualTo(5L);
    }

    private static CompanyProfile company(UUID id, String name, boolean active, LocalDateTime createdDate) {
        CompanyProfile profile = new CompanyProfile();
        profile.setId(id);
        profile.setLegalName(name);
        profile.setActive(active);
        profile.setCreatedDate(createdDate);
        return profile;
    }

    private static TenantEnabledModule module(UUID tenantId, OOSMModule module) {
        TenantEnabledModule row = new TenantEnabledModule();
        row.setCompanyTenantId(tenantId);
        row.setModule(module);
        return row;
    }

    private static List<Object[]> rows(Object[]... rows) {
        return List.of(rows);
    }
}
