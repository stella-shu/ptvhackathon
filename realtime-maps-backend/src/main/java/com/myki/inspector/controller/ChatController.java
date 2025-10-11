package com.myki.inspector.controller;

import com.myki.inspector.dto.ChatMessageDto;
import com.myki.inspector.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // WebSocket: Client sends to /app/channels/{channelId}/send
    @MessageMapping("/channels/{channelId}/send")
    public void sendMessage(@DestinationVariable String channelId,
                            @Payload ChatMessageDto message) {
        message.setChannelId(channelId); // trust the URL path
        chatService.sendMessage(message);
    }

    // WebSocket: Client sends to /app/channels/{channelId}/join
    @MessageMapping("/channels/{channelId}/join")
    public void joinChannel(@DestinationVariable String channelId,
                            @Payload ChatMessageDto message,
                            SimpMessageHeaderAccessor headerAccessor) {
        // Store session info
        if (headerAccessor.getSessionAttributes() != null) {
            headerAccessor.getSessionAttributes().put("username", message.getSenderName());
            headerAccessor.getSessionAttributes().put("channelId", channelId);
            headerAccessor.getSessionAttributes().put("userId", message.getSenderId());
        }

        chatService.joinChannel(channelId, message.getSenderId(), message.getSenderName());
    }
}

@RestController
@RequestMapping("/api/channels")
@CrossOrigin(origins = "*")
class ChatRestController {
    private final ChatService chatService;

    public ChatRestController(ChatService chatService) {
        this.chatService = chatService;
    }

    // GET /api/channels/{channelId}/messages
    @GetMapping("/{channelId}/messages")
    public List<ChatMessageDto> getChannelHistory(@PathVariable String channelId) {
        return chatService.getChannelHistory(channelId);
    }

    // POST /api/channels/{channelId}/messages (alternative to WebSocket)
    @PostMapping("/{channelId}/messages")
    public ChatMessageDto sendMessageRest(@PathVariable String channelId,
                                          @Valid @RequestBody ChatMessageDto message) {
        message.setChannelId(channelId);
        return chatService.sendMessage(message);
    }
}
