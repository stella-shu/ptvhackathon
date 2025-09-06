package com.myki.inspector.repository;

import com.myki.inspector.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT c FROM ChatMessage c WHERE c.channelId = ?1 ORDER BY c.createdAt DESC")
    List<ChatMessage> findByChannelIdOrderByCreatedAtDesc(String channelId);

    @Query(value = "SELECT * FROM chat_messages WHERE channel_id = ?1 ORDER BY created_at DESC LIMIT 50", nativeQuery = true)
    List<ChatMessage> findRecentMessagesByChannel(String channelId);

    List<ChatMessage> findByChannelIdAndTypeOrderByCreatedAtDesc(String channelId, ChatMessage.MessageType type);
}