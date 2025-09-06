package com.myki.inspector.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_msg_channel_created", columnList = "channel_id,created_at")
})
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="channel_id", nullable = false)
    private String channelId;         // e.g. "ops-global" or "dm:INS001:INS007"

    @Column(name="sender_id", nullable = false)
    private String senderId;          // e.g. "INS001"

    @Column(name="sender_name", nullable = false)
    private String senderName;        // display name (or same as id)

    @Column(nullable = false, length = 2000)
    private String content;           // message text

    @Column(name="pin_lat")
    private Double pinLat;            // optional map pin

    @Column(name="pin_lon")
    private Double pinLon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type = MessageType.CHAT;

    @Column(name="created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public enum MessageType { CHAT, LOCATION_ALERT, SYSTEM, HOTSPOT_ALERT }

    public ChatMessage() {}

    // Getters/setters
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

    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
