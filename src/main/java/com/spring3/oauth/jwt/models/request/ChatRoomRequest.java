package com.spring3.oauth.jwt.models.request;

import com.spring3.oauth.jwt.entity.enums.ChatRoomTypeEnum;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ChatRoomRequest {
    private String name;
    private long userId;
    private long roomId;
    private ChatRoomTypeEnum roomType;
}
