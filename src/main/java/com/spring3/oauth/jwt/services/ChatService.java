package com.spring3.oauth.jwt.services;

import com.spring3.oauth.jwt.entity.ChatMessage;
import com.spring3.oauth.jwt.entity.ChatRoom;
import com.spring3.oauth.jwt.entity.User;
import com.spring3.oauth.jwt.entity.enums.ChatRoomTypeEnum;
import com.spring3.oauth.jwt.models.dtos.ChapterResponseDTO;
import com.spring3.oauth.jwt.models.dtos.ChatMessageDto;
import com.spring3.oauth.jwt.models.dtos.ChatRoomResponseDTO;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    public List<ChatRoomResponseDTO> getAllAvailableChatRooms(Long userId) {
        User user = userRepository.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        List<ChatRoom> availableRooms = chatRoomRepository.findAvailableChatRooms(ChatRoomTypeEnum.PUBLIC, ChatRoomTypeEnum.PRIVATE, userId);
        return availableRooms.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }



    @Transactional(readOnly = true)
    public List<ChatRoomResponseDTO> getUserChatRooms(Long userId) {
        User user = userRepository.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        List<ChatRoom> chatRooms = chatRoomRepository.findByParticipantsContaining(user);
        return chatRooms.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ChatRoomResponseDTO createChatRoom(ChatRoomRequest chatRoomRequest) {
        User author = userRepository.getUserById(chatRoomRequest.getUserId());
        if (author == null) {
            throw new RuntimeException("Author not found");
        }

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setAuthor(author);
        chatRoom.setName(chatRoomRequest.getName());

        // Set room type
        chatRoom.setRoomType(
                chatRoomRequest.getRoomType() != null
                        ? chatRoomRequest.getRoomType()
                        : ChatRoomTypeEnum.PRIVATE
        );

        chatRoomRepository.save(chatRoom);
        return convertToDto(chatRoom);
    }


    private ChatRoomResponseDTO convertToDto(ChatRoom chatRoom) {
        ChatRoomResponseDTO dto = new ChatRoomResponseDTO();
        dto.setId(chatRoom.getId());
        dto.setRoomName(chatRoom.getName());

        // Add null check for author
        if (chatRoom.getAuthor() != null) {
            dto.setAuthorId(chatRoom.getAuthor().getId());
            dto.setAuthorName(chatRoom.getAuthor().getFullName());
        } else {
            // Set default values or null for author fields
            dto.setAuthorId(null);
            dto.setAuthorName("Unknown");
        }

        dto.setParticipantNames(chatRoom.getParticipants().stream()
                .map(User::getFullName)
                .collect(Collectors.toList()));
        return dto;
    }

    @Transactional
    public void joinChatRoom(JoinChatRequest joinChatRequest) {
        ChatRoom chatRoom = chatRoomRepository.findById(joinChatRequest.getChatRoomId())
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        User user = userRepository.getUserById(joinChatRequest.getUserId());
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // Different join logic based on room type
        switch (chatRoom.getRoomType()) {
            case PUBLIC:
                // Anyone can join public rooms
                break;

            case PRIVATE:
                // Only followers can join private rooms
                User author = chatRoom.getAuthor();
                if (!author.getFollowers().contains(user)) {
                    throw new RuntimeException("You must follow the author to join this private chat room");
                }
                break;
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

    public List<ChatMessageDto> getRoomChatHistory(Long roomId) {
        List<ChatMessage> mgs = chatMessageRepository.findByChatRoomIdOrderByTimestampAsc(roomId);
        return mgs.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ChatMessageDto convertToDto(ChatMessage chatMessage) {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(chatMessage.getId());
        dto.setContent(chatMessage.getContent());
    // Get sender name from repository
        User sender = userRepository.findFirstById(chatMessage.getSenderUser().getId());
        dto.setSender(sender != null ? sender.getFullName() : "Unknown User");
        dto.setChatRoomId(String.valueOf(chatMessage.getChatRoom().getId()));
        dto.setCreatedAt(chatMessage.getTimestamp());
        return dto;
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


        if (!chatRoom.getParticipants().contains(user)) {
            chatRoom.getParticipants().add(user);
            chatRoomRepository.save(chatRoom);
        }
    }
}