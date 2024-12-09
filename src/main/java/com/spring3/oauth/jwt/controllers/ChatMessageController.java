package com.spring3.oauth.jwt.controllers;

import com.spring3.oauth.jwt.entity.ChatMessage;
import com.spring3.oauth.jwt.entity.ChatRoom;
import com.spring3.oauth.jwt.entity.enums.MessageTypeEnum;
import com.spring3.oauth.jwt.models.dtos.ChatRoomResponseDTO;
import com.spring3.oauth.jwt.services.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
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

    @MessageMapping("/sendMessage/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, @Payload ChatMessage chatMessage) {
        chatService.validateUserInRoom(chatMessage.getSenderUser().getId(), roomId);
        chatMessage.setType(MessageTypeEnum.CHAT);
        // Save to database
        ChatMessage savedMessage = chatService.createAndSaveChatMessage(chatMessage, roomId);
        messagingTemplate.convertAndSend("/topic/room/" + roomId, savedMessage);
    }

    @GetMapping("/rooms/user/{userId}")
    public List<ChatRoomResponseDTO> getUserChatRooms(@PathVariable Long userId) {
        return chatService.getUserChatRooms(userId);
    }

    @GetMapping("/history/{roomId}")
    public List<?> getChatHistory(@PathVariable Long roomId) {
        return chatService.getRoomChatHistory(roomId);
    }

    @GetMapping("/history/{roomId}/since")
    public List<ChatMessage> getChatHistorySince(
            @PathVariable Long roomId,
            @RequestParam LocalDateTime since) {
        return chatService.getRoomChatHistorySince(roomId, since);
    }

//    @MessageMapping("/chat/join/{roomId}")
//    public void joinRoom(@DestinationVariable Long roomId, @Payload ChatMessage chatMessage,
//                         SimpMessageHeaderAccessor headerAccessor) {
//        Long userId = chatMessage.getSenderUser().getId();
//        chatService.validateAndJoinRoom(userId, roomId);
//
//        chatMessage.setType(MessageTypeEnum.JOIN);
//        headerAccessor.getSessionAttributes().put("userId", userId);
//        headerAccessor.getSessionAttributes().put("roomId", roomId);
//
//        messagingTemplate.convertAndSend("/topic/room/" + roomId, chatMessage);
//    }
//
//    @MessageMapping("/chat/leave/{roomId}")
//    public void leaveRoom(@DestinationVariable Long roomId, @Payload ChatMessage chatMessage) {
//        Long userId = chatMessage.getSenderUser().getId();
//        chatService.removeUserFromChatRoom(userId, roomId);
//
//        chatMessage.setType(MessageTypeEnum.LEAVE);
//        messagingTemplate.convertAndSend("/topic/room/" + roomId, chatMessage);
//    }
}