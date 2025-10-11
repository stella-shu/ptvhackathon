package com.myki.inspector.service;

import com.myki.inspector.dto.ChatMessageDto;
import com.myki.inspector.entity.ChatMessage;
import com.myki.inspector.repository.ChatMessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.ExecutorSubscribableChannel;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    private RecordingMessagingTemplate messagingTemplate;
    private ChatService chatService;

    @Test
    void sendMessage_withValidPayload_persistsAndBroadcasts() {
        messagingTemplate = new RecordingMessagingTemplate();
        chatService = new ChatService(messagingTemplate, chatMessageRepository);

        ChatMessage saved = new ChatMessage();
        saved.setId(42L);
        saved.setChannelId("general");
        saved.setSenderId("INS001");
        saved.setSenderName("Inspector One");
        saved.setContent("Hello team");
        saved.setCreatedAt(Instant.now());

        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> {
            ChatMessage entity = invocation.getArgument(0);
            entity.setId(saved.getId());
            entity.setCreatedAt(saved.getCreatedAt());
            return entity;
        });

        ChatMessageDto dto = new ChatMessageDto();
        dto.setChannelId("general");
        dto.setSenderId("INS001");
        dto.setSenderName("Inspector One");
        dto.setContent("Hello team");

        ChatMessageDto response = chatService.sendMessage(dto);

        ArgumentCaptor<ChatMessage> entityCaptor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(chatMessageRepository).save(entityCaptor.capture());
        ChatMessage persisted = entityCaptor.getValue();
        assertThat(persisted.getChannelId()).isEqualTo("general");
        assertThat(persisted.getSenderId()).isEqualTo("INS001");
        assertThat(persisted.getContent()).isEqualTo("Hello team");

        assertThat(messagingTemplate.lastDestination()).isEqualTo("/topic/channels/general");
        assertThat(messagingTemplate.lastPayload()).isInstanceOf(ChatMessageDto.class);

        assertThat(response.getId()).isEqualTo(42L);
        assertThat(response.getChannelId()).isEqualTo("general");
        assertThat(response.getContent()).isEqualTo("Hello team");
    }

    @Test
    void sendMessage_withBlankContent_throwsBadRequest() {
        messagingTemplate = new RecordingMessagingTemplate();
        chatService = new ChatService(messagingTemplate, chatMessageRepository);

        ChatMessageDto dto = new ChatMessageDto();
        dto.setChannelId("general");
        dto.setSenderId("INS002");
        dto.setSenderName("Inspector Two");
        dto.setContent("   ");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> chatService.sendMessage(dto));
        assertThat(ex.getStatusCode().value()).isEqualTo(400);
        assertThat(ex.getReason()).contains("content");

        verify(chatMessageRepository, never()).save(any());
    }

    private static class RecordingMessagingTemplate extends SimpMessagingTemplate {
        private String destination;
        private Object payload;

        RecordingMessagingTemplate() {
            super(new ExecutorSubscribableChannel());
        }

        @Override
        protected void doSend(String destination, Message<?> message) {
            this.destination = destination;
            this.payload = message.getPayload();
        }

        String lastDestination() {
            return destination;
        }

        Object lastPayload() {
            return payload;
        }
    }
}
