package com.spring3.oauth.jwt.models.dtos;

import com.spring3.oauth.jwt.entity.enums.NovelStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NovelDetailResponseDTO {
    private String slug;
    private String title;
    private String description;
    private LocalDateTime releasedAt;
    private NovelStatusEnum status;
    private boolean isClosed;
    private String thumbnailImageUrl;
    private int readCounts;
    private int totalChapters;
    private BigDecimal averageRatings;
    private int likeCounts;
    private String authorName;
    private List<String> genreNames;
    private boolean isLiked;
    private BigDecimal userRate;
}
