package com.spring3.oauth.jwt.models.dtos;

import com.spring3.oauth.jwt.entity.Novel;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChapterResponseDTO {
    private Integer id;
    private int chapterNo;
    private String title;
    private LocalDateTime releasedAt;
    private String contentDoc;
    private String thumbnailImageUrl;
    private Integer novelId;
}
