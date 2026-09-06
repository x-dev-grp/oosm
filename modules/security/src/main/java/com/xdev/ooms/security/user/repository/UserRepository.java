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

    @Query("""
            SELECT u FROM OOSMUser u
            WHERE u.username = :username
              AND COALESCE(u.isDeleted, FALSE) = FALSE
            """)
    Optional<OOSMUser> findActiveByUsername(@Param("username") String username);

    @Query("""
            SELECT u FROM OOSMUser u
            WHERE COALESCE(u.isDeleted, FALSE) = FALSE
              AND (
                  u.username = :input
                  OR LOWER(u.email) = LOWER(:input)
                  OR u.phoneNumber = :input
              )
            """)
    Optional<OOSMUser> findActiveByLoginIdentifier(@Param("input") String input);

    @Query("""
            SELECT DISTINCT u FROM OOSMUser u
            JOIN FETCH u.role r
            LEFT JOIN FETCH r.permissions
            WHERE COALESCE(u.isDeleted, FALSE) = FALSE
              AND (
                  u.username = :input
                  OR LOWER(u.email) = LOWER(:input)
                  OR u.phoneNumber = :input
              )
            """)
    Optional<OOSMUser> findActiveByLoginIdentifierWithRolePermissions(@Param("input") String input);

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

    @Query("""
            SELECT u FROM OOSMUser u
            WHERE LOWER(u.email) = LOWER(:email)
              AND COALESCE(u.isDeleted, FALSE) = FALSE
            """)
    Optional<OOSMUser> findActiveByEmailIgnoreCase(@Param("email") String email);

    Optional<OOSMUser> findByPhoneNumber(String phoneNumber);

    Optional<OOSMUser> findByPhoneNumberAndIsDeletedFalse(String phoneNumber);

    @Query("""
            SELECT u FROM OOSMUser u
            WHERE u.phoneNumber = :phoneNumber
              AND COALESCE(u.isDeleted, FALSE) = FALSE
            """)
    Optional<OOSMUser> findActiveByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    List<OOSMUser> findByRoleRoleNameAndTenantIdAndIsDeletedFalse(String roleName, UUID tenantId);

    List<OOSMUser> findByRoleRoleNameAndTenantId(String roleName, UUID tenantId);

    List<OOSMUser> findByRole_Id(UUID roleId);

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

    List<OOSMUser> findByTenantId(UUID tenantId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE oosmuser
            SET is_locked = TRUE,
                enabled = FALSE,
                is_deleted = TRUE,
                username = CASE
                    WHEN username LIKE '%#deleted#%' THEN username
                    ELSE username || '#deleted#' || id::text
                END,
                email = CASE
                    WHEN email IS NULL OR email LIKE '%#deleted#%' THEN email
                    ELSE email || '#deleted#' || id::text
                END,
                phone_number = CASE
                    WHEN phone_number IS NULL OR phone_number LIKE '%#deleted#%' THEN phone_number
                    ELSE phone_number || '#deleted#' || id::text
                END
            WHERE tenant_id = :tenantId
            """, nativeQuery = true)
    int deactivateUsersForTenant(@Param("tenantId") UUID tenantId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE oosmuser
            SET is_locked = FALSE,
                enabled = TRUE,
                is_deleted = FALSE,
                username = CASE
                    WHEN strpos(username, '#deleted#' || id::text) > 0
                        THEN replace(username, '#deleted#' || id::text, '')
                    ELSE username
                END,
                email = CASE
                    WHEN email IS NOT NULL AND strpos(email, '#deleted#' || id::text) > 0
                        THEN replace(email, '#deleted#' || id::text, '')
                    ELSE email
                END,
                phone_number = CASE
                    WHEN phone_number IS NOT NULL AND strpos(phone_number, '#deleted#' || id::text) > 0
                        THEN replace(phone_number, '#deleted#' || id::text, '')
                    ELSE phone_number
                END
            WHERE tenant_id = :tenantId
            """, nativeQuery = true)
    int reactivateUsersForTenant(@Param("tenantId") UUID tenantId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM OOSMUser u WHERE u.tenantId = :tenantId")
    int deleteByTenantId(@Param("tenantId") UUID tenantId);

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
