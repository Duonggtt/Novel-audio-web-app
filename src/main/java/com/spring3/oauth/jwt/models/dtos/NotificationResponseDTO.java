package com.spring3.oauth.jwt.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationResponseDTO {
    private Integer id;
    private long userId;
    private String fullName;
    private Integer commentId;
    private String message;
    private String createdAt;
    private Boolean isRead;
}
