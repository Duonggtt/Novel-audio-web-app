package com.spring3.oauth.jwt.repositories;

import com.spring3.oauth.jwt.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // You can add custom query methods here if needed
    List<ChatMessage> findByChatRoomIdOrderByTimestampAsc(Long roomId);
    List<ChatMessage> findByChatRoomIdAndTimestampGreaterThanOrderByTimestampAsc(Long roomId, LocalDateTime since);
}