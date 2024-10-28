package com.spring3.oauth.jwt.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AudioFileResponseDTO {
    private Integer id;
    private String audioUrl;
    private Long duration;
    private Integer chapterId;
}
