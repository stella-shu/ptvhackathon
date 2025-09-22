package com.myki.inspector.dto;

import com.myki.inspector.entity.ChatMessage;
import java.time.Instant;

public class ChatMessageDto {
    private Long id;                 // message id (for UI keys)
    private String channelId;
    private String senderId;
    private String senderName;
    private String content;
    private Double pinLat;
    private Double pinLon;
    private ChatMessage.MessageType type = ChatMessage.MessageType.CHAT;
    private Instant ts = Instant.now(); // server timestamp

    public ChatMessageDto() {}

    // Mapper from entity -> DTO (use for outbound/broadcast)
    public static ChatMessageDto fromEntity(ChatMessage m) {
        ChatMessageDto dto = new ChatMessageDto();
        dto.id = m.getId();
        dto.channelId = m.getChannelId();
        dto.senderId = m.getSenderId();
        dto.senderName = m.getSenderName();
        dto.content = m.getContent();
        dto.pinLat = m.getPinLat();
        dto.pinLon = m.getPinLon();
        dto.type = m.getType();
        dto.ts = m.getCreatedAt();
        return dto;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getChannelId() { return channelId; }
    public void setChannelId(String channelId) { this.channelId = channelId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Double getPinLat() { return pinLat; }
    public void setPinLat(Double pinLat) { this.pinLat = pinLat; }

    public Double getPinLon() { return pinLon; }
    public void setPinLon(Double pinLon) { this.pinLon = pinLon; }

    public ChatMessage.MessageType getType() { return type; }
    public void setType(ChatMessage.MessageType type) { this.type = type; }

    public Instant getTs() { return ts; }
    public void setTs(Instant ts) { this.ts = ts; }
}