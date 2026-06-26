package com.xdev.ooms.security.user.repository;

import com.xdev.ooms.security.user.entity.OOSMUser;
import com.xdev.ooms.sharedkernel.models.OOSMModule;
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
public interface UserRepository extends BaseRepository<OOSMUser> {
    Optional<OOSMUser> findByUsername(String username);

    Optional<OOSMUser> findByUsernameAndIsDeletedFalse(String username);

    @Query("SELECT u FROM OOSMUser u WHERE (u.phoneNumber = :input OR LOWER(u.email) = LOWER(:input)) AND COALESCE(u.isDeleted, FALSE) = FALSE")
    Optional<OOSMUser> findByPhoneOrEmailIgnoreCase(@Param("input") String input);

    @Query("""
            SELECT DISTINCT u FROM OOSMUser u
            JOIN FETCH u.role r
            LEFT JOIN FETCH r.permissions
            WHERE u.username = :username AND COALESCE(u.isDeleted, FALSE) = FALSE
            """)
    Optional<OOSMUser> findByUsernameWithRolePermissions(@Param("username") String username);

    Optional<OOSMUser> findByEmailIgnoreCase(String email);

    Optional<OOSMUser> findByEmailIgnoreCaseAndIsDeletedFalse(String email);

    Optional<OOSMUser> findByPhoneNumber(String phoneNumber);

    Optional<OOSMUser> findByPhoneNumberAndIsDeletedFalse(String phoneNumber);

    List<OOSMUser> findByRoleRoleNameAndTenantIdAndIsDeletedFalse(String roleName, UUID tenantId);

    List<OOSMUser> findByRoleRoleNameAndTenantId(String roleName, UUID tenantId);

    @Query("SELECT u FROM OOSMUser u JOIN u.role r WHERE r.roleName = :roleName " +
            "AND (u.tenantId = :tenantId OR u.tenantId IS NULL) AND COALESCE(u.isDeleted, FALSE) = FALSE")
    List<OOSMUser> findByRoleNameAndTenant(@Param("roleName") String roleName,
                                          @Param("tenantId") UUID tenantId);

    @Query("""
            SELECT DISTINCT u
            FROM OOSMUser u
            JOIN u.role r
            LEFT JOIN r.permissions p
            WHERE COALESCE(u.isDeleted, FALSE) = FALSE
              AND u.tenantId = :tenantId
              AND UPPER(r.roleName) <> 'OOSMADMIN'
              AND (
                   (p.module = :module
                    AND UPPER(p.entity) = UPPER(:entity)
                    AND UPPER(p.permissionName) = UPPER(:permissionName))
                   OR UPPER(r.roleName) = 'ADMIN'
              )
            ORDER BY u.firstName, u.lastName, u.username
            """)
    List<OOSMUser> findAssignableUsersByPermissionOrAdmin(@Param("tenantId") UUID tenantId,
                                                          @Param("module") OOSMModule module,
                                                          @Param("entity") String entity,
                                                          @Param("permissionName") String permissionName);

    @Query("SELECT COUNT(u) FROM OOSMUser u WHERE COALESCE(u.isDeleted, FALSE) = FALSE")
    long countAllActiveUsers();

    @Query("SELECT COUNT(u) FROM OOSMUser u WHERE COALESCE(u.isDeleted, FALSE) = FALSE AND u.isLocked = TRUE")
    long countLockedUsers();

    @Query("SELECT COUNT(u) FROM OOSMUser u WHERE COALESCE(u.isDeleted, FALSE) = FALSE AND u.tenantId IS NULL")
    long countUsersWithoutTenant();

    @Query("SELECT COUNT(u) FROM OOSMUser u WHERE COALESCE(u.isDeleted, FALSE) = FALSE AND u.createdDate >= :since")
    long countUsersCreatedSince(@Param("since") LocalDateTime since);

    @Query("""
            SELECT r.roleName, COUNT(u)
            FROM OOSMUser u JOIN u.role r
            WHERE COALESCE(u.isDeleted, FALSE) = FALSE
            GROUP BY r.roleName
            ORDER BY COUNT(u) DESC
            """)
    List<Object[]> countUsersGroupedByRole();

    @Query("""
            SELECT u.tenantId, COUNT(u)
            FROM OOSMUser u
            WHERE COALESCE(u.isDeleted, FALSE) = FALSE AND u.tenantId IS NOT NULL
            GROUP BY u.tenantId
            ORDER BY COUNT(u) DESC
            """)
    List<Object[]> countUsersGroupedByTenantId();

    @Query("""
            SELECT u FROM OOSMUser u
            WHERE COALESCE(u.isDeleted, FALSE) = FALSE
            ORDER BY u.createdDate DESC
            """)
    List<OOSMUser> findRecentUsers(Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE OOSMUser u SET u.tenantId = NULL WHERE u.id = :id")
    void clearTenantId(@Param("id") UUID id);

    @Query("""
            SELECT u FROM OOSMUser u
            WHERE u.tenantId = :tenantId
              AND u.id <> :excludeUserId
              AND COALESCE(u.isDeleted, FALSE) = FALSE
            ORDER BY u.firstName, u.lastName, u.username
            """)
    List<OOSMUser> findActiveUsersByTenantExcluding(
            @Param("tenantId") UUID tenantId,
            @Param("excludeUserId") UUID excludeUserId);
}
