package com.xdev.ooms.security.messaging.controller;

import com.xdev.ooms.security.messaging.dto.ChatContactDto;
import com.xdev.ooms.security.messaging.dto.ChatConversationDto;
import com.xdev.ooms.security.messaging.dto.ChatMessageDto;
import com.xdev.ooms.security.messaging.dto.SendChatMessageRequest;
import com.xdev.ooms.security.messaging.service.ChatService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/contacts")
    public ResponseEntity<List<ChatContactDto>> contacts() {
        return ResponseEntity.ok(chatService.listContacts());
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<ChatConversationDto>> conversations() {
        return ResponseEntity.ok(chatService.listConversations());
    }

    @PostMapping("/conversations/with/{userId}")
    public ResponseEntity<ChatConversationDto> openConversation(@PathVariable UUID userId) {
        return ResponseEntity.ok(chatService.openConversationWith(userId));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<Map<String, Object>> messages(
            @PathVariable UUID conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<ChatMessageDto> result = chatService.listMessages(conversationId, page, size);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", result.getContent(),
                "total", result.getTotalElements(),
                "page", result.getNumber() + 1,
                "totalPages", result.getTotalPages()));
    }

    @PostMapping("/messages")
    public ResponseEntity<ChatMessageDto> send(@RequestBody SendChatMessageRequest request) {
        return ResponseEntity.ok(chatService.sendMessage(request));
    }

    @PatchMapping("/conversations/{conversationId}/read")
    public ResponseEntity<Map<String, Object>> markRead(@PathVariable UUID conversationId) {
        long updated = chatService.markConversationRead(conversationId);
        return ResponseEntity.ok(Map.of("success", true, "updated", updated));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> unreadCount() {
        return ResponseEntity.ok(Map.of("success", true, "count", chatService.unreadCount()));
    }
}
