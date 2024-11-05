package com.spring3.oauth.jwt.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentResponseDTO {
    private Integer id;
    private String content;
    private LocalDateTime createdAt;
    private Integer userId;
    private String user_image_path;
    private String username;
    private Integer novelId;
    private Integer parentId;
}
