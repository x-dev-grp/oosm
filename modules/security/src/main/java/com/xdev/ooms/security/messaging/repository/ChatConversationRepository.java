package com.xdev.ooms.security.messaging.repository;

import com.xdev.ooms.security.messaging.entity.ChatConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, UUID> {

    Optional<ChatConversation> findByTenantIdAndParticipantLowIdAndParticipantHighIdAndIsDeletedFalse(
            UUID tenantId,
            UUID participantLowId,
            UUID participantHighId);

    @Query("""
            SELECT c FROM ChatConversation c
            WHERE c.tenantId = :tenantId
              AND COALESCE(c.isDeleted, FALSE) = FALSE
              AND (c.participantLowId = :userId OR c.participantHighId = :userId)
            ORDER BY c.lastMessageAt DESC NULLS LAST, c.createdDate DESC
            """)
    List<ChatConversation> findForUser(@Param("tenantId") UUID tenantId, @Param("userId") UUID userId);
}
