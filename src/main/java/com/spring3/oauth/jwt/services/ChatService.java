package com.spring3.oauth.jwt.services;

import com.spring3.oauth.jwt.entity.ChatMessage;
import com.spring3.oauth.jwt.entity.ChatRoom;
import com.spring3.oauth.jwt.entity.User;
import com.spring3.oauth.jwt.models.request.ChatRoomRequest;
import com.spring3.oauth.jwt.models.request.JoinChatRequest;
import com.spring3.oauth.jwt.repositories.ChatMessageRepository;
import com.spring3.oauth.jwt.repositories.ChatRoomRepository;
import com.spring3.oauth.jwt.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional(readOnly = true)
    public List<ChatRoom> getAllChatRooms() {
        return chatRoomRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ChatRoom> getChatRoomsByAuthorId(Long authorId) {
        User author = userRepository.findFirstById(authorId);
        if (author == null) {
            throw new RuntimeException("Author not found");
        }
        return chatRoomRepository.findByAuthorId(authorId);
    }

    @Transactional(readOnly = true)
    public List<ChatRoom> getUserChatRooms(Long userId) {
        User user = userRepository.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return chatRoomRepository.findByParticipantsContaining(user);
    }

    @Transactional
    public ChatRoom createChatRoom(ChatRoomRequest chatRoomRequest) {
        User author = userRepository.getUserById(chatRoomRequest.getUserId());
        if (author == null) {
            throw new RuntimeException("Author not found");
        }

        // Check if user has ROLE_AUTHOR
//        if (!userRepository.hasAuthorRole(chatRoomRequest.getUserId())) {
//            throw new RuntimeException("Only authors can create chat rooms");
//        }

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setAuthor(author);
        chatRoom.setName(chatRoomRequest.getName());
        return chatRoomRepository.save(chatRoom);
    }

    @Transactional
    public void joinChatRoom(JoinChatRequest joinChatRequest) {
        ChatRoom chatRoom = chatRoomRepository.findById(joinChatRequest.getChatRoomId())
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        User user = userRepository.getUserById(joinChatRequest.getUserId());
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // Check if user follows the author using User entity relationships
        User author = chatRoom.getAuthor();
        if (!author.getFollowers().contains(user)) {
            throw new RuntimeException("You must follow the author to join this chat room");
        }

        // Check room capacity
        if (chatRoom.getParticipants().size() >= chatRoom.getMaxParticipants()) {
            throw new RuntimeException("Chat room is full");
        }

        chatRoom.getParticipants().add(user);
        chatRoomRepository.save(chatRoom);
    }

    @Transactional
    public void handleUnfollow(Long userId, Long authorId) {
        // Get all chat rooms by this author
        List<ChatRoom> authorChatRooms = chatRoomRepository.findByAuthorId(authorId);

        // Remove user from all author's chat rooms
        for (ChatRoom chatRoom : authorChatRooms) {
            removeUserFromChatRoom(userId, chatRoom.getId());
        }
    }

    @Transactional
    public void removeUserFromChatRoom(Long userId, Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        User user = userRepository.getUserById(userId);
        if (user != null && chatRoom.getParticipants().contains(user)) {
            chatRoom.getParticipants().remove(user);
            chatRoomRepository.save(chatRoom);
        }
    }

    @Transactional
    public ChatMessage createAndSaveChatMessage(ChatMessage chatMessage, Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));
        chatMessage.setChatRoom(chatRoom);
        return chatMessageRepository.save(chatMessage);
    }

    public List<ChatMessage> getRoomChatHistory(Long roomId) {
        return chatMessageRepository.findByChatRoomIdOrderByTimestampAsc(roomId);
    }

    public List<ChatMessage> getRoomChatHistorySince(Long roomId, LocalDateTime since) {
        return chatMessageRepository.findByChatRoomIdAndTimestampGreaterThanOrderByTimestampAsc(roomId, since);
    }

    @Transactional(readOnly = true)
    public void validateUserInRoom(Long userId, Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        User user = userRepository.findFirstById(userId);
        if (user == null || !chatRoom.getParticipants().contains(user)) {
            throw new RuntimeException("User is not in this chat room");
        }
    }

    @Transactional
    public void validateAndJoinRoom(Long userId, Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        User user = userRepository.findFirstById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        User author = chatRoom.getAuthor();
        if (!author.getFollowers().contains(user)) {
            throw new RuntimeException("You must follow the author to join this chat room");
        }

        if (chatRoom.getParticipants().size() >= chatRoom.getMaxParticipants()) {
            throw new RuntimeException("Chat room is full");
        }

        if (!chatRoom.getParticipants().contains(user)) {
            chatRoom.getParticipants().add(user);
            chatRoomRepository.save(chatRoom);
        }
    }
}