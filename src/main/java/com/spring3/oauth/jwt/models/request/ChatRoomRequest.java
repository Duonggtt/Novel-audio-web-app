package com.spring3.oauth.jwt.models.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ChatRoomRequest {
    private String name;
    private long userId;
}
