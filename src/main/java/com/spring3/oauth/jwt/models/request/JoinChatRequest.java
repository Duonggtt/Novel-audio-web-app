package com.spring3.oauth.jwt.models.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class JoinChatRequest {
    private long chatRoomId;
    private long userId;
}
