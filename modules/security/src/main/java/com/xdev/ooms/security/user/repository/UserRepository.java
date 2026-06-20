package com.xdev.ooms.security.user.repository;

import com.xdev.ooms.security.user.entity.OSMUser;
import com.xdev.ooms.sharedkernel.models.OSMModule;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends BaseRepository<OSMUser> {
    Optional<OSMUser> findByUsername(String username);

    Optional<OSMUser> findByUsernameAndIsDeletedFalse(String username);

    @Query("SELECT u FROM OSMUser u WHERE (u.phoneNumber = :input OR LOWER(u.email) = LOWER(:input)) AND COALESCE(u.isDeleted, FALSE) = FALSE")
    Optional<OSMUser> findByPhoneOrEmailIgnoreCase(@Param("input") String input);

    @Query("""
            SELECT DISTINCT u FROM OSMUser u
            JOIN FETCH u.role r
            LEFT JOIN FETCH r.permissions
            WHERE u.username = :username AND COALESCE(u.isDeleted, FALSE) = FALSE
            """)
    Optional<OSMUser> findByUsernameWithRolePermissions(@Param("username") String username);

    Optional<OSMUser> findByEmailIgnoreCase(String email);

    Optional<OSMUser> findByEmailIgnoreCaseAndIsDeletedFalse(String email);

    Optional<OSMUser> findByPhoneNumber(String phoneNumber);

    Optional<OSMUser> findByPhoneNumberAndIsDeletedFalse(String phoneNumber);

    List<OSMUser> findByRoleRoleNameAndTenantIdAndIsDeletedFalse(String roleName, UUID tenantId);

    List<OSMUser> findByRoleRoleNameAndTenantId(String roleName, UUID tenantId);

    @Query("SELECT u FROM OSMUser u JOIN u.role r WHERE r.roleName = :roleName " +
            "AND (u.tenantId = :tenantId OR u.tenantId IS NULL) AND COALESCE(u.isDeleted, FALSE) = FALSE")
    List<OSMUser> findByRoleNameAndTenant(@Param("roleName") String roleName,
                                          @Param("tenantId") UUID tenantId);

    @Query("""
            SELECT DISTINCT u
            FROM OSMUser u
            JOIN u.role r
            LEFT JOIN r.permissions p
            WHERE COALESCE(u.isDeleted, FALSE) = FALSE
              AND u.tenantId = :tenantId
              AND UPPER(r.roleName) <> 'OSMADMIN'
              AND (
                   (p.module = :module
                    AND UPPER(p.entity) = UPPER(:entity)
                    AND UPPER(p.permissionName) = UPPER(:permissionName))
                   OR UPPER(r.roleName) = 'ADMIN'
              )
            ORDER BY u.firstName, u.lastName, u.username
            """)
    List<OSMUser> findAssignableUsersByPermissionOrAdmin(@Param("tenantId") UUID tenantId,
                                                          @Param("module") OSMModule module,
                                                          @Param("entity") String entity,
                                                          @Param("permissionName") String permissionName);

    @Query("SELECT COUNT(u) FROM OSMUser u WHERE COALESCE(u.isDeleted, FALSE) = FALSE")
    long countAllActiveUsers();

    @Query("SELECT COUNT(u) FROM OSMUser u WHERE COALESCE(u.isDeleted, FALSE) = FALSE AND u.isLocked = TRUE")
    long countLockedUsers();

    @Query("SELECT COUNT(u) FROM OSMUser u WHERE COALESCE(u.isDeleted, FALSE) = FALSE AND u.tenantId IS NULL")
    long countUsersWithoutTenant();

    @Query("SELECT COUNT(u) FROM OSMUser u WHERE COALESCE(u.isDeleted, FALSE) = FALSE AND u.createdDate >= :since")
    long countUsersCreatedSince(@Param("since") LocalDateTime since);

    @Query("""
            SELECT r.roleName, COUNT(u)
            FROM OSMUser u JOIN u.role r
            WHERE COALESCE(u.isDeleted, FALSE) = FALSE
            GROUP BY r.roleName
            ORDER BY COUNT(u) DESC
            """)
    List<Object[]> countUsersGroupedByRole();

    @Query("""
            SELECT u.tenantId, COUNT(u)
            FROM OSMUser u
            WHERE COALESCE(u.isDeleted, FALSE) = FALSE AND u.tenantId IS NOT NULL
            GROUP BY u.tenantId
            ORDER BY COUNT(u) DESC
            """)
    List<Object[]> countUsersGroupedByTenantId();

    @Query("""
            SELECT u FROM OSMUser u
            WHERE COALESCE(u.isDeleted, FALSE) = FALSE
            ORDER BY u.createdDate DESC
            """)
    List<OSMUser> findRecentUsers(Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE OSMUser u SET u.tenantId = NULL WHERE u.id = :id")
    void clearTenantId(@Param("id") UUID id);

    @Query("""
            SELECT u FROM OSMUser u
            WHERE u.tenantId = :tenantId
              AND u.id <> :excludeUserId
              AND COALESCE(u.isDeleted, FALSE) = FALSE
            ORDER BY u.firstName, u.lastName, u.username
            """)
    List<OSMUser> findActiveUsersByTenantExcluding(
            @Param("tenantId") UUID tenantId,
            @Param("excludeUserId") UUID excludeUserId);
}
