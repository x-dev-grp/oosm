package com.xdev.ooms.security.admin.service;

import com.xdev.ooms.security.admin.dto.*;
import com.xdev.ooms.security.companyprofile.entity.CompanyProfile;
import com.xdev.ooms.security.companyprofile.repository.CompanyProfileRepository;
import com.xdev.ooms.security.user.entity.OSMUser;
import com.xdev.ooms.security.user.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminDashboardService {
    private static final int RECENT_LIMIT = 5;
    private static final int TOP_TENANTS_LIMIT = 8;

    private final UserRepository userRepository;
    private final CompanyProfileRepository companyProfileRepository;

    public AdminDashboardService(UserRepository userRepository, CompanyProfileRepository companyProfileRepository) {
        this.userRepository = userRepository;
        this.companyProfileRepository = companyProfileRepository;
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

        Map<UUID, CompanyProfile> profilesByKey = loadProfilesIndex();
        stats.setUsersByRole(mapUsersByRole());
        stats.setTopTenantsByUsers(mapTopTenants(userRepository.countUsersGroupedByTenantId(), profilesByKey));
        stats.setRecentTenants(mapRecentTenants(profilesByKey));
        stats.setRecentUsers(mapRecentUsers(profilesByKey));

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

    private List<AdminTenantSummaryDTO> mapRecentTenants(Map<UUID, CompanyProfile> profilesByKey) {
        Map<UUID, Long> userCounts = userRepository.countUsersGroupedByTenantId().stream()
                .collect(Collectors.toMap(row -> (UUID) row[0], row -> ((Number) row[1]).longValue(), (left, right) -> left));

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

    private Map<UUID, CompanyProfile> loadProfilesIndex() {
        Map<UUID, CompanyProfile> index = new HashMap<>();
        companyProfileRepository.findAllByIsDeletedFalse().forEach(profile -> {
            index.put(profile.getId(), profile);
            if (profile.getTenantId() != null) {
                index.putIfAbsent(profile.getTenantId(), profile);
            }
        });
        return index;
    }
}
