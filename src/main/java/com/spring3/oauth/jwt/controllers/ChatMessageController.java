package com.spring3.oauth.jwt.controllers;

import com.spring3.oauth.jwt.entity.ChatMessage;
import com.spring3.oauth.jwt.entity.ChatRoom;
import com.spring3.oauth.jwt.entity.enums.MessageTypeEnum;
import com.spring3.oauth.jwt.models.dtos.ChatMessageDto;
import com.spring3.oauth.jwt.models.dtos.ChatRoomResponseDTO;
import com.spring3.oauth.jwt.services.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
public class ChatMessageController {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat.sendMessage/{roomId}")
    @SendTo("/chat/{roomId}")
    public ChatMessageDto sendMessage(
            @DestinationVariable Long roomId,
            @Payload ChatMessage chatMessage
    ) {
        if (chatMessage.getSenderUser() == null) {
            throw new RuntimeException("Sender cannot be null");
        }
        // Validate user is in room
        chatService.validateUserInRoom(chatMessage.getSenderUser().getId(), roomId);

        // Save and broadcast message
        ChatMessage savedMessage = chatService.createAndSaveChatMessage(chatMessage, roomId);
        return chatService.convertToDto(savedMessage);
    }

    @GetMapping("/rooms/user/{userId}")
    public List<ChatRoomResponseDTO> getUserChatRooms(@PathVariable Long userId) {
        return chatService.getUserChatRooms(userId);
    }

    @GetMapping("/history/{roomId}")
    public List<?> getChatHistory(@PathVariable Long roomId) {
        return chatService.getRoomChatHistory(roomId);
    }




}