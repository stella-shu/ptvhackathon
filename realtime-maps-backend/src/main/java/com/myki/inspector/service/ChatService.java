package com.myki.inspector.service;

import com.myki.inspector.dto.ChatMessageDto;
import com.myki.inspector.entity.ChatMessage;
import com.myki.inspector.repository.ChatMessageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class ChatService {
    private final SimpMessagingTemplate bus;
    private final ChatMessageRepository repo;
    private static final Pattern CHANNEL_PATTERN = Pattern.compile("^[A-Za-z0-9:_-]{1,120}$");

    public ChatService(SimpMessagingTemplate bus, ChatMessageRepository repo) {
        this.bus = bus;
        this.repo = repo;
    }

    public ChatMessageDto sendMessage(ChatMessageDto in) {
        MessagePayload payload = normalizePayload(in);

        ChatMessage m = new ChatMessage();
        m.setChannelId(payload.channelId());
        m.setSenderId(payload.senderId());
        m.setSenderName(payload.senderName());
        m.setContent(payload.content());
        m.setPinLat(payload.pinLat());
        m.setPinLon(payload.pinLon());
        m.setType(payload.type());

        ChatMessage saved = repo.save(m);

        // map & broadcast
        ChatMessageDto out = ChatMessageDto.fromEntity(saved);
        bus.convertAndSend("/topic/channels/" + out.getChannelId(), out);
        return out;
    }

    public List<ChatMessageDto> getChannelHistory(String channelId) {
        String normalizedChannel = normalizeChannelId(channelId);
        var list = repo.findRecentMessagesByChannel(normalizedChannel);
        Collections.reverse(list); // oldest -> newest for UI
        return list.stream().map(ChatMessageDto::fromEntity).toList();
    }

    public ChatMessageDto joinChannel(String channelId, String inspectorId, String inspectorName) {
        String normalizedChannel = normalizeChannelId(channelId);
        String sanitizedId = normalizeSenderId(inspectorId);
        String sanitizedName = normalizeRequired(inspectorName, 120, "senderName");

        ChatMessage sys = new ChatMessage();
        sys.setChannelId(normalizedChannel);
        sys.setSenderId("SYSTEM");
        sys.setSenderName("System");
        sys.setContent(sanitizedName + " joined the channel");
        sys.setType(ChatMessage.MessageType.SYSTEM);
        ChatMessage saved = repo.save(sys);

        ChatMessageDto dto = ChatMessageDto.fromEntity(saved);
        bus.convertAndSend("/topic/channels/" + normalizedChannel, dto);
        return dto;
    }

    public ChatMessageDto sendLocationAlert(String channelId, String senderId, String senderName,
                                            Double lat, Double lon, String alertMessage) {
        String normalizedChannel = normalizeChannelId(channelId);
        String sanitizedSenderId = normalizeSenderId(senderId);
        String sanitizedSenderName = normalizeRequired(senderName, 120, "senderName");
        String sanitizedContent = normalizeContent(alertMessage);
        CoordinatePair coordinates = normalizeCoordinates(lat, lon);

        ChatMessage alert = new ChatMessage();
        alert.setChannelId(normalizedChannel);
        alert.setSenderId(sanitizedSenderId);
        alert.setSenderName(sanitizedSenderName);
        alert.setContent(sanitizedContent);
        alert.setPinLat(coordinates.lat());
        alert.setPinLon(coordinates.lon());
        alert.setType(ChatMessage.MessageType.LOCATION_ALERT);
        ChatMessage saved = repo.save(alert);

        ChatMessageDto dto = ChatMessageDto.fromEntity(saved);
        bus.convertAndSend("/topic/channels/" + normalizedChannel, dto);
        return dto;
    }

    private MessagePayload normalizePayload(ChatMessageDto in) {
        if (in == null) {
            throw badRequest("message payload is required");
        }
        String channelId = normalizeChannelId(in.getChannelId());
        String senderId = normalizeSenderId(in.getSenderId());
        String senderName = normalizeRequired(in.getSenderName(), 120, "senderName");
        String content = normalizeContent(in.getContent());
        ChatMessage.MessageType type = in.getType() != null ? in.getType() : ChatMessage.MessageType.CHAT;
        CoordinatePair coordinates = normalizeCoordinates(in.getPinLat(), in.getPinLon());
        return new MessagePayload(channelId, senderId, senderName, content, coordinates.lat(), coordinates.lon(), type);
    }

    private String normalizeChannelId(String value) {
        String trimmed = normalizeRequired(value, 120, "channelId");
        if (!CHANNEL_PATTERN.matcher(trimmed).matches()) {
            throw badRequest("channelId may only contain letters, numbers, :, _, or -");
        }
        return trimmed;
    }

    private String normalizeSenderId(String value) {
        String trimmed = normalizeRequired(value, 64, "senderId");
        if (!CHANNEL_PATTERN.matcher(trimmed).matches()) {
            throw badRequest("senderId may only contain letters, numbers, :, _, or -");
        }
        return trimmed;
    }

    private String normalizeRequired(String value, int maxLength, String field) {
        if (!StringUtils.hasText(value)) {
            throw badRequest(field + " is required");
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw badRequest(field + " is required");
        }
        if (trimmed.length() > maxLength) {
            throw badRequest(field + " must be <= " + maxLength + " characters");
        }
        return trimmed;
    }

    private String normalizeContent(String value) {
        if (value == null) {
            throw badRequest("content is required");
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw badRequest("content cannot be blank");
        }
        if (trimmed.length() > 2000) {
            throw badRequest("content must be <= 2000 characters");
        }
        return trimmed;
    }

    private CoordinatePair normalizeCoordinates(Double lat, Double lon) {
        if (lat == null && lon == null) {
            return new CoordinatePair(null, null);
        }
        if (lat == null || lon == null) {
            throw badRequest("pinLat and pinLon must both be provided");
        }
        if (!Double.isFinite(lat) || lat < -90 || lat > 90) {
            throw badRequest("pinLat must be between -90 and 90");
        }
        if (!Double.isFinite(lon) || lon < -180 || lon > 180) {
            throw badRequest("pinLon must be between -180 and 180");
        }
        return new CoordinatePair(lat, lon);
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private record MessagePayload(
            String channelId,
            String senderId,
            String senderName,
            String content,
            Double pinLat,
            Double pinLon,
            ChatMessage.MessageType type
    ) {}

    private record CoordinatePair(Double lat, Double lon) {}
}
