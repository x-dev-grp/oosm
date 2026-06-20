package com.xdev.ooms.security.messaging.controller;

import com.xdev.ooms.security.messaging.dto.ChatMessageDto;
import com.xdev.ooms.security.messaging.dto.SendChatMessageRequest;
import com.xdev.ooms.security.messaging.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatWebSocketController {

    private final ChatService chatService;

    public ChatWebSocketController(ChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/chat.send")
    public ChatMessageDto send(SendChatMessageRequest request, Principal principal) {
        return chatService.sendMessage(request, principal);
    }
}
