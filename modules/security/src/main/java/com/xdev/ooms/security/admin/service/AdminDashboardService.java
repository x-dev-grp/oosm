package com.xdev.ooms.security.admin.service;

import com.xdev.ooms.security.admin.dto.*;
import com.xdev.ooms.security.authorization.repository.AuthorizationRepository;
import com.xdev.ooms.security.companyprofile.entity.CompanyProfile;
import com.xdev.ooms.security.companyprofile.repository.CompanyProfileRepository;
import com.xdev.ooms.security.supportticket.enums.SupportTicketStatus;
import com.xdev.ooms.security.supportticket.repository.SupportTicketRepository;
import com.xdev.ooms.security.tenantmodule.entity.TenantEnabledModule;
import com.xdev.ooms.security.tenantmodule.repository.TenantEnabledModuleRepository;
import com.xdev.ooms.security.user.entity.OOSMUser;
import com.xdev.ooms.security.user.repository.UserRepository;
import com.xdev.ooms.sharedkernel.models.OOSMModule;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminDashboardService {
    private static final int RECENT_LIMIT = 5;
    private static final int TOP_TENANTS_LIMIT = 8;
    private static final int ACTIVE_USERS_WINDOW_DAYS = 7;

    private final UserRepository userRepository;
    private final CompanyProfileRepository companyProfileRepository;
    private final TenantEnabledModuleRepository tenantEnabledModuleRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final AuthorizationRepository authorizationRepository;

    public AdminDashboardService(UserRepository userRepository,
                                 CompanyProfileRepository companyProfileRepository,
                                 TenantEnabledModuleRepository tenantEnabledModuleRepository,
                                 SupportTicketRepository supportTicketRepository,
                                 AuthorizationRepository authorizationRepository) {
        this.userRepository = userRepository;
        this.companyProfileRepository = companyProfileRepository;
        this.tenantEnabledModuleRepository = tenantEnabledModuleRepository;
        this.supportTicketRepository = supportTicketRepository;
        this.authorizationRepository = authorizationRepository;
    }

    @Transactional(readOnly = true)
    public AdminDashboardStatsDTO getStats() {
        AdminDashboardStatsDTO stats = new AdminDashboardStatsDTO();

        long totalTenants = companyProfileRepository.countAllActiveTenants();
        long activeTenants = companyProfileRepository.countActiveTenants();
        long totalUsers = userRepository.countAllActiveUsers();
        long lockedUsers = userRepository.countLockedUsers();

        stats.setTotalTenants(totalTenants);
        stats.setActiveTenants(activeTenants);
        stats.setInactiveTenants(Math.max(0, totalTenants - activeTenants));
        stats.setTotalUsers(totalUsers);
        stats.setLockedUsers(lockedUsers);
        stats.setActiveUsers(Math.max(0, totalUsers - lockedUsers));
        stats.setUsersWithoutTenant(userRepository.countUsersWithoutTenant());
        stats.setNewUsersLast30Days(userRepository.countUsersCreatedSince(LocalDateTime.now().minusDays(30)));

        List<CompanyProfile> profiles = companyProfileRepository.findAllByIsDeletedFalse();
        Map<UUID, CompanyProfile> profilesByKey = indexProfiles(profiles);
        List<Object[]> userCountsByTenant = userRepository.countUsersGroupedByTenantId();
        Map<UUID, Long> userCounts = toCountMap(userCountsByTenant);
        Map<UUID, Long> activeUserCounts = toCountMap(authorizationRepository.countActiveUsersGroupedByTenantIdSince(
                Instant.now().minus(ACTIVE_USERS_WINDOW_DAYS, ChronoUnit.DAYS)));
        Map<UUID, Instant> lastActivity = toInstantMap(authorizationRepository.findLastActivityGroupedByTenantId());
        Map<UUID, Set<OOSMModule>> modulesByTenant = loadModulesByTenant();

        stats.setActiveUsersLast7Days(activeUserCounts.values().stream().mapToLong(Long::longValue).sum());
        stats.setUsersByRole(mapUsersByRole());
        stats.setTopTenantsByUsers(mapTopTenants(userCountsByTenant, profilesByKey));
        stats.setRecentTenants(mapRecentTenants(userCounts));
        stats.setRecentUsers(mapRecentUsers(profilesByKey));
        stats.setCompanies(mapCompanies(profiles, userCounts, activeUserCounts, lastActivity, modulesByTenant));
        stats.setModuleAdoption(mapModuleAdoption(stats.getCompanies()));
        stats.setSupportTickets(mapSupportSummary());

        return stats;
    }

    private List<AdminRoleCountDTO> mapUsersByRole() {
        return userRepository.countUsersGroupedByRole().stream()
                .map(row -> new AdminRoleCountDTO(String.valueOf(row[0]), ((Number) row[1]).longValue()))
                .toList();
    }

    private List<AdminTenantSummaryDTO> mapTopTenants(List<Object[]> groupedCounts, Map<UUID, CompanyProfile> profilesByKey) {
        return groupedCounts.stream()
                .limit(TOP_TENANTS_LIMIT)
                .map(row -> {
                    UUID tenantId = (UUID) row[0];
                    long userCount = ((Number) row[1]).longValue();
                    return toTenantSummary(tenantId, userCount, profilesByKey.get(tenantId));
                })
                .toList();
    }

    private List<AdminTenantSummaryDTO> mapRecentTenants(Map<UUID, Long> userCounts) {
        return companyProfileRepository.findRecentTenants(PageRequest.of(0, RECENT_LIMIT)).stream()
                .map(profile -> toTenantSummary(
                        profile.getId(),
                        userCounts.getOrDefault(profile.getId(), 0L),
                        profile
                ))
                .toList();
    }

    private List<AdminUserSummaryDTO> mapRecentUsers(Map<UUID, CompanyProfile> profilesByKey) {
        return userRepository.findRecentUsers(PageRequest.of(0, RECENT_LIMIT)).stream()
                .map(user -> {
                    AdminUserSummaryDTO dto = new AdminUserSummaryDTO();
                    dto.setId(user.getId());
                    dto.setUsername(user.getUsername());
                    dto.setEmail(user.getEmail());
                    dto.setRoleName(user.getRole() != null ? user.getRole().getRoleName() : null);
                    dto.setLocked(user.isLocked());
                    dto.setCreatedDate(user.getCreatedDate());
                    dto.setTenantName(resolveTenantName(user.getTenantId(), profilesByKey));
                    return dto;
                })
                .toList();
    }

    private List<AdminCompanyOverviewDTO> mapCompanies(List<CompanyProfile> profiles,
                                                       Map<UUID, Long> userCounts,
                                                       Map<UUID, Long> activeUserCounts,
                                                       Map<UUID, Instant> lastActivity,
                                                       Map<UUID, Set<OOSMModule>> modulesByTenant) {
        return profiles.stream()
                .map(profile -> {
                    UUID id = profile.getId();
                    AdminCompanyOverviewDTO dto = new AdminCompanyOverviewDTO();
                    dto.setTenantId(id);
                    dto.setTenantName(profile.getLegalName() != null ? profile.getLegalName() : "");
                    dto.setCity(profile.getCity());
                    dto.setActive(profile.isActive());
                    dto.setCreatedDate(profile.getCreatedDate());
                    dto.setUserCount(userCounts.getOrDefault(id, 0L));
                    dto.setActiveUsersLast7Days(activeUserCounts.getOrDefault(id, 0L));
                    Instant lastSeen = lastActivity.get(id);
                    dto.setLastActivityAt(lastSeen != null ? LocalDateTime.ofInstant(lastSeen, ZoneId.systemDefault()) : null);
                    dto.setEnabledModules(modulesByTenant.getOrDefault(id, Set.of()).stream()
                            .sorted()
                            .map(Enum::name)
                            .toList());
                    return dto;
                })
                .sorted(Comparator.comparing(AdminCompanyOverviewDTO::getCreatedDate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private List<AdminModuleAdoptionDTO> mapModuleAdoption(List<AdminCompanyOverviewDTO> companies) {
        Map<String, Long> counts = companies.stream()
                .flatMap(company -> company.getEnabledModules().stream())
                .collect(Collectors.groupingBy(module -> module, Collectors.counting()));
        return Arrays.stream(OOSMModule.values())
                .map(module -> new AdminModuleAdoptionDTO(module.name(), counts.getOrDefault(module.name(), 0L)))
                .toList();
    }

    private AdminSupportSummaryDTO mapSupportSummary() {
        AdminSupportSummaryDTO summary = new AdminSupportSummaryDTO();
        for (Object[] row : supportTicketRepository.countGroupedByStatus()) {
            if (!(row[0] instanceof SupportTicketStatus status)) {
                continue;
            }
            long count = ((Number) row[1]).longValue();
            switch (status) {
                case OPEN -> summary.setOpen(count);
                case IN_PROGRESS -> summary.setInProgress(count);
                case RESOLVED -> summary.setResolved(count);
                case CLOSED -> summary.setClosed(count);
            }
        }
        return summary;
    }

    private Map<UUID, Set<OOSMModule>> loadModulesByTenant() {
        Map<UUID, Set<OOSMModule>> modules = new HashMap<>();
        for (TenantEnabledModule row : tenantEnabledModuleRepository.findByIsDeletedFalse()) {
            if (row.getCompanyTenantId() == null || row.getModule() == null) {
                continue;
            }
            modules.computeIfAbsent(row.getCompanyTenantId(), key -> EnumSet.noneOf(OOSMModule.class)).add(row.getModule());
        }
        return modules;
    }

    private static Map<UUID, Long> toCountMap(List<Object[]> rows) {
        Map<UUID, Long> counts = new HashMap<>();
        for (Object[] row : rows) {
            counts.put((UUID) row[0], ((Number) row[1]).longValue());
        }
        return counts;
    }

    private static Map<UUID, Instant> toInstantMap(List<Object[]> rows) {
        Map<UUID, Instant> values = new HashMap<>();
        for (Object[] row : rows) {
            if (row[1] instanceof Instant instant) {
                values.put((UUID) row[0], instant);
            }
        }
        return values;
    }

    private AdminTenantSummaryDTO toTenantSummary(UUID tenantId, long userCount, CompanyProfile profile) {
        AdminTenantSummaryDTO dto = new AdminTenantSummaryDTO();
        dto.setTenantId(tenantId);
        dto.setUserCount(userCount);
        if (profile != null) {
            dto.setTenantName(profile.getLegalName());
            dto.setActive(profile.isActive());
            dto.setCreatedDate(profile.getCreatedDate());
        } else {
            dto.setTenantName("");
            dto.setActive(false);
        }
        return dto;
    }

    private String resolveTenantName(UUID tenantId, Map<UUID, CompanyProfile> profilesByKey) {
        if (tenantId == null) {
            return "";
        }
        CompanyProfile profile = profilesByKey.get(tenantId);
        return profile != null && profile.getLegalName() != null ? profile.getLegalName() : "";
    }

    private static Map<UUID, CompanyProfile> indexProfiles(List<CompanyProfile> profiles) {
        Map<UUID, CompanyProfile> index = new HashMap<>();
        profiles.forEach(profile -> {
            index.put(profile.getId(), profile);
            if (profile.getTenantId() != null) {
                index.putIfAbsent(profile.getTenantId(), profile);
            }
        });
        return index;
    }
}
