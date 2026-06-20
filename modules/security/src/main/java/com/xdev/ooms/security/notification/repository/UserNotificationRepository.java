package com.xdev.ooms.security.notification.repository;

import com.xdev.ooms.security.notification.entity.UserNotification;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface UserNotificationRepository extends BaseRepository<UserNotification> {

    Page<UserNotification> findByUserIdAndIsDeletedFalseOrderByCreatedDateDesc(UUID userId, Pageable pageable);

    Page<UserNotification> findByUserIdAndReadAtIsNullAndIsDeletedFalseOrderByCreatedDateDesc(UUID userId, Pageable pageable);

    long countByUserIdAndReadAtIsNullAndIsDeletedFalse(UUID userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE UserNotification n
            SET n.readAt = :readAt, n.lastModifiedDate = :readAt
            WHERE n.userId = :userId AND n.readAt IS NULL AND COALESCE(n.isDeleted, FALSE) = FALSE
            """)
    int markAllReadForUser(@Param("userId") UUID userId, @Param("readAt") LocalDateTime readAt);
}
