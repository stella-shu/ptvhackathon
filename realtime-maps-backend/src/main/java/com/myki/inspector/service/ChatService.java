package com.myki.inspector.service;

import com.myki.inspector.dto.ChatMessageDto;
import com.myki.inspector.entity.ChatMessage;
import com.myki.inspector.repository.ChatMessageRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;

@Service
public class ChatService {
    private final SimpMessagingTemplate bus;
    private final ChatMessageRepository repo;

    public ChatService(SimpMessagingTemplate bus, ChatMessageRepository repo) {
        this.bus = bus;
        this.repo = repo;
    }

    public ChatMessageDto sendMessage(ChatMessageDto in) {
        // persist
        ChatMessage m = new ChatMessage();
        m.setChannelId(in.getChannelId());
        m.setSenderId(in.getSenderId());
        m.setSenderName(in.getSenderName());
        m.setContent(in.getContent());
        m.setPinLat(in.getPinLat());
        m.setPinLon(in.getPinLon());
        m.setType(in.getType() != null ? in.getType() : ChatMessage.MessageType.CHAT);
        ChatMessage saved = repo.save(m);

        // map & broadcast
        ChatMessageDto out = ChatMessageDto.fromEntity(saved);
        bus.convertAndSend("/topic/channels/" + out.getChannelId(), out);
        return out;
    }

    public List<ChatMessageDto> getChannelHistory(String channelId) {
        var list = repo.findRecentMessagesByChannel(channelId);
        Collections.reverse(list); // oldest -> newest for UI
        return list.stream().map(ChatMessageDto::fromEntity).toList();
    }

    public ChatMessageDto joinChannel(String channelId, String inspectorId, String inspectorName) {
        ChatMessage sys = new ChatMessage();
        sys.setChannelId(channelId);
        sys.setSenderId("SYSTEM");
        sys.setSenderName("System");
        sys.setContent(inspectorName + " joined the channel");
        sys.setType(ChatMessage.MessageType.SYSTEM);
        ChatMessage saved = repo.save(sys);

        ChatMessageDto dto = ChatMessageDto.fromEntity(saved);
        bus.convertAndSend("/topic/channels/" + channelId, dto);
        return dto;
    }

    public ChatMessageDto sendLocationAlert(String channelId, String senderId, String senderName,
                                            Double lat, Double lon, String alertMessage) {
        ChatMessage alert = new ChatMessage();
        alert.setChannelId(channelId);
        alert.setSenderId(senderId);
        alert.setSenderName(senderName);
        alert.setContent(alertMessage);
        alert.setPinLat(lat);
        alert.setPinLon(lon);
        alert.setType(ChatMessage.MessageType.LOCATION_ALERT);
        ChatMessage saved = repo.save(alert);

        ChatMessageDto dto = ChatMessageDto.fromEntity(saved);
        bus.convertAndSend("/topic/channels/" + channelId, dto);
        return dto;
    }
}