package com.xdev.ooms.security.messaging.entity;

import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "chat_conversation")
public class ChatConversation extends BaseEntity {

    @Column(nullable = false)
    private UUID participantLowId;

    @Column(nullable = false)
    private UUID participantHighId;

    @Column(length = 500)
    private String lastMessagePreview;

    private LocalDateTime lastMessageAt;

    public UUID getParticipantLowId() {
        return participantLowId;
    }

    public void setParticipantLowId(UUID participantLowId) {
        this.participantLowId = participantLowId;
    }

    public UUID getParticipantHighId() {
        return participantHighId;
    }

    public void setParticipantHighId(UUID participantHighId) {
        this.participantHighId = participantHighId;
    }

    public String getLastMessagePreview() {
        return lastMessagePreview;
    }

    public void setLastMessagePreview(String lastMessagePreview) {
        this.lastMessagePreview = lastMessagePreview;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }
}
