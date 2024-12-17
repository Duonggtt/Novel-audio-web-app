package com.spring3.oauth.jwt.controllers;


import com.spring3.oauth.jwt.entity.ChatRoom;
import com.spring3.oauth.jwt.models.dtos.ChatRoomResponseDTO;
import com.spring3.oauth.jwt.models.request.ChatRoomRequest;
import com.spring3.oauth.jwt.models.request.JoinChatRequest;
import com.spring3.oauth.jwt.services.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ChatroomController {
    private final ChatService chatService;
    @PostMapping("/chat-rooms/create")
    @PreAuthorize("hasRole('ROLE_AUTHOR')")
    public ResponseEntity<?> createChatRoom(
            @RequestBody ChatRoomRequest chatRoomRequest
    ) {
        return ResponseEntity.ok(chatService.createChatRoom(chatRoomRequest));
    }

    @PostMapping("/chat-rooms/join")
    public ResponseEntity<Void> joinChatRoom(
            @RequestBody JoinChatRequest joinChatRequest
            ) {
        chatService.joinChatRoom(joinChatRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/chat-rooms")
    public ResponseEntity<List<?>> getAllChatRooms(@RequestParam Long userId) {
        List<ChatRoomResponseDTO> chatRooms = chatService.getAllAvailableChatRooms(userId);
        return ResponseEntity.ok(chatRooms);
    }

    @GetMapping("/authors/{authorId}/chat-rooms")
    public ResponseEntity<List<ChatRoom>> getChatRoomsByAuthorId(
            @PathVariable Long authorId
    ) {
        List<ChatRoom> chatRooms = chatService.getChatRoomsByAuthorId(authorId);
        return ResponseEntity.ok(chatRooms);
    }

    @DeleteMapping("/chat-rooms/{chatRoomId}/leave")
    public ResponseEntity<Void> leaveChatRoom(
            @PathVariable Long chatRoomId,
            @RequestParam Long userId
    ) {
        chatService.removeUserFromChatRoom(userId, chatRoomId);
        return ResponseEntity.ok().build();
    }
}