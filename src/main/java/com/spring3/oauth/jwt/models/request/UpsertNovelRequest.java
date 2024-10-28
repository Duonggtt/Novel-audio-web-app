package com.spring3.oauth.jwt.models.request;

import com.spring3.oauth.jwt.entity.Author;
import com.spring3.oauth.jwt.entity.Genre;
import com.spring3.oauth.jwt.entity.enums.NovelStatusEnum;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UpsertNovelRequest {
    private String slug;
    private String title;
    private String description;
    private NovelStatusEnum status;
    private String thumbnailImageUrl;
    private boolean isClosed;
    private int readCounts;
    private int totalChapters;
    private BigDecimal averageRatings;
    private int likeCounts;
    private Integer authorId;
    private List<Integer> genreIds;
}
