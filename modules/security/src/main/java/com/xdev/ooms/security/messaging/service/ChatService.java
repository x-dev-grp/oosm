package com.xdev.ooms.security.messaging.service;

import com.xdev.ooms.security.messaging.dto.ChatContactDto;
import com.xdev.ooms.security.messaging.dto.ChatConversationDto;
import com.xdev.ooms.security.messaging.dto.ChatMessageDto;
import com.xdev.ooms.security.messaging.dto.SendChatMessageRequest;
import com.xdev.ooms.security.messaging.entity.ChatConversation;
import com.xdev.ooms.security.messaging.entity.ChatMessage;
import com.xdev.ooms.security.messaging.repository.ChatConversationRepository;
import com.xdev.ooms.security.messaging.repository.ChatMessageRepository;
import com.xdev.ooms.security.user.entity.OSMUser;
import com.xdev.ooms.security.user.repository.UserRepository;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import com.xdev.ooms.sharedkernel.utils.AuditHelper;
import com.xdev.ooms.sharedkernel.utils.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ChatService {

    public static final String USER_MESSAGES_DESTINATION = "/queue/messages";
    public static final String USER_UNREAD_DESTINATION = "/queue/unread";

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatService(
            ChatConversationRepository conversationRepository,
            ChatMessageRepository messageRepository,
            UserRepository userRepository,
            SimpMessagingTemplate messagingTemplate) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public List<ChatContactDto> listContacts() {
        OSMUser current = requireCurrentUser();
        UUID tenantId = requireTenantId(current);

        return userRepository.findActiveUsersByTenantExcluding(tenantId, current.getId()).stream()
                .map(this::toContactDto)
                .sorted(Comparator.comparing(ChatContactDto::getDisplayName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public List<ChatConversationDto> listConversations() {
        OSMUser current = requireCurrentUser();
        UUID tenantId = requireTenantId(current);

        List<ChatConversation> conversations = conversationRepository.findForUser(tenantId, current.getId());
        Map<UUID, OSMUser> usersById = loadUsersForConversations(conversations, current.getId());

        List<ChatConversationDto> result = new ArrayList<>();
        for (ChatConversation conversation : conversations) {
            UUID otherUserId = otherParticipantId(conversation, current.getId());
            OSMUser otherUser = usersById.get(otherUserId);
            if (otherUser == null) {
                continue;
            }
            ChatConversationDto dto = toConversationDto(conversation, otherUser);
            dto.setUnreadCount(countUnreadInConversation(conversation.getId(), current.getId()));
            result.add(dto);
        }
        return result;
    }

    public Page<ChatMessageDto> listMessages(UUID conversationId, int page, int size) {
        OSMUser current = requireCurrentUser();
        ChatConversation conversation = requireConversationForUser(conversationId, current);

        Page<ChatMessage> messages = messageRepository.findByConversationIdAndIsDeletedFalseOrderByCreatedDateDesc(
                conversation.getId(),
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100)));

        Map<UUID, OSMUser> usersById = loadUsersForMessages(messages.getContent());

        return messages.map(message -> toMessageDto(message, current.getId(), usersById.get(message.getSenderUserId())));
    }

    @Transactional
    public ChatConversationDto openConversationWith(UUID otherUserId) {
        OSMUser current = requireCurrentUser();
        OSMUser other = requireSameTenantUser(otherUserId, current);
        ChatConversation conversation = findOrCreateConversation(current, other);
        ChatConversationDto dto = toConversationDto(conversation, other);
        dto.setUnreadCount(countUnreadInConversation(conversation.getId(), current.getId()));
        return dto;
    }

    @Transactional
    public ChatMessageDto sendMessage(SendChatMessageRequest request) {
        return sendMessage(request, null);
    }

    @Transactional
    public ChatMessageDto sendMessage(SendChatMessageRequest request, Principal principal) {
        OSMUser current = requireCurrentUser(principal);
        if (request == null || request.getBody() == null || request.getBody().isBlank()) {
            throw new IllegalArgumentException("Message body is required");
        }

        ChatConversation conversation;
        OSMUser recipient;

        if (request.getConversationId() != null) {
            conversation = requireConversationForUser(request.getConversationId(), current);
            recipient = requireOtherParticipant(conversation, current);
        } else if (request.getRecipientUserId() != null) {
            recipient = requireSameTenantUser(request.getRecipientUserId(), current);
            conversation = findOrCreateConversation(current, recipient);
        } else {
            throw new IllegalArgumentException("conversationId or recipientUserId is required");
        }

        ChatMessage message = new ChatMessage();
        message.setConversationId(conversation.getId());
        message.setTenantId(conversation.getTenantId());
        message.setSenderUserId(current.getId());
        message.setBody(request.getBody().trim());
        AuditHelper.applyAuditOnCreate(message);
        messageRepository.save(message);

        conversation.setLastMessagePreview(truncatePreview(message.getBody()));
        conversation.setLastMessageAt(message.getCreatedDate());
        conversationRepository.save(conversation);

        ChatMessageDto dto = toMessageDto(message, current.getId(), current);
        dto.setMine(true);

        ChatMessageDto recipientDto = toMessageDto(message, recipient.getId(), current);
        recipientDto.setMine(false);

        pushToUser(current.getUsername(), dto);
        pushToUser(recipient.getUsername(), recipientDto);
        pushUnreadCount(recipient);

        return dto;
    }

    @Transactional
    public long markConversationRead(UUID conversationId) {
        OSMUser current = requireCurrentUser();
        requireConversationForUser(conversationId, current);
        int updated = messageRepository.markConversationReadForUser(conversationId, current.getId(), LocalDateTime.now());
        pushUnreadCount(current);
        return updated;
    }

    public long unreadCount() {
        OSMUser current = requireCurrentUser();
        UUID tenantId = requireTenantId(current);
        return messageRepository.countUnreadForUser(tenantId, current.getId());
    }

    private void pushToUser(String username, ChatMessageDto dto) {
        if (username == null || username.isBlank()) {
            return;
        }
        messagingTemplate.convertAndSendToUser(username, USER_MESSAGES_DESTINATION, dto);
    }

    private void pushUnreadCount(OSMUser user) {
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            return;
        }
        UUID tenantId = requireTenantId(user);
        long count = messageRepository.countUnreadForUser(tenantId, user.getId());
        messagingTemplate.convertAndSendToUser(user.getUsername(), USER_UNREAD_DESTINATION, Map.of("count", count));
    }

    private long countUnreadInConversation(UUID conversationId, UUID currentUserId) {
        return messageRepository.findByConversationIdAndIsDeletedFalseOrderByCreatedDateDesc(
                        conversationId,
                        PageRequest.of(0, 200))
                .stream()
                .filter(message -> message.getReadAt() == null)
                .filter(message -> !currentUserId.equals(message.getSenderUserId()))
                .count();
    }

    private ChatConversation findOrCreateConversation(OSMUser current, OSMUser other) {
        UUID tenantId = requireTenantId(current);
        UUID low = minUuid(current.getId(), other.getId());
        UUID high = maxUuid(current.getId(), other.getId());

        return conversationRepository
                .findByTenantIdAndParticipantLowIdAndParticipantHighIdAndIsDeletedFalse(tenantId, low, high)
                .orElseGet(() -> {
                    ChatConversation conversation = new ChatConversation();
                    conversation.setTenantId(tenantId);
                    conversation.setParticipantLowId(low);
                    conversation.setParticipantHighId(high);
                    AuditHelper.applyAuditOnCreate(conversation);
                    return conversationRepository.save(conversation);
                });
    }

    private ChatConversation requireConversationForUser(UUID conversationId, OSMUser current) {
        ChatConversation conversation = conversationRepository.findById(conversationId)
                .filter(item -> !Boolean.TRUE.equals(item.getDeleted()))
                .orElseThrow(() -> new EntityNotFoundException("Conversation not found"));

        UUID tenantId = requireTenantId(current);
        if (!tenantId.equals(conversation.getTenantId())) {
            throw new AccessDeniedException("Conversation not accessible");
        }
        if (!isParticipant(conversation, current.getId())) {
            throw new AccessDeniedException("Conversation not accessible");
        }
        return conversation;
    }

    private OSMUser requireOtherParticipant(ChatConversation conversation, OSMUser current) {
        UUID otherUserId = otherParticipantId(conversation, current.getId());
        return userRepository.findById(otherUserId)
                .filter(user -> !Boolean.TRUE.equals(user.getDeleted()))
                .orElseThrow(() -> new EntityNotFoundException("Recipient not found"));
    }

    private OSMUser requireSameTenantUser(UUID userId, OSMUser current) {
        if (userId == null || userId.equals(current.getId())) {
            throw new IllegalArgumentException("Invalid recipient");
        }
        OSMUser other = userRepository.findById(userId)
                .filter(user -> !Boolean.TRUE.equals(user.getDeleted()))
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        UUID tenantId = requireTenantId(current);
        if (other.getTenantId() == null || !tenantId.equals(other.getTenantId())) {
            throw new AccessDeniedException("Users must belong to the same tenant");
        }
        return other;
    }

    private OSMUser requireCurrentUser() {
        return requireCurrentUser(null);
    }

    private OSMUser requireCurrentUser(Principal principal) {
        String username = SecurityUtils.getCurrentUsername().orElse(null);
        if ((username == null || username.isBlank()) && principal != null) {
            username = principal.getName();
        }
        if (username == null || username.isBlank()) {
            throw new AccessDeniedException("Authenticated user required");
        }
        return userRepository.findByUsernameAndIsDeletedFalse(username)
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user not found"));
    }

    private UUID requireTenantId(OSMUser user) {
        UUID tenantId = user.getTenantId() != null ? user.getTenantId() : TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new AccessDeniedException("Tenant context is required for messaging");
        }
        return tenantId;
    }

    private boolean isParticipant(ChatConversation conversation, UUID userId) {
        return userId.equals(conversation.getParticipantLowId()) || userId.equals(conversation.getParticipantHighId());
    }

    private UUID otherParticipantId(ChatConversation conversation, UUID currentUserId) {
        return currentUserId.equals(conversation.getParticipantLowId())
                ? conversation.getParticipantHighId()
                : conversation.getParticipantLowId();
    }

    private Map<UUID, OSMUser> loadUsersForConversations(List<ChatConversation> conversations, UUID currentUserId) {
        List<UUID> ids = conversations.stream()
                .map(conversation -> otherParticipantId(conversation, currentUserId))
                .distinct()
                .toList();
        return userRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(OSMUser::getId, Function.identity()));
    }

    private Map<UUID, OSMUser> loadUsersForMessages(List<ChatMessage> messages) {
        List<UUID> ids = messages.stream().map(ChatMessage::getSenderUserId).distinct().toList();
        return userRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(OSMUser::getId, Function.identity()));
    }

    private ChatContactDto toContactDto(OSMUser user) {
        ChatContactDto dto = new ChatContactDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setDisplayName(displayName(user));
        dto.setRoleName(user.getRole() != null ? user.getRole().getRoleName() : null);
        return dto;
    }

    private ChatConversationDto toConversationDto(ChatConversation conversation, OSMUser otherUser) {
        ChatConversationDto dto = new ChatConversationDto();
        dto.setId(conversation.getId());
        dto.setOtherUserId(otherUser.getId());
        dto.setOtherUsername(otherUser.getUsername());
        dto.setOtherDisplayName(displayName(otherUser));
        dto.setLastMessagePreview(conversation.getLastMessagePreview());
        dto.setLastMessageAt(conversation.getLastMessageAt());
        return dto;
    }

    private ChatMessageDto toMessageDto(ChatMessage message, UUID viewerUserId, OSMUser sender) {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(message.getId());
        dto.setConversationId(message.getConversationId());
        dto.setSenderUserId(message.getSenderUserId());
        dto.setBody(message.getBody());
        dto.setCreatedDate(message.getCreatedDate());
        dto.setRead(message.getReadAt() != null);
        dto.setMine(viewerUserId.equals(message.getSenderUserId()));
        if (sender != null) {
            dto.setSenderUsername(sender.getUsername());
            dto.setSenderDisplayName(displayName(sender));
        }
        return dto;
    }

    private String displayName(OSMUser user) {
        String fullName = ((user.getFirstName() != null ? user.getFirstName() : "") + " "
                + (user.getLastName() != null ? user.getLastName() : "")).trim();
        return fullName.isBlank() ? user.getUsername() : fullName;
    }

    private String truncatePreview(String body) {
        if (body == null) {
            return "";
        }
        return body.length() <= 500 ? body : body.substring(0, 497) + "...";
    }

    private UUID minUuid(UUID left, UUID right) {
        return left.compareTo(right) <= 0 ? left : right;
    }

    private UUID maxUuid(UUID left, UUID right) {
        return left.compareTo(right) >= 0 ? left : right;
    }
}
