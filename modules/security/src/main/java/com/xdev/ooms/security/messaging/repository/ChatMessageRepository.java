package com.xdev.ooms.security.messaging.repository;

import com.xdev.ooms.security.messaging.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    Page<ChatMessage> findByConversationIdAndIsDeletedFalseOrderByCreatedDateDesc(
            UUID conversationId,
            Pageable pageable);

    @Query("""
            SELECT COUNT(m) FROM ChatMessage m
            JOIN ChatConversation c ON c.id = m.conversationId
            WHERE COALESCE(m.isDeleted, FALSE) = FALSE
              AND COALESCE(c.isDeleted, FALSE) = FALSE
              AND c.tenantId = :tenantId
              AND m.senderUserId <> :userId
              AND m.readAt IS NULL
              AND (c.participantLowId = :userId OR c.participantHighId = :userId)
            """)
    long countUnreadForUser(@Param("tenantId") UUID tenantId, @Param("userId") UUID userId);

    @Modifying
    @Query("""
            UPDATE ChatMessage m
            SET m.readAt = :readAt
            WHERE m.conversationId = :conversationId
              AND m.senderUserId <> :userId
              AND m.readAt IS NULL
              AND COALESCE(m.isDeleted, FALSE) = FALSE
            """)
    int markConversationReadForUser(
            @Param("conversationId") UUID conversationId,
            @Param("userId") UUID userId,
            @Param("readAt") LocalDateTime readAt);
}
