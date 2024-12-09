package com.spring3.oauth.jwt.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRoomResponseDTO {
    private long id;
    private long authorId;
    private String authorName;
    private String roomName;
    private int maxParticipants;
    private List<String> participantNames;
}
